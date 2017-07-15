package com.pelikanit.im.admin;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.plugins.server.sun.http.HttpContextBuilder;
import org.jboss.resteasy.spi.ResteasyDeployment;

import com.pelikanit.im.IrrigationManagement;
import com.pelikanit.im.admin.im.IrrigationManagementService;
import com.pelikanit.im.utils.ConfigurationUtils;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.BasicAuthenticator;

@SuppressWarnings("restriction")
public class HttpsAdmin {
	
	private static final Logger logger = Logger.getLogger(
			HttpsAdmin.class.getCanonicalName());
	
	public static class IconHandler implements HttpHandler {
		
		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			
			exchange.sendResponseHeaders(200, 0);
			final byte[] buffer = new byte[1024];
			int read;
			
			try (final InputStream in = getClass().getClassLoader().getResourceAsStream(
					"com/pelikanit/im/apple-touch-icon.png");
					final OutputStream out = exchange.getResponseBody()) {
				
				while ((read = in.read(buffer)) != -1) {
					
					out.write(buffer, 0, read);
					
				}
				
			} catch (Exception e) {
				
				e.printStackTrace();
				
			}
			
		}
		
	};
	
	private static final int REQUEST_BACKLOG = 5;
	
	private HttpsServer httpsServer;
	
	private ExecutorService executorService;
	
	private HttpContextBuilder httpContextBuilder;
	
	public HttpsAdmin(final ConfigurationUtils config, final IrrigationManagement im)
			throws Exception {
		
		final String host = config.getHttpsAdminHost();
		int port;
		try {
			port = config.getHttpsAdminPort();
		} catch (NoSuchElementException e) {
			port = 443;
		}
		
		final InetSocketAddress address;
		if (host == null) {
			address = new InetSocketAddress(port);
		} else {
			address = new InetSocketAddress(host, port);
		}
		
		executorService = java.util.concurrent.Executors.newFixedThreadPool(5);
		httpsServer = HttpsServer.create(address, REQUEST_BACKLOG);
		httpsServer.setExecutor(executorService);
		
        char[] passphrase = config.getHttpsAdminKeystorePassword().toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(config.getHttpsAdminKeystore()), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
             public void configure (final HttpsParameters params) {
                SSLContext c = getSSLContext();

                 // get the default parameters
                 SSLParameters sslparams = c.getDefaultSSLParameters();

                 sslparams.setNeedClientAuth(params.getNeedClientAuth());
                 sslparams.setWantClientAuth(params.getWantClientAuth());
                 sslparams.setCipherSuites(params.getCipherSuites());
                 sslparams.setProtocols(params.getProtocols());

                 params.setSSLParameters(sslparams);
             }
        });
	
		final HttpContext adminContext = httpsServer.createContext(
				IrrigationManagementHttpHandler.PATH, new IrrigationManagementHttpHandler());
		httpsServer.createContext("/apple-touch-icon.png", new IconHandler());
		final String username = config.getHttpsAdminUsername();
		final String password = config.getHttpsAdminPassword();
		if ((username != null) && !username.trim().isEmpty()) {
			adminContext.setAuthenticator(new BasicAuthenticator("IM") {
			
				@Override
		        public boolean checkCredentials(String user, String pwd) {
		            return username.equals(user) && password.equals(pwd);
		        }
				
		    });
		}
		
		httpContextBuilder = new HttpContextBuilder();
		httpContextBuilder.setPath("/rest");

		final ResteasyDeployment deployment = httpContextBuilder.getDeployment();

		deployment.getActualResourceClasses().add(IrrigationManagementService.class);
		
		httpContextBuilder.bind(httpsServer);

		final Dispatcher dispatcher = deployment.getDispatcher();
		dispatcher.getDefaultContextObjects().put(IrrigationManagement.class, im);

	}
	
	public void start() {
		
		httpsServer.start();
		
		logger.info("Init of HttpServer complete");
		
	}
	
	public void stop() {
		
		httpContextBuilder.cleanup();
		executorService.shutdown();	
		httpsServer.stop(5);
		logger.info("Shutdown of HttpServer done...");
		
	}
	
}
