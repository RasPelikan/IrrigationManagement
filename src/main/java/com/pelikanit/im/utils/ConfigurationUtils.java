package com.pelikanit.im.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationUtils {
	
	private static final Logger logger = Logger.getLogger(
			ConfigurationUtils.class.getCanonicalName());
	
	private static final String PROPS_TESTENV = "test.environment";
	private static final String PROPS_HTTPSADMIN_KEYSTORE = "httpsadmin.keystore";
	private static final String PROPS_HTTPSADMIN_KEYSTOREPASSWORD = "httpsadmin.keystore.password";
	private static final String PROPS_HTTPSADMIN_HOST = "httpsadmin.host";
	private static final String PROPS_HTTPSADMIN_PORT = "httpsadmin.port";

	private static final String POSTFIX_URL = ".url";
	private static final String POSTFIX_AREA = ".area";
	private static final String POSTFIX_TYPE = ".type";
	private static final String POSTFIX_HUMANITY = ".humanity";
	private static final String POSTFIX_GPIO = ".gpio";
	private static final String POSTFIX_DURATION = ".duration";
	private static final String POSTFIX_START = ".start";
	private static final String POSTFIX_DAYSOFWEEK = ".daysofweek";
	private static final String POSTFIX_IRRIGATORS = ".irrigators";
	
	private static final String PROPS_SENSOR_HUMANITY = "sensor.humanity.";
	private static final String PROPS_SENSOR_RAIN_URL = "sensor.rain.url";
	private static final String PROPS_CYCLES = "cycles";
	private static final String PROPS_CYCLE = "cycle.";
	private static final String PROPS_IRRIGATOR = "irrigator.";
	
	private Properties props;
	
	public ConfigurationUtils(final String[] args) {
		
		if (args.length < 1) {
			System.err.println("Expecting configuration-file as argument!");
		}
		if (args.length > 1) {
			System.err.println("Expecting configuration-file as argument!");
		}
		
		InputStream in = null;
		try {
			
			final File propsFile = new File(args[0]);
			in = new FileInputStream(propsFile);
			
			props = new Properties();
			props.load(in);
			
		} catch (Exception e) {
			
			logger.log(Level.SEVERE,
					"Could not parse properties '"
					+ args[0]
					+ "'",
					e);
			Runtime.getRuntime().exit(1);
			
		} finally {
			
			if (in != null) {
				
				try {
					in.close();
				} catch (Throwable e) {
					logger.log(Level.WARNING,
							"Could not close input-stream of properties '"
							+ args[1]
							+ "'",
							e);
				}
				
			}
			
		}
		
	}

	public String getHttpsAdminKeystore() {
		
		final String keystore = props.getProperty(PROPS_HTTPSADMIN_KEYSTORE);
		
		if (keystore == null) {
			throw new RuntimeException("No property '"
					+ PROPS_HTTPSADMIN_KEYSTORE
					+ "' given. Use 'keytool -genkey -keyalg RSA -alias selfsigned -keystore test.jks -storepass any_password_you_like -keysize 2048' to build it");
		}
		
		return keystore;
		
	}
	
	public String getHttpsAdminKeystorePassword() {
		
		return props.getProperty(PROPS_HTTPSADMIN_KEYSTOREPASSWORD);
		
	}

	public int getHttpsAdminPort() {
		
		return getIntProperty(PROPS_HTTPSADMIN_PORT);
		
	}
	
	public String getHttpsAdminHost() {
		
		return props.getProperty(PROPS_HTTPSADMIN_HOST);
		
	}
	
	public boolean isTestEnvironment() {
		
		return Boolean.TRUE.toString().equals(props.getProperty(PROPS_TESTENV));
		
	}
	
	public int[] getCycles() {
		
		return getIntPropertyArray(PROPS_CYCLES);
		
	}
	
	public String getHumanitySensorUrl(final int sensor) {
		
		final String key = PROPS_SENSOR_HUMANITY + Integer.toString(sensor) + POSTFIX_URL;
		return props.getProperty(key);
		
	}
	
	public String getRainSensorUrl() {
		
		final String key = PROPS_SENSOR_RAIN_URL;
		return props.getProperty(key);
		
	}
	
	public String getCycleStart(final int cycle) {
		
		final String key = PROPS_CYCLE + Integer.toString(cycle) + POSTFIX_START;
		return props.getProperty(key);
		
	}

	public int getCycleDuration(final int cycle) {
		
		final String key = PROPS_CYCLE + Integer.toString(cycle) + POSTFIX_DURATION;
		return getIntProperty(key);
		
	}
	
	public int[] getCycleIrrigators(final int cycle) {
		
		final String key = PROPS_CYCLE + Integer.toString(cycle) + POSTFIX_IRRIGATORS;
		return getIntPropertyArray(key);
		
	}

	public int[] getCycleDaysOfWeek(final int cycle) {
		
		final String key = PROPS_CYCLE + Integer.toString(cycle) + POSTFIX_DAYSOFWEEK;
		return getIntPropertyArray(key);
		
	}
	
	public int getIrrigatorArea(final int irrigator) {
		
		final String key = PROPS_IRRIGATOR + Integer.toString(irrigator) + POSTFIX_AREA;
		return getIntProperty(key);
		
	}

	public int[] getIrrigatorIrrigators(final int irrigator) {
		
		final String key = PROPS_IRRIGATOR + Integer.toString(irrigator) + POSTFIX_IRRIGATORS;
		return getIntPropertyArray(key);
		
	}
	
	public int getIrrigatorHumanitySensor(final int irrigator) {
		
		final String key = PROPS_IRRIGATOR + Integer.toString(irrigator) + POSTFIX_HUMANITY;
		return getIntProperty(key);
		
	}
	
	public String getIrrigatorType(final int irrigator) {
		
		final String key = PROPS_IRRIGATOR + Integer.toString(irrigator) + POSTFIX_TYPE;
		return props.getProperty(key);
		
	}

	public String getIrrigatorUrl(final int irrigator) {
		
		final String key = PROPS_IRRIGATOR + Integer.toString(irrigator) + POSTFIX_URL;
		return props.getProperty(key);
		
	}

	public String getIrrigatorGpio(final int irrigator) {
		
		final String key = PROPS_IRRIGATOR + Integer.toString(irrigator) + POSTFIX_GPIO;
		return props.getProperty(key);
		
	}
	
	private float getFloatProperty(final String propsName) {
		
		final String value = props.getProperty(propsName);
		if (value == null) {
			throw new NoSuchElementException(propsName);
		}
		try {
			
			return Float.parseFloat(value);
			
		} catch(Exception e) {
			
			throw new RuntimeException(
					"Could not read property '"
					+ propsName
					+ "' as float. Given value is '"
					+ value
					+ "'", e);
			
		}
		
	}
	
	private int[] getIntPropertyArray(final String propsName) {
		
		final String valuesString = props.getProperty(propsName);
		if (valuesString == null) {
			return null;
		}
		
		try {
			
			final String[] values = valuesString.split(",");
			final int[] result = new int[values.length];
			for (int i = 0; i < values.length; ++i) {
				result[i] = Integer.parseInt(values[i]);
			}
			return result;
			
		} catch(Exception e) {
			
			throw new RuntimeException(
					"Could not read property '"
					+ propsName
					+ "' as int-array. Given value is '"
					+ valuesString
					+ "'", e);
			
		}

	}
	
	private int getIntProperty(final String propsName) {
		
		final String value = props.getProperty(propsName);
		if (value == null) {
			throw new NoSuchElementException(propsName);
		}
		try {
			
			return Integer.parseInt(value);
			
		} catch(Exception e) {
			
			throw new RuntimeException(
					"Could not read property '"
					+ propsName
					+ "' as int. Given value is '"
					+ value
					+ "'", e);
			
		}
		
	}

}
