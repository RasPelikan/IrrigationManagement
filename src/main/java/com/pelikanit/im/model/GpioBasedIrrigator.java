package com.pelikanit.im.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pelikanit.im.utils.ConfigurationUtils;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.plugin.mock.platform.MockPlatform;
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalOutputProvider;
import com.pi4j.plugin.pigpio.provider.gpio.digital.PiGpioDigitalOutputProvider;

public class GpioBasedIrrigator extends Irrigator {

    private static final Logger logger = LoggerFactory.getLogger(GpioBasedIrrigator.class);

    private static Context pi4j = null;
    
    private static String gpioProvider = null;

    private DigitalOutput gpio;

    private boolean inverse;

    public GpioBasedIrrigator(
            final int id,
            final ConfigurationUtils config) {

        super(id, Type.GPIO);

        if (pi4j == null) {
            
            if (config.isTestEnvironment()) {
                pi4j = Pi4J
                        .newContextBuilder()
                        .add(new MockPlatform() {
                            @Override
                            protected String[] getProviders() {
                                return new String[] { MockDigitalOutputProvider.ID };
                            }
                        })
                        .add(MockDigitalOutputProvider.newInstance())
                        .build();
                gpioProvider = MockDigitalOutputProvider.ID;
            } else {
                pi4j = Pi4J.newAutoContext();
                gpioProvider = PiGpioDigitalOutputProvider.ID;
            }
            
            final var shutdownHook = new Thread() {
                @Override
                public void run() {
                    logger.info("Shutdown Pi4J");
                    pi4j.shutdown();
                }

            };
            Runtime.getRuntime().addShutdownHook(shutdownHook);

        }
        
        inverse = config.isIrrigatorGpioInverse(id);
        final var gpioNo = config.getIrrigatorGpio(id);
        
        gpio = DigitalOutput
                .newBuilder(pi4j)
                .id("irrigator-" + id)
                .name("Irrigator " + id)
                .address(gpioNo)
                .shutdown(offState())
                .initial(offState())
                .provider(gpioProvider)
                .build();
        
    }

    private DigitalState offState() {

        return inverse ? DigitalState.HIGH : DigitalState.LOW;

    }
    
    @Override
    protected void switchOff() {

        if (inverse) {
            gpio.on();
        } else {
            gpio.off();
        }

    }

    @Override
    protected void switchOn() {

        if (inverse) {
            gpio.off();
        } else {
            gpio.on();
        }

    }
    
}
