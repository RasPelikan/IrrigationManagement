package com.pelikanit.im.admin.im;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.pelikanit.im.IrrigationManagement;

@Path("/im")
@Produces(MediaType.APPLICATION_JSON)
public class IrrigationManagementService {
	
	private final static Logger logger = Logger.getLogger(
			IrrigationManagementService.class.getCanonicalName());
	
	@Context
	private IrrigationManagement irrigationManagement;
	
	@GET
	@Path("/shutdown")
	public String shutdown(@QueryParam("returnCode") Integer returnCode) {
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				// wait until response is sent to the browser
				try {
					
					Thread.sleep(1000);
					
				} catch (InterruptedException e) {
					// never mind
				}
				
				final int code;
				if (returnCode != null) {
					code = returnCode;
				} else {
					code = 1;
				}
				irrigationManagement.shutdown(code);
				
			}
			
		}).start();
		
		return "{\"status\":\"SHUTDOWN now...\"}";
		
	}
	
	@GET
	@Path("/status")
	public Status status() {
		
		final Status result = new Status();
		
		result.setPaused(irrigationManagement.isPaused());
		
		final List<Irrigator> irrigators = new LinkedList<>();
		result.setIrrigators(irrigators);
		
		final com.pelikanit.im.Status status = irrigationManagement.getStatus();
		if (status.getIrrigators() != null) {
			status.getIrrigators().forEach(i -> {
				final Irrigator irrigator = new Irrigator();
				irrigator.setId(i.getId());
				irrigator.setOn(i.isOn());
				irrigator.setType(i.getType().name());
				irrigators.add(irrigator);
			});
		}
		
		return result;
		
	}

	@GET
	@Path("/pause")
	public String pause(@QueryParam("paused") final boolean paused) throws Exception {
		
		irrigationManagement.setPaused(paused);
		return "{\"status\": \"OK\"}";
		
	}

	@GET
	@Path("/irrigator/{id}")
	public String switchIrrigator(@PathParam("id") final int id,
			final @QueryParam("off") boolean off) throws Exception {
		
		try {
			if (off) {
				irrigationManagement.switchOff(id);
			} else {
				irrigationManagement.switchOn(id);
			}
			return "{\"status\": \"OK\"}";
		} catch (RuntimeException e) {
			logger.log(Level.WARNING, e.getMessage(), e.getCause());
			return "{\"status\": \"" + e.getMessage() + "\"}";
		}
		
	}
	
	private static final int IRRIGATOR_SITUATION_NORMAL = 3;
	private static final int IRRIGATOR_SITUATION_ERROR = 0;
	private static final int IRRIGATOR_SITUATION_TIMEOUT = 1;
	private static final int IRRIGATOR_SITUATION_EXECPTION = 2;
	
	@GET
	@Path("/simulator/irrigator/{id}")
	public String irrigator(@PathParam("id") final int id,
			final @QueryParam("off") boolean off) throws Exception {
		
		final int situation = (int) System.currentTimeMillis() % 8;
		switch (situation) {
		case IRRIGATOR_SITUATION_ERROR:
			logger.info("Irrigator '" + id + "': error");
			return "Whatever";
		case IRRIGATOR_SITUATION_EXECPTION:
			logger.info("Irrigator '" + id + "': exception");
			throw new Exception("Unexpected");
		case IRRIGATOR_SITUATION_TIMEOUT:
			logger.info("Irrigator '" + id + "': timeout");
			Thread.sleep(30000);
		}
		
		logger.info("Irrigator '" + id + "': " + (off ? "off" : "on"));
		return "OK";
		
	}
	
	@GET
	@Path("/simulator/sensor/humanity")
	public String humanitySensor() throws Exception {
		
		final int situation = (int) System.currentTimeMillis() % 8;
		float value = ((float) (situation - IRRIGATOR_SITUATION_NORMAL)) / 4;
		switch (situation) {
		case IRRIGATOR_SITUATION_ERROR:
			logger.info("Humanity sensor: error");
			return "Whatever";
		case IRRIGATOR_SITUATION_EXECPTION:
			logger.info("Humanity sensor: exception");
			throw new Exception("Unexpected");
		case IRRIGATOR_SITUATION_TIMEOUT:
			logger.info("Humanity sensor: timeout");
			Thread.sleep(30000);
			value = 0.9f;
		}
		
		final String result = Float.toString(value);
		logger.info("Humanity sensor: " + result);
		return result;
		
	}

	
	@GET
	@Path("/simulator/sensor/rain")
	public String rainSensor() throws Exception {
		
		boolean rain = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) % 2 == 0;
		
		final int situation = (int) System.currentTimeMillis() % 8;
		float value = ((float) (situation - IRRIGATOR_SITUATION_NORMAL)) / 4;
		switch (situation) {
		case IRRIGATOR_SITUATION_ERROR:
			logger.info("Rain sensor: error");
			return "Whatever";
		case IRRIGATOR_SITUATION_EXECPTION:
			logger.info("Rain sensor: exception");
			throw new Exception("Unexpected");
		case IRRIGATOR_SITUATION_TIMEOUT:
			logger.info("Rain sensor: timeout");
			Thread.sleep(30000);
			value = 0.9f;
		}
		
		final String result;
		if (rain) {
			result = Float.toString(value);
		} else {
			result = "0";
		}
		logger.info("Rain sensor: " + result);
		return result;
		
	}
	
}
