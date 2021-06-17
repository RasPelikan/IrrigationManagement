package com.pelikanit.im;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;

import com.pelikanit.im.model.Cycle;

public class IrrigatorManagementTest {

    @Test
    public void testEveryDayInterval() {

        int intervalOffset = 0;

        Cycle cycle = new Cycle();
        cycle.setInterval(1);

        Calendar calendar = Calendar.getInstance();

        boolean start1 = startIrrigation(calendar, cycle, intervalOffset);
        System.err.println(start1);

        calendar.add(Calendar.DATE, -1);
        boolean start2 = startIrrigation(calendar, cycle, intervalOffset);
        System.err.println(start2);

        Assert.assertTrue(start1);
        Assert.assertEquals(start1, start2);

    }

    @Test
    public void testTwoDayInterval() {

        int intervalOffset = 0;

        Cycle cycle = new Cycle();
        cycle.setInterval(2);

        Calendar calendar = Calendar.getInstance();

        boolean start1 = startIrrigation(calendar, cycle, intervalOffset);
        System.err.println(start1);

        calendar.add(Calendar.DATE, -1);
        boolean start2 = startIrrigation(calendar, cycle, intervalOffset);
        System.err.println(start2);

        Assert.assertNotEquals(start1, start2);

    }

    @Test
    public void testTwoDayIntervalWithOffset() {

        int intervalOffset = 1;

        Cycle cycle = new Cycle();
        cycle.setInterval(2);

        Calendar calendar = Calendar.getInstance();

        boolean start1 = startIrrigation(calendar, cycle, intervalOffset);
        System.err.println(start1);

        calendar.add(Calendar.DATE, -1);
        boolean start2 = startIrrigation(calendar, cycle, intervalOffset);
        System.err.println(start2);

        Assert.assertNotEquals(start1, start2);

    }

    public boolean startIrrigation(Calendar calendar, Cycle cycle, int intervalOffset) {

        LocalDateTime now = LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
        LocalDateTime reference = LocalDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneId.systemDefault());
        long days = ChronoUnit.DAYS.between(reference, now);
        return (days + intervalOffset) % cycle.getInterval() == 0;

    }

}
