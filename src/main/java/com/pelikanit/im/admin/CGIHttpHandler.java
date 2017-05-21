package com.pelikanit.im.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@SuppressWarnings("restriction")
public abstract class CGIHttpHandler implements HttpHandler {
	
	protected abstract String getResourcePackage();
	
	public void handle(final HttpExchange exchange) throws IOException {
		
		//final Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
		final String pathInfo = exchange.getRequestURI().getPath();

		exchange.setAttribute("Content-type", "text/html;charset=UTF-8");
		exchange.sendResponseHeaders(200, 0);
		
		final byte[] buffer = new byte[1024];
		int read;
		
		final String resourceName = getResourcePackage() + pathInfo;
		try (final InputStream in = getClass().getClassLoader()
				.getResourceAsStream(resourceName);
				final OutputStream out = exchange.getResponseBody()) {
			
			while ((read = in.read(buffer)) != -1) {
				
				out.write(buffer, 0, read);
				
			}
			
		} catch (Exception e) {
			
			System.err.println(resourceName);
			e.printStackTrace();
			
		}
			
		exchange.close();
				
	}
		
	/**
	 * returns the url parameters in a map
	 * 
	 * @param query
	 * @return map
	 */
	protected Map<String, String> queryToMap(final String query) {
		
		Map<String, String> result = new HashMap<>();
		
		if (query != null) {
			for (String param : query.split("&")) {
				String pair[] = param.split("=");
				if (pair.length > 1) {
					result.put(pair[0], pair[1]);
				} else {
					result.put(pair[0], "");
				}
			}
		}
		
		return result;
		
	}

}
