package com.pelikanit.im.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pelikanit.im.utils.ConfigurationUtils;

public class LoggingIrrigator extends Irrigator {

    private static final Logger logger = LoggerFactory.getLogger(LoggingIrrigator.class);
	
	public LoggingIrrigator(
	        final int id,
	        final ConfigurationUtils config) {
	    
	    super(id, Type.LOG);
        
    }
	
	@Override
	protected void switchOn() {
		logger.info("irrigator '" + getId() + "': ON");
	}
	
	@Override
	protected void switchOff() {
		logger.info("irrigator '" + getId() + "': OFF");
	}
	
}
