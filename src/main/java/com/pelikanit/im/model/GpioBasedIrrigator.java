package com.pelikanit.im.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

public class GpioBasedIrrigator extends Irrigator {

	private GpioPinDigitalOutput gpio;
	
	@Override
	protected void switchOn() {
		gpio.setState(PinState.LOW);
	}
	
	@Override
	protected void switchOff() {
		gpio.setState(PinState.HIGH);
	}
	
	public GpioPinDigitalOutput getGpio() {
		return gpio;
	}
	
	public void setGpio(GpioPinDigitalOutput gpio) {
		this.gpio = gpio;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
}
