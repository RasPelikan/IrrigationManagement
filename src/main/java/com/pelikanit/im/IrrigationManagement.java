package com.pelikanit.im;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.pelikanit.im.admin.HttpsAdmin;
import com.pelikanit.im.model.ActiveCycle;
import com.pelikanit.im.model.Cycle;
import com.pelikanit.im.model.GpioBasedIrrigator;
import com.pelikanit.im.model.HumanitySensor;
import com.pelikanit.im.model.Irrigator;
import com.pelikanit.im.model.Irrigator.Type;
import com.pelikanit.im.model.LoggingIrrigator;
import com.pelikanit.im.model.RainSensor;
import com.pelikanit.im.model.UrlBasedIrrigator;
import com.pelikanit.im.utils.ConfigurationUtils;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * Main-class. Responsible for properly startup and shutdown of all services:
 * <ul>
 * <li>Administration-HttpServer
 * <li>Gpio-Management
 * </ul>
 * 
 * @author stephan
 */
public class IrrigationManagement implements Shutdownable {

	private static final Logger logger = Logger
			.getLogger(IrrigationManagement.class.getCanonicalName());

	private static Thread shutdownHook;

	private static final Object shutdownMutex = new Object();

	private volatile int shutdown;

	private HttpsAdmin httpsAdmin;

	private Cycle[] cycles;

	private Map<Integer, HumanitySensor> humanitySensors;

	private RainSensor rainSensor;

	private Map<Integer, List<Irrigator>> irrigators;

	private ActiveCycle activeCycle;
	
	private GpioController gpio;

	
	/**
	 * Entry-point at startup.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {

		// process arguments
		final ConfigurationUtils config = new ConfigurationUtils(args);

		// build central instance
		IrrigationManagement main = new IrrigationManagement();
		try {

			// initialize admin-httpserver, sensors,...
			main.initialize(config);

			final Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.SECOND, 10);
			calendar.add(Calendar.MINUTE, 1);

			// wait until shutdown
			do {

				// go asleep
				synchronized (shutdownMutex) {

					final long before = System.currentTimeMillis();
					long timeToSleep = calendar.getTimeInMillis() - before;
					if (timeToSleep > 0) {
						try {
							shutdownMutex.wait(1000); // keepAlive interval
						} catch (InterruptedException e) {
							// not excepted but OK
						}
					}
					
					long timePassed = System.currentTimeMillis() - before;
					long timeLeft = timeToSleep
							- (System.currentTimeMillis() - before);
					if (timeLeft <= 0) {

						final Calendar intervalTimeout = (Calendar) calendar
								.clone();
						intervalTimeout.set(Calendar.SECOND, 0);

						main.interval(intervalTimeout);

						calendar.add(Calendar.MINUTE, 1);

					} else if (timePassed > 500) {
						
						main.keepAlive();
						
					}

				}

			} while (!main.isShutdown());

		} catch (Throwable e) {

			logger.log(Level.SEVERE, "Error running application", e);

			Runtime.getRuntime().removeShutdownHook(shutdownHook);

		}
		// shutdown
		finally {

			// stop admin-httpserver
			main.stop(config);

		}

		// can be used to identify intentional shutdown
		logger.info("Shutdown return code: " + main.shutdown);
		System.exit(main.shutdown);
		
	}

	// constructor
	public IrrigationManagement() {

		// initialize properties
		shutdown = -1;
		activeCycle = null;

		// capture "kill" commands and shutdown regularly
		shutdownHook = new Thread() {

			@Override
			public void run() {

				// shutdown
				shutdown(1);

			}

		};
		Runtime.getRuntime().addShutdownHook(shutdownHook);

	}

	/**
	 * @return whether anything caused a shutdown
	 */
	public boolean isShutdown() {

		return shutdown != -1;

	}

	/**
	 * Checks every minute whether something to do
	 * 
	 * @param calendar
	 *            The minute
	 */
	private void interval(final Calendar calendar) {

		if (activeCycle == null) {
			activeCycle = getNewCycle(calendar);
		}
		if (activeCycle != null) {
			activeCycle = activeCycle.update(calendar.getTime());
		}

	}
	
	/**
	 * Does a keep-alive ping to every irriaator of the active cycle.
	 * 
	 * This is necessary because Wifi-based irrigators automatically
	 * switch of if there is no keep alive every second.
	 * 
	 * For Gpio based irrigators the keep alive turns them off to be
	 * sure water is not flowing if not required.
	 */
	private void keepAlive() {
		
		final Set<Irrigator> nonCollectiveIrrigators = getNonCollectiveIrrigators();
				
		for (final Irrigator irrigator : nonCollectiveIrrigators) {
		
			try {
				irrigator.keepAlive();
			} catch (Exception e) {
				logger.log(Level.WARNING, "Could not do keepalive for irrigator '"
						+ irrigator.getId() + "'", e);
			}
			
		}
		
	}

