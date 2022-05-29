package com.pelikanit.im.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pelikanit.im.IrrigationManagement;

import io.javalin.Javalin;
import io.javalin.http.Context;

public class Controller {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    
    private final IrrigationManagement management;
    
    public Controller(
            final IrrigationManagement management,
            final Javalin httpConfig) {

        this.management = management;
        
        httpConfig.get("/rest/shutdown", this::shutdown);
        httpConfig.get("/rest/status", this::status);
        httpConfig.get("/rest/pause", this::pause);
        httpConfig.get("/rest/irrigator/{id}", this::switchIrrigator);

    }

    public void stop() {

    }

    public void shutdown(
            final Context ctx) throws Exception {

        final String returnCode = ctx.queryParam("returnCode");
        final int code;
        if (returnCode != null) {
            code = Integer.parseInt(returnCode);
        } else {
            code = 1;
        }

        new Thread(new Runnable() {
            
            @Override
            public void run() {
                
                // wait until response is sent to the browser
                try {
                    
                    Thread.sleep(1000);
                    
                } catch (InterruptedException e) {
                    // never mind
                }
                
                management.shutdown(code);
                
            }
            
        }).start();
        
        ctx.result("{\"status\":\"SHUTDOWN now...\"}");
        
    }

    public void status(
            final Context ctx) throws Exception {
        
        final Status status = management.getStatus();
        
        ctx.json(status);
        
    }

    public void pause(
            final Context ctx) throws Exception {
        
        final var paused = Boolean.parseBoolean(ctx.queryParam("paused"));
        
        management.setPaused(paused);
        
        ctx.result("{\"status\": \"OK\"}");
        
    }

    public void switchIrrigator(
            final Context ctx) throws Exception {
        
        final int id = Integer.parseInt(ctx.pathParam("id"));
        final boolean off = Boolean.parseBoolean(ctx.queryParam("off"));
        
        try {
            if (off) {
                management.switchOff(id);
            } else {
                management.switchOn(id);
            }
            
            ctx.result("{\"status\": \"OK\"}");
        } catch (RuntimeException e) {
            logger.warn(e.getMessage(), e.getCause());
            ctx.result("{\"status\": \"" + e.getMessage() + "\"}");
        }
        
    }

}
