package com.pelikanit.im;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pelikanit.im.admin.Controller;
import com.pelikanit.im.admin.Irrigator;
import com.pelikanit.im.admin.Status;
import com.pelikanit.im.model.Cycle;
import com.pelikanit.im.model.Irrigator.Type;
import com.pelikanit.im.model.UrlBasedIrrigator;
import com.pelikanit.im.utils.ConfigurationUtils;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class IrrigationManagement {

    private static final Logger logger = LoggerFactory.getLogger(IrrigationManagement.class);

    static ConfigurationUtils config;

    static Clock clock;
    
    /**
     * Entry-point at startup.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {

        config = new ConfigurationUtils(args);

        IrrigationManagement.clock = Clock.systemDefaultZone();

        // build central instance
        IrrigationManagement main = new IrrigationManagement();
        try {

            main.start();

            // wait until shutdown
            do {

                // go asleep
                synchronized (shutdownMutex) {

                    try {
                        shutdownMutex.wait(1000); // keepAlive interval
                    } catch (InterruptedException e) {
                        // not excepted but OK
                    }

                }

            } while (!main.isShutdown());

        } catch (Throwable e) {

            logger.error("Error running application", e);

            Runtime.getRuntime().removeShutdownHook(shutdownHook);

        }
        // shutdown
        finally {

            // stop admin-httpserver
            main.stop();

        }

        // can be used to identify intentional shutdown
        logger.info("Shutdown return code: " + main.shutdown);

        System.exit(main.shutdown);

    }
    
    public static ConfigurationUtils getConfig() {

        return config;

    }

    public static Clock getClock() {

        return clock;

    }

    private static Thread shutdownHook;

    private static final Object shutdownMutex = new Object();

    private static LocalDate intervalStart;

    private static volatile boolean paused = false;

    private volatile int shutdown = -1;

    private static Set<Cycle> activeCycles = new HashSet<>();

    private Javalin httpServer;

    private Controller adminController;

    private Timer keepAliveTimer;
    
    private Timer intervalTimer;
    
    public IrrigationManagement() {

        intervalStart = getConfig().getIntervalStart();
        
        // capture "kill" commands and shutdown regularly
        shutdownHook = new Thread() {

            @Override
            public void run() {

                // shutdown
                shutdown(1);

            }

        };
        Runtime.getRuntime().addShutdownHook(shutdownHook);

    }
    
    public static boolean isIntervalActive(int interval) {
        
        return intervalStart
                .until(LocalDate.now(), ChronoUnit.DAYS)
                % interval
                == 0;
        
    }

    public void shutdown(int returnCode) {

        // wake-up main-method to shutdown all services immediately
        synchronized (shutdownMutex) {

            if (shutdown != -1) {
                return;
            }

            logger.info("Shutdown initiated");

            shutdown = returnCode;
            shutdownMutex.notify();

        }

    }
    
    public boolean isShutdown() {

        return shutdown != -1;

    }

    public void start() {

        startHttpServer();
        startKeepAliveInterval();
        startIntervalTimer();
        
    }
    
    private void stop() {
        
        stopIntervalTimer();
        stopKeepAliveInterval();
        stopHttpServer();
        
    }

    private void startHttpServer() {
        
        final var httpConfig = Javalin
                .create(config -> {
                    config.contextPath = "/im";
                    config.addStaticFiles("/htdocs", Location.CLASSPATH);
                });

        adminController = new Controller(this, httpConfig);

        httpServer = httpConfig.start(getConfig().getAdminPort());
        

    }

    private void stopHttpServer() {

        adminController.stop();
        httpServer.stop();

    }
    
    private void startKeepAliveInterval() {
        
        keepAliveTimer = new Timer("keepalive");
        
        keepAliveTimer.scheduleAtFixedRate(
                keepAlive(),
                getConfig().getKeepAliveSeconds() * 1000,
                getConfig().getKeepAliveSeconds() * 1000);
        
    }
    
    private void stopKeepAliveInterval() {

        keepAliveTimer.cancel();

    }

    private TimerTask keepAlive() {

        final var irrigators = getConfig().getIrrigators();

        return new TimerTask() {
            
            @Override
            public void run() {
                
                irrigators
                        .stream()
                        .filter(irrigator -> irrigator.getType() == Type.URL)
                        .forEach(irrigator -> ((UrlBasedIrrigator) irrigator).keepAlive());
                
            }
            
        };
        
    }

    private void startIntervalTimer() {

        intervalTimer = new Timer("interval");
        
        final var firstDelay = Duration.between(
                Instant.now(),
                Instant
                        .now()
                        .truncatedTo(ChronoUnit.MINUTES)
                        .plus(1, ChronoUnit.MINUTES)
                        .plusSeconds(2))
                .toMillis();
        
        intervalTimer.scheduleAtFixedRate(
                interval(),
                firstDelay,
                60000); // every minute
        
    }

    private void stopIntervalTimer() {

        intervalTimer.cancel();

    }

    public static void setPaused(boolean toBePaused) {

        if (paused == toBePaused) {
            return;
        }

        paused = toBePaused;

        activeCycles
                .stream()
                .flatMap(activeCycle -> activeCycle.getIrrigators().stream())
                .forEach(irrigator -> {
                    if (paused) {
                        irrigator.on();
                    } else {
                        irrigator.off();
                    }
                });

    }

    public static boolean isPaused() {

        return paused;

    }

    public Status getStatus() {

        final Status result = new Status();

        result.setIrrigators(
                getConfig()
                .getIrrigators()
                .stream()
                .map(irrigator -> new Irrigator(irrigator))
                .collect(Collectors.toList()));

        result.setPaused(isPaused());

        return result;

    }

    public void switchOff(final int irrigator) {
        
        getConfig()
                .getIrrigator(irrigator)
                .off();
        
    }
    
    public void switchOn(final int irrigator) {
        
        getConfig()
                .getIrrigator(irrigator)
                .on();
        
    }
    
    TimerTask interval() {
        
        final var cycles = getConfig().getCycles();

        return new TimerTask() {

            @Override
            public void run() {
                
                final var activeIrrigatorsBefore = activeCycles
                        .stream()
                        .flatMap(activeCycle -> activeCycle.getIrrigators().stream())
                        .collect(Collectors.toList());

                cycles.forEach(cycle -> {
                        final var active = cycle.isActive();
                        if (active && !activeCycles.contains(cycle)) {
                            logger.info("Activating cycle {}", cycle.getId());
                            activeCycles.add(cycle);
                        }
                        if (!active && activeCycles.contains(cycle)) {
                            logger.info("Deactivating cycle {}", cycle.getId());
                            activeCycles.remove(cycle);
                        }
                    });
                
                if (paused) {
                    logger.trace("Paused...");
                    return;
                }

                final var activeIrrigatorsAfter = activeCycles
                        .stream()
                        .flatMap(activeCycle -> activeCycle.getIrrigators().stream())
                        .collect(Collectors.toList());
                
                logger.trace("Before: {}", activeIrrigatorsBefore);
                logger.trace("After: {}", activeIrrigatorsAfter);
                activeIrrigatorsBefore
                        .stream()
                        .filter(Predicate.not(activeIrrigatorsAfter::contains))
                        .forEach(irrigator -> irrigator.off());

                activeIrrigatorsAfter
                        .stream()
                        .filter(Predicate.not(activeIrrigatorsBefore::contains))
                        .forEach(irrigator -> irrigator.on());

            }

        };

    }

}
