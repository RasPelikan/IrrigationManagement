package com.pelikanit.im.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class RainSensor implements UrlBasedComponent<Float> {

	private int connectTimeout;
	
	private int readTimeout;
	
	private String url;
	
	private float value;

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
