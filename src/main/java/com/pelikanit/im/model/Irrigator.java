package com.pelikanit.im.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public abstract class Irrigator {

	public static enum Type { URL, GPIO, LOG };
	
	private int id;
	
	private Type type;

	private HumanitySensor humanitySensor;
	
	private int area;
	
	public abstract void on();
	
	public abstract void off();

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
		if (!(obj instanceof Cycle)) {
			return false;
		}
		return id == ((Cycle) obj).getId();
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
