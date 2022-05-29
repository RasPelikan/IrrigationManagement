package com.pelikanit.im.model;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface UrlBasedComponent<T> {

	static Logger logger = Logger.getLogger(UrlBasedComponent.class.getCanonicalName());
	
	String getUrl();
	
	int getConnectTimeout();
	
	int getReadTimeout();

	T parse(final String value);
	
	default T getValue(final String query) throws Exception {
		
		final String urlString;
		if (query != null) {
			urlString = getUrl() + query;
		} else {
			urlString = getUrl();
		}
		final URL url = new URL(urlString);
		
		final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		InputStream in = null;
		try {
			
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(getConnectTimeout());
			connection.setReadTimeout(getReadTimeout());
			connection.setUseCaches(false);
			HttpURLConnection.setFollowRedirects(false);
			connection.setAllowUserInteraction(false);
			
			final int responseCode = connection.getResponseCode();
			
			in = connection.getInputStream();
			if (in == null) {
				in = connection.getErrorStream();
			}
			
			final StringBuilder result = new StringBuilder();
			final byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				result.append(new String(buffer, 0, read, "ISO-8859-1"));
			}
			
			final String msg = result.toString();
            if (responseCode != 200) {
                throw new RuntimeException(msg);
			}

			return parse(msg);
			
		} finally {

			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					logger.log(Level.WARNING, "Could not close input stream", e);
				}
			}
			
			if (connection != null) {
				try {
					connection.disconnect();
				} catch (Exception e) {
					logger.log(Level.WARNING, "Could not disconnect", e);
				}
			}
			
		}
		
	}
	
}
