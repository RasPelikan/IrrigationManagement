package com.pelikanit.im.model;

import java.util.logging.Logger;

public class LoggingIrrigator extends Irrigator {

	private static final Logger logger = Logger.getLogger(LoggingIrrigator.class.getCanonicalName());
	
	@Override
	protected void switchOn() {
		logger.info("irrigator '" + getId() + "': ON");
	}
	
	@Override
	protected void switchOff() {
		logger.info("irrigator '" + getId() + "': OFF");
	}
	
}