	/**
	 * Find a cycle matching the given calendar
	 * 
	 * @param calendar
	 * @return The matching cycle
	 */
	private ActiveCycle getNewCycle(final Calendar calendar) {

		final String start = String.format("%02d%02d",
				calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE));
		for (final Cycle cycle : cycles) {

			if (start.equals(cycle.getStart())) {

				final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
				for (final int cycleDay : cycle.getDaysOfWeek()) {
					if (dayOfWeek == cycleDay) {

						return new ActiveCycle(cycle);

					}
				}

			}

		}

		return null;

	}

	/**
	 * Shut's the mowing-robot down
	 */
	@Override
	public void shutdown(int returnCode) {

		// wake-up main-method to shutdown all services immediately
		synchronized (shutdownMutex) {

			shutdown = returnCode;
			shutdownMutex.notify();

		}

	}
	
	private void stopIrrigators() {
		
		final Set<Irrigator> nonCollectiveIrrigators = getNonCollectiveIrrigators();
		
		for (final Irrigator irrigator : nonCollectiveIrrigators) {
			try {
				irrigator.off();
			} catch (Exception e) {
				logger.log(Level.WARNING, "Could not stop irrigator '" + irrigator.getId() + "'", e);
			}
		}
		
	}
	
	private void stopGpio(final ConfigurationUtils config) {
		
		if (!config.isTestEnvironment()) {
			gpio.shutdown();
		}
		
	}

	private void stop(final ConfigurationUtils config) {

		stopIrrigators();
		stopGpio(config);
		
		if (httpsAdmin != null) {
			httpsAdmin.stop();
		}

		logger.info("Shutdown complete");

	}

	private void initialize(final ConfigurationUtils config) throws Exception {

		initializeGpio(config);
		initializeHttpsClients();
		initializeAdminHttpServer(config);

		initializeSensors(config);
		initializeIrrigators(config);
		initializeCycles(config);
		dumpConfig();

		logger.info("Init complete");

	}

	private void initializeHttpsClients() {
		
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { 
		    new X509TrustManager() {     
		        public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
		            return new X509Certificate[0];
		        } 
		        public void checkClientTrusted( 
		            java.security.cert.X509Certificate[] certs, String authType) {
		            } 
		        public void checkServerTrusted( 
		            java.security.cert.X509Certificate[] certs, String authType) {
		        }
		    } 
		}; 

		// Install the all-trusting trust manager
		try {
		    SSLContext sc = SSLContext.getInstance("SSL"); 
		    sc.init(null, trustAllCerts, new java.security.SecureRandom()); 
		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (GeneralSecurityException e) {
			logger.log(Level.WARNING, "Could not force to use self signed certificates", e);
		} 
		
		// Accept all hostnames
		HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
		
	}
	
	private void initializeGpio(final ConfigurationUtils config) {

		if (!config.isTestEnvironment()) {
			gpio = GpioFactory.getInstance();
		}

	}

	private void initializeSensors(final ConfigurationUtils config) {

		humanitySensors = new HashMap<Integer, HumanitySensor>();

		for (int id = 0; id < 100; ++id) {

			final String url = config.getHumanitySensorUrl(id);
			if (url == null) {
				break;
			}

			final HumanitySensor sensor = new HumanitySensor();
			humanitySensors.put(id, sensor);

			sensor.setId(id);
			sensor.setUrl(url);
			sensor.setConnectTimeout(config.getUrlComponentConnectTimeout());
			sensor.setReadTimeout(config.getUrlComponentReadTimeout());

		}

		final String url = config.getRainSensorUrl();
		if (url != null) {

			rainSensor = new RainSensor();
			rainSensor.setUrl(url);
			rainSensor.setConnectTimeout(config.getUrlComponentConnectTimeout());
			rainSensor.setReadTimeout(config.getUrlComponentReadTimeout());

		}

	}

	private void initializeIrrigators(final ConfigurationUtils config) {

		irrigators = new HashMap<Integer, List<Irrigator>>();

		final HashMap<Integer, Irrigator> cache = new HashMap<Integer, Irrigator>();
		for (int id = 0; id < 100; ++id) {

			final int[] subIrrigatorIds = config.getIrrigatorIrrigators(id);

			final List<Irrigator> subIrrigators = new LinkedList<Irrigator>();
			if (subIrrigatorIds != null) {

				for (final int subIrrigatorId : subIrrigatorIds) {

					final Irrigator irrigator = cache.get(subIrrigatorId);
					if (irrigator == null) {
						throw new RuntimeException("Unknown irrigator '"
								+ subIrrigatorId + "'");
					}
					subIrrigators.add(irrigator);

				}

			} else {

				final String typeStr = config.getIrrigatorType(id);
				if (typeStr == null) {
					return;
				}

				final Type type = Irrigator.Type.valueOf(typeStr.toUpperCase());

				final Irrigator common;
				switch (type) {
				case GPIO: {

					final GpioBasedIrrigator irrigator = new GpioBasedIrrigator();
					common = irrigator;

					irrigator.setType(type);

					final String gpioPinName = config.getIrrigatorGpio(id);
					
					final GpioPinDigitalOutput gpioPin = gpio.provisionDigitalOutputPin(
							RaspiPin.getPinByName(gpioPinName),
							"Irrigator" + irrigator.getId(),
							PinState.HIGH);
					gpioPin.setShutdownOptions(true, PinState.HIGH,
							PinPullResistance.OFF, PinMode.DIGITAL_INPUT);
					
					irrigator.setGpio(gpioPin);

					break;
				}
				case URL: {

					final UrlBasedIrrigator irrigator = new UrlBasedIrrigator();
					common = irrigator;

					irrigator.setType(type);

					irrigator.setUrl(config.getIrrigatorUrl(id));
					irrigator.setConnectTimeout(config.getUrlComponentConnectTimeout());
					irrigator.setReadTimeout(config.getUrlComponentReadTimeout());

					break;
				}
				case LOG: {

					final LoggingIrrigator irrigator = new LoggingIrrigator();
					common = irrigator;

					irrigator.setType(type);

					break;
				}
				default:
					throw new RuntimeException("Unsupported type '" + type
							+ "'");
				}

				subIrrigators.add(common);
				cache.put(id, common);

				common.setId(id);

				final int area = config.getIrrigatorArea(id);
				common.setArea(area);

				try {
					final int sensorId = config.getIrrigatorHumanitySensor(id);
					final HumanitySensor sensor = humanitySensors.get(sensorId);
					if (sensor == null) {
						throw new RuntimeException("Unknown humanity sensor '"
								+ sensorId + "' used by irrigator '" + id + "'");
					}
					common.setHumanitySensor(sensor);
				} catch (Exception e) {
					logger.log(Level.INFO, "Could not initialize humanity sensor - "
							+ "using no sensor!", e);
					common.setHumanitySensor(null);
				}

				// turn of on startup
				try {
					common.off();
				} catch (Exception e) {
					logger.log(Level.WARNING,
							"Could not initialize irrigator to OFF", e);
				}

			}
			irrigators.put(id, subIrrigators);

		}

	}

	private void initializeCycles(final ConfigurationUtils config) {

		final int[] cycleIds = config.getCycles();
		if (cycleIds == null) {
			cycles = new Cycle[0];
			return;
		}

		cycles = new Cycle[cycleIds.length];
		for (int i = 0; i < cycleIds.length; ++i) {

			final Cycle cycle = new Cycle();
			cycles[i] = cycle;

			final int id = cycleIds[i];
			cycle.setId(id);

			final int[] daysOfWeek = config.getCycleDaysOfWeek(id);
			cycle.setDaysOfWeek(daysOfWeek);

			final int duration = config.getCycleDuration(id);
			cycle.setDuration(duration);

			final String start = config.getCycleStart(id);
			cycle.setStart(start);

			final LinkedList<List<Irrigator>> cycleIrrigators = new LinkedList<>();
			cycle.setIrrigators(cycleIrrigators);

			final int[] irrigatorIds = config.getCycleIrrigators(id);
			if (irrigatorIds != null) {
				for (final int irrigatorId : irrigatorIds) {

					final List<Irrigator> irrigatorsInCycle = irrigators
							.get(irrigatorId);
					if (irrigatorsInCycle != null) {
						cycleIrrigators.add(irrigatorsInCycle);
					}

				}
			}

		}

	}

	/**
	 * Initialize the Admin-HttpServer
	 * 
	 * @throws Exception
	 */
	private void initializeAdminHttpServer(final ConfigurationUtils config)
			throws Exception {

		httpsAdmin = new HttpsAdmin(config, this);

		httpsAdmin.start();

	}

	private void dumpConfig() {

		logger.log(Level.INFO, "rain sensor: {0}", rainSensor);
		logger.log(Level.INFO, "{0} humanity sensors:", humanitySensors.size());
		for (final HumanitySensor sensor : humanitySensors.values()) {
			logger.log(Level.INFO, "humanity sensor: {0}", sensor);
		}
		logger.log(Level.INFO, "{0} cycles:", cycles.length);
		for (final Cycle cycle : cycles) {
			logger.log(Level.INFO, "Cycle: {0}", cycle);
		}

	}

	public Status getStatus() {
		
		final Status result = new Status();
		
		result.setIrrigators(getNonCollectiveIrrigators());
		
		return result;
		
	}
	
	public void switchOn(final int irrigatorId) {
		
		irrigators.get(irrigatorId).forEach(i -> {
			try {
				i.on();
			} catch (Exception e) {
				throw new RuntimeException("irrigator '" + i.getId() + "' failed", e);
			}
		});
		
	}
	
	public void switchOff(final int irrigatorId) throws Exception {
		
		irrigators.get(irrigatorId).forEach(i -> {
			try {
				i.off();
			} catch (Exception e) {
				throw new RuntimeException("irrigator '" + i.getId() + "' failed", e);
			}
		});
		
	}
	
	private Set<Irrigator> getNonCollectiveIrrigators() {
		
		final HashSet<Irrigator> nonCollectiveIrrigators = new HashSet<>();
		irrigators.values().forEach((i) -> 
				nonCollectiveIrrigators.addAll(i));
		return nonCollectiveIrrigators;
		
	}
		
}
