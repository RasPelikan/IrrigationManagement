package com.pelikanit.im;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pelikanit.im.utils.ConfigurationUtils;

public class IrrigationManagementTest {

    private static Logger logger = LoggerFactory.getLogger(IrrigationManagementTest.class);

    @Test
    public void testSingleCycle() throws Exception {

        IrrigationManagement.clock = Clock.fixed(
                Instant.parse("2022-06-01T15:51:32Z"),
                ZoneOffset.UTC);

        final var props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("im-single-cycle.properties"));
        IrrigationManagement.config = new ConfigurationUtils(props);

        final var im = new IrrigationManagement();
        
        final var intervalTask = im.interval();
        
        for (int i = 0; i < 18; ++i) {
            
            logger.trace("Clock: {}", Instant.now(IrrigationManagement.clock));
            intervalTask.run();
            
            IrrigationManagement.clock = Clock.offset(
                    IrrigationManagement.clock,
                    Duration.ofSeconds(10));
            
        }
        
    }

    @Test
    public void testTwoCyclesWithSpace() throws Exception {

        IrrigationManagement.clock = Clock.fixed(
                Instant.parse("2022-06-01T15:51:32Z"),
                ZoneOffset.UTC);

        final var props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("im-two-cycles-with-space.properties"));
        IrrigationManagement.config = new ConfigurationUtils(props);

        final var im = new IrrigationManagement();
        
        final var intervalTask = im.interval();
        
        for (int i = 0; i < 24; ++i) {
            
            logger.trace("Clock: {}", Instant.now(IrrigationManagement.clock));
            intervalTask.run();
            
            IrrigationManagement.clock = Clock.offset(
                    IrrigationManagement.clock,
                    Duration.ofSeconds(10));
            
        }
        
    }

    @Test
    public void testTwoCyclesWithoutSpace() throws Exception {

        IrrigationManagement.clock = Clock.fixed(
                Instant.parse("2022-06-01T15:51:32Z"),
                ZoneOffset.UTC);

        final var props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("im-two-cycles-without-space.properties"));
        IrrigationManagement.config = new ConfigurationUtils(props);

        final var im = new IrrigationManagement();
        
        final var intervalTask = im.interval();
        
        for (int i = 0; i < 24; ++i) {
            
            logger.trace("Clock: {}", Instant.now(IrrigationManagement.clock));
            intervalTask.run();
            
            IrrigationManagement.clock = Clock.offset(
                    IrrigationManagement.clock,
                    Duration.ofSeconds(10));
            
        }
        
    }

    @Test
    public void testTwoCyclesWithoutSpaceMixed() throws Exception {

        IrrigationManagement.clock = Clock.fixed(
                Instant.parse("2022-06-01T15:51:32Z"),
                ZoneOffset.UTC);

        final var props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("im-two-cycles-without-space-mixed.properties"));
        IrrigationManagement.config = new ConfigurationUtils(props);

        final var im = new IrrigationManagement();
        
        final var intervalTask = im.interval();
        
        for (int i = 0; i < 24; ++i) {
            
            logger.trace("Clock: {}", Instant.now(IrrigationManagement.clock));
            intervalTask.run();
            
            IrrigationManagement.clock = Clock.offset(
                    IrrigationManagement.clock,
                    Duration.ofSeconds(10));
            
        }
        
    }

    @Test
    public void testTwoCyclesWithoutSpaceMixedThreeIrrigators() throws Exception {

        IrrigationManagement.clock = Clock.fixed(
                Instant.parse("2022-06-01T15:51:32Z"),
                ZoneOffset.UTC);

        final var props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("im-two-cycles-without-space-mixed-3.properties"));
        IrrigationManagement.config = new ConfigurationUtils(props);

        final var im = new IrrigationManagement();
        
        final var intervalTask = im.interval();
        
        for (int i = 0; i < 32; ++i) {
            
            logger.trace("Clock: {}", Instant.now(IrrigationManagement.clock));
            intervalTask.run();
            
            IrrigationManagement.clock = Clock.offset(
                    IrrigationManagement.clock,
                    Duration.ofSeconds(10));
            
        }
        
    }

}
