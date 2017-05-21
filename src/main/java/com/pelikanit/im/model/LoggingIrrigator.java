package com.pelikanit.im.model;

import java.util.logging.Logger;

public class LoggingIrrigator extends Irrigator {

	private static final Logger logger = Logger.getLogger(LoggingIrrigator.class.getCanonicalName());
	
	@Override
	public void on() {
		logger.info("irrigator '" + getId() + "': ON");
	}
	
	@Override
	public void off() {
		logger.info("irrigator '" + getId() + "': OFF");
	}
	
}
