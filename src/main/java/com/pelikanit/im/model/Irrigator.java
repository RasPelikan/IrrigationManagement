package com.pelikanit.im.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public abstract class Irrigator {

	public static enum Type { URL, GPIO, LOG };
	
	private int id;
	
	private Type type;

	private HumanitySensor humanitySensor;
	
	private int area;
	
	private boolean on;
	
	protected abstract void switchOn() throws Exception;
	
	protected abstract void switchOff() throws Exception;

	public void on() throws Exception {
		
		if (isOn()) {
			return;
		}
		// mark as switched on before doing the switch-on
		// why? see off()
		on = true;
		switchOn();
		
	}
	
	public void off() throws Exception {
		
		if (!isOn()) {
			return;
		}
		// mark as switched off before doing the switch-off
		// is for safety reasons: if switching off fails
		// the next keep-alive
		on = false;
		switchOff();
		
	}
	
	public void keepAlive() throws Exception {
		
		if (isOn()) {
			switchOn();
		} else {
			switchOff();
		}
		
	}

	public boolean isOn() {
		return on;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Irrigator)) {
			return false;
		}
		return id == ((Irrigator) obj).getId();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public HumanitySensor getHumanitySensor() {
		return humanitySensor;
	}

	public void setHumanitySensor(HumanitySensor humanitySensor) {
		this.humanitySensor = humanitySensor;
	}
	
	public int getArea() {
		return area;
	}
	
	public void setArea(int area) {
		this.area = area;
	}
	
}
