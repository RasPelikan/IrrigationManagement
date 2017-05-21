package com.pelikanit.im.admin.im;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.pelikanit.im.IrrigationManagement;

@Path("/im")
@Produces(MediaType.APPLICATION_JSON)
public class IrrigationManagementService {
	
	@Context
	private IrrigationManagement irrigationManagement;
	
	@GET
	@Path("/shutdown")
	public String shutdown() {
		
		irrigationManagement.shutdown();
		
		/*
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				// wait until response is sent to the browser
				synchronized (shutdownMutex) {
					
					try {
						
						shutdownMutex.wait();
						
					} catch (InterruptedException e) {
						// never mind
					}
					
				}
				
				irrigationManagement.shutdown();
				
			}
			
		}).start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// never mind
		}
		*/
		
		return "SHUTDOWN now...";
		
		/*
		if (shutdown) {
			
			synchronized (shutdownMutex) {
				
				shutdownMutex.notify();
				
			}
			
		}
		 */
	}
		
	@GET
	@Path("/status")
	public Status status() {
		
		final Status result = new Status();
		
		//final com.pelikanit.im.Status status = irrigationManagement.getStatus();
		
		return result;
		
	}
		
}
