package com.pelikanit.im.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class UrlBasedIrrigator extends Irrigator implements UrlBasedComponent<String> {

	public static final String OK = "OK";

	private int connectTimeout;
	
	private int readTimeout;
	
	private String url;
	
	@Override
	protected void switchOn() throws Exception {
		
		final String result = getValue(null);
		processResult(result);

	}
	
	@Override
	protected void switchOff() throws Exception {
		
		final String result = getValue("?off=true");
		processResult(result);
		
	}
	
	/**
	 * Override default behavior which would 
	 * do a request over WiFi every second. This is
	 * OK to keep the irrigator turn on but not 
	 * necessary if turned off since the WiFi based
	 * irrigators switch off automatically if not
	 * pinged every second.
	 */
	@Override
	public void keepAlive() throws Exception {
		
		if (!isOn()) {
			return;
		}
		switchOn();
		
	}
	
	private void processResult(final String result) throws Exception {
		
		if (OK.equals(result)) {
			return;
		}
		throw new Exception(result);
		
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
	public String parse(final String value) {
		return value;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}
	
}
