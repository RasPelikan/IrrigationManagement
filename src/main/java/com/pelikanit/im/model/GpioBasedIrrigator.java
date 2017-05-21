package com.pelikanit.im.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class GpioBasedIrrigator extends Irrigator {

	private String gpio;
	
	@Override
	public void on() {
		// switch on gpio
	}
	
	@Override
	public void off() {
		// switch off gpio
	}
	
	public String getGpio() {
		return gpio;
	}
	
	public void setGpio(String gpio) {
		this.gpio = gpio;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
}
