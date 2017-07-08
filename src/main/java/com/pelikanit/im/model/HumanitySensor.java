package com.pelikanit.im.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class HumanitySensor implements UrlBasedComponent<Float> {

	private int id;

	private int connectTimeout;
	
	private int readTimeout;
	
	private String url;
	
	private float value = 1.0f;
	
	public void fetch() throws Exception {
		value = UrlBasedComponent.super.getValue(null);
	}

	@Override
	public int getConnectTimeout() {
		return connectTimeout;
	}
	
	@Override
	public int getReadTimeout() {
		return readTimeout;
	}
	
	@Override
	public Float parse(final String value) {
		return Float.parseFloat(value);
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}
	
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}
	
}
