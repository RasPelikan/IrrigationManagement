package com.pelikanit.im.model;

import com.pelikanit.im.utils.ConfigurationUtils;

public class UrlBasedIrrigator extends Irrigator implements UrlBasedComponent<String> {

	public static final String OK = "OK";

	private int connectTimeout;
	
	private int readTimeout;
	
	private String url;
	
    public UrlBasedIrrigator(
            final int id,
            final ConfigurationUtils config) {
        
        super(id, Type.URL);
        connectTimeout = config.getUrlComponentConnectTimeout();
        readTimeout = config.getUrlComponentReadTimeout();
        url = config.getIrrigatorUrl(id);
        
    }
	
	@Override
    protected void switchOn() {
		
        try {
            final String result = getValue(null);
            processResult(result);
        } catch (Exception e) {
            throw new RuntimeException("Could not switch off", e);
        }

	}
	
	@Override
    protected void switchOff() {
		
        try {
            final String result = getValue("?off=true");
            processResult(result);
        } catch (Exception e) {
            throw new RuntimeException("Could not switch off", e);
        }
		
	}
	
	/**
     * Override default behavior which would do a request over WiFi every second.
     * This is OK to keep the irrigator turn on but not necessary if turned off
     * since the WiFi based irrigators switch off automatically if not pinged every
     * 10 seconds.
     */
	@Override
    public void keepAlive() {
		
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
	
	@Override
    public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public String toString() {
        return "irrigator " + getId() + " is " + (isOn() ? "on" : "off");
	}
	
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}
	
}
