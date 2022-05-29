package com.pelikanit.im.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.pelikanit.im.IrrigationManagement;
import com.pelikanit.im.utils.ConfigurationUtils;

public class Cycle {

    private final int id;

    private final int start;

    private final int end;

    private final int interval;

    private List<Irrigator> irrigators;

    public Cycle(final int id, final ConfigurationUtils config) {

        this.id = id;

        this.start = config.getCycleStart(id);
        this.end = config.getCycleEnd(id);

        this.interval = config.getCycleInterval(id);
        if (interval < 0) {
            throw new RuntimeException(
                    "Cycle "
                    + id
                    + " interval must be an integer value greater than 0!");
        }
        
        final var irrigatorIds = config.getCycleIrrigators(id);
        if (irrigatorIds == null) {
            throw new RuntimeException(
                    "No irrigators configured for cycle "
                    + id);
        }
        this.irrigators = Arrays
                .stream(irrigatorIds)
                .mapToObj(irrigatorId -> config.getIrrigator(irrigatorId))
                .collect(Collectors.toList());

    }

    public boolean isActive() {
    
        final var now = LocalDateTime.now(IrrigationManagement.getClock());
        final var currentTime =
                now.get(ChronoField.HOUR_OF_DAY) * 100
                + now.get(ChronoField.MINUTE_OF_HOUR);
        
        // interval not covering midnight
        
        if (getEnd() > getStart()) {

            if ((currentTime < getStart())
                    || (currentTime >= getEnd())) {
                return false;
            }

            return IrrigationManagement.isIntervalActive(interval);

        }

        // interval covering midnight

        // period before midnight
        if (currentTime >= getStart()) {
            
            return IrrigationManagement.isIntervalActive(interval);
            
        }
        
        // period after midnight
        if (currentTime < getEnd()) {
            
            return IrrigationManagement.isIntervalActive(interval + 1);
            
        }
        
        return false;
        
    }
    
    @Override
    public String toString() {
        return "cycle " + id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Cycle)) {
            return false;
        }
        return id == ((Cycle) obj).getId();
    }

    public int getId() {
        return id;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public List<Irrigator> getIrrigators() {
        return irrigators;
    }

    public int getInterval() {
        return interval;
    }

}
