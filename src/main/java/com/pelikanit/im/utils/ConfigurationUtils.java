package com.pelikanit.im.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pelikanit.im.model.Cycle;
import com.pelikanit.im.model.GpioBasedIrrigator;
import com.pelikanit.im.model.Irrigator;
import com.pelikanit.im.model.Irrigator.Type;
import com.pelikanit.im.model.LoggingIrrigator;
import com.pelikanit.im.model.UrlBasedIrrigator;

public class ConfigurationUtils {
	
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationUtils.class);
	
	private static final String PROPS_TESTENV = "test.environment";
    private static final String PROPS_ADMIN_HOST = "admin.host";
    private static final String PROPS_ADMIN_PORT = "admin.port";
    private static final String PROPS_ADMIN_USERNAME = "admin.username";
    private static final String PROPS_ADMIN_PASSWORD = "admin.password";

    private static final String POSTFIX_URL = ".url";
    private static final String POSTFIX_TYPE = ".type";
    private static final String POSTFIX_GPIO = ".gpio";
    private static final String POSTFIX_GPIO_INVERSE = ".gpio-inverse";
    private static final String POSTFIX_END = ".end";
    private static final String POSTFIX_START = ".start";
    private static final String POSTFIX_IRRIGATORS = ".irrigators";
    private static final String POSTFIX_INTERVAL = ".interval";

    private static final String PROPS_INTERVAL_START = "interval.start";
    private static final String PROPS_KEEP_ALIVE = "keepalive.seconds";

    private static final String PROPS_CYCLE = "cycle.";
    private static final String PROPS_IRRIGATOR = "irrigator.";

    private static final String PROPS_URL_CONNECT_TIMEOUT = "url.timeout.connect";
    private static final String PROPS_URL_READ_TIMEOUT = "url.timeout.read";

	private Properties props;
	
	private Map<Integer, Irrigator> irrigators;
	
	private Map<Integer, Cycle> cycles;
	
    public ConfigurationUtils(final Properties props) {

        this.props = props;

        initialize();

    }

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
			
            initialize();

		} catch (Exception e) {
			
            logger.error("Could not parse properties '{}'", args[0], e);
			Runtime.getRuntime().exit(1);
			
		} finally {
			
			if (in != null) {
				
				try {
					in.close();
				} catch (Throwable e) {
                    logger.error("Could not close input-stream of properties '{}'", args[1], e);
				}
				
			}
			
		}
		
	}
	
	private void initialize() {
	    
        irrigators = new HashMap<>();
        props
                .keySet()
                .stream()
                .map(key -> key.toString())
                .filter(key -> key.startsWith(PROPS_IRRIGATOR))
                .filter(key -> key.endsWith(POSTFIX_TYPE))
                .map(key -> key.substring(PROPS_IRRIGATOR.length(), key.length() - POSTFIX_TYPE.length()))
                .map(irrigatorId -> Integer.parseInt(irrigatorId))
                .map(irrigatorId -> getIrrigator(irrigatorId))
                .forEach(irrigator -> irrigators.put(irrigator.getId(), irrigator));
        
        cycles = props
                .keySet()
                .stream()
                .map(key -> key.toString())
                .filter(key -> key.startsWith(PROPS_CYCLE))
                .filter(key -> key.endsWith(POSTFIX_START))
                .map(key -> key.substring(PROPS_CYCLE.length(), key.length() - POSTFIX_START.length()))
                .map(cycleId -> Integer.parseInt(cycleId))
                .map(cycleId -> new Cycle(cycleId, this))
                .collect(Collectors.toMap(
                        cycle -> cycle.getId(),
                        cycle -> cycle));
	    
	}

    public int getAdminPort() {
		
        return getIntProperty(PROPS_ADMIN_PORT);
		
	}
	
    public String getAdminUsername() {
		
        return props.getProperty(PROPS_ADMIN_USERNAME);
		
	}
	
    public String getAdminPassword() {
		
        return props.getProperty(PROPS_ADMIN_PASSWORD);
		
	}
	
    public String getAdminHost() {
		
        return props.getProperty(PROPS_ADMIN_HOST);
		
	}
	
	public boolean isTestEnvironment() {
		
		return Boolean.TRUE.toString().equals(props.getProperty(PROPS_TESTENV));
		
	}

    public int getUrlComponentConnectTimeout() {

        return getIntProperty(PROPS_URL_CONNECT_TIMEOUT);

    }

    public int getUrlComponentReadTimeout() {

        return getIntProperty(PROPS_URL_READ_TIMEOUT);

    }

    public Collection<Cycle> getCycles() {

        return cycles.values();

    }

    public Cycle getCycle(final int id) {

        return cycles.get(id);

    }

    public Collection<Irrigator> getIrrigators() {
        
        return irrigators.values();

    }

    public Irrigator getIrrigator(final int id) {

        if (irrigators.containsKey(id)) {
            return irrigators.get(id);
        }

        final var type = getIrrigatorType(id);
        Irrigator irrigator;
        switch (type) {
        case URL:
            irrigator = new UrlBasedIrrigator(id, this);
            break;
        case GPIO:
            irrigator = new GpioBasedIrrigator(id, this);
            break;
        default:
            irrigator = new LoggingIrrigator(id, this);
        }

        irrigators.put(id, irrigator);
        return irrigator;
        
    }

    public LocalDate getIntervalStart() {

        return getDateProperty(PROPS_INTERVAL_START);

    }

    public int getCycleStart(final int cycle) {

        final String key = PROPS_CYCLE + Integer.toString(cycle) + POSTFIX_START;
        return getIntProperty(key);

    }

    public int getCycleEnd(final int cycle) {

        final String key = PROPS_CYCLE + Integer.toString(cycle) + POSTFIX_END;
        return getIntProperty(key);

    }

    public int[] getCycleIrrigators(final int cycle) {

        final String key = PROPS_CYCLE + Integer.toString(cycle) + POSTFIX_IRRIGATORS;
        return getIntPropertyArray(key);

    }

    public int getCycleInterval(final int cycle) {

        final String key = PROPS_CYCLE + Integer.toString(cycle) + POSTFIX_INTERVAL;
        return getIntProperty(key);

    }

    public int[] getIrrigatorIrrigators(final int irrigator) {

        final String key = PROPS_IRRIGATOR + Integer.toString(irrigator) + POSTFIX_IRRIGATORS;
        return getIntPropertyArray(key);

    }

    public Type getIrrigatorType(final int irrigator) {

        final String key = PROPS_IRRIGATOR + Integer.toString(irrigator) + POSTFIX_TYPE;
        final String value = props.getProperty(key);
        if (value == null) {
            return Type.LOG;
        }
        return Type.valueOf(value.toUpperCase());

    }

    public String getIrrigatorUrl(final int irrigator) {

        final String key = PROPS_IRRIGATOR + Integer.toString(irrigator) + POSTFIX_URL;
        return props.getProperty(key);

    }

    public int getIrrigatorGpio(final int irrigator) {

        final String key = PROPS_IRRIGATOR + Integer.toString(irrigator) + POSTFIX_GPIO;
        return getIntProperty(key);

    }

    public boolean isIrrigatorGpioInverse(final int irrigator) {

        final String key = PROPS_IRRIGATOR + Integer.toString(irrigator) + POSTFIX_GPIO_INVERSE;
        return Boolean.TRUE.toString().equals(props.getProperty(key));

    }

    public int getKeepAliveSeconds() {

        return getIntProperty(PROPS_KEEP_ALIVE);

    }

	@SuppressWarnings("unused")
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
	
    private LocalDate getDateProperty(final String propsName) {
        
        final String value = props.getProperty(propsName);
        if (value == null) {
            return LocalDate.now();
        }
        try {
            
            return LocalDate.parse(value);
            
        } catch(Exception e) {
            
            throw new RuntimeException(
                    "Could not read property '"
                    + propsName
                    + "' as date in format 'YYYY-MM-DD'. Given value is '"
                    + value
                    + "'", e);
            
        }
        
    }

}
