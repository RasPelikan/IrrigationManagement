package com.pelikanit.im.model;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActiveCycle {
	
	private final static Logger logger = Logger.getLogger(ActiveCycle.class.getCanonicalName());

	private Cycle cycle;
	
	private Map<Integer, Integer> durations;
	
	private int currentIrrigatorIndex = -1;
	
	private Date startOfCurrentIrrigator = null;
	
    public ActiveCycle(final Cycle cycle, final int minutesPassed) {
	
		this.cycle = cycle;
        calculateDurations(minutesPassed);
		
	}
	
	private boolean isIrrigatorIntervalActive() {
		
		return startOfCurrentIrrigator != null;
		
	}
	
	private boolean activateNextIrrigatorInterval(final Date now) {
		
		currentIrrigatorIndex += 1;
		if (currentIrrigatorIndex == cycle.getIrrigators().size()) {
			return false;
		}
		
		startOfCurrentIrrigator = now;
		
		return true;
		
	}
	
	public ActiveCycle update(final Date now) {
		
		final HashSet<Irrigator> oldCycleIrrigator = new HashSet<Irrigator>();
		try {
			
			if (isIrrigatorIntervalActive()) {
				
				final boolean intervalEnded = isIrrigatorIntervalEnded(now);
				if (intervalEnded) {
					
					collectCycleIrrigatorsToBeTurnedOff(oldCycleIrrigator);
					setIrrigatorIntervalToInactive();
					
				} else {
					
					updateIrrigatorsOfIntervalToTurnedOn();
					
				}
				
			}
			
			if (!isIrrigatorIntervalActive()) {
			
				final boolean foundAnotherInterval = activateNextIrrigatorInterval(now);
				if (! foundAnotherInterval) {
					return null;
				}
				
				turnAdditionalIrrigatorsOfNewIntervalOn(oldCycleIrrigator);
				
			}
			
		} finally {
			
			turnOfIrrigatorsOfOldInterval(oldCycleIrrigator);
			
		}
		
		return this;
		
	}

	private void turnOfIrrigatorsOfOldInterval(
			final HashSet<Irrigator> oldCycleIrrigator) {
		
		for (final Irrigator toBeTurnedOff : oldCycleIrrigator) {

			logger.log(Level.WARNING, "Turn off irrigiator '"
					+ toBeTurnedOff.getId() + "'");
			try {
				toBeTurnedOff.off();
			} catch (Exception e) {
				logger.log(Level.WARNING, "Could not turn off irrigator '"
						+ toBeTurnedOff.getId() + "'", e);
			}
			
		}
		
	}
	
	private void updateIrrigatorsOfIntervalToTurnedOn() {
		
		turnAdditionalIrrigatorsOfNewIntervalOn(null);
		
	}

	private void turnAdditionalIrrigatorsOfNewIntervalOn(
			final HashSet<Irrigator> oldCycleIrrigator) {
		
		final List<Irrigator> irrigators = cycle.getIrrigators().get(currentIrrigatorIndex);
		for (final Irrigator irrigator : irrigators) {
			
			final boolean isAlreadyOn = (oldCycleIrrigator != null)
					&& oldCycleIrrigator.remove(irrigator);
			if (!isAlreadyOn) {
				logger.log(Level.WARNING, "Turn on irrigiator '"
						+ irrigator.getId() + "'");
				try {
					irrigator.on();
				} catch (Exception e) {
					logger.log(Level.WARNING, "Could not turn on irrigator '"
							+ irrigator.getId() + "'", e);
				}
			};
			
		}
		
	}

	private void setIrrigatorIntervalToInactive() {
		
		startOfCurrentIrrigator = null;
	
	}

	private void collectCycleIrrigatorsToBeTurnedOff(
			final HashSet<Irrigator> oldCycleIrrigator) {
		
		final List<Irrigator> irrigators = cycle.getIrrigators().get(currentIrrigatorIndex);
		for (final Irrigator irrigator : irrigators) {
			oldCycleIrrigator.add(irrigator);
		}
		
	}

	private boolean isIrrigatorIntervalEnded(final Date now) {
		
		final Integer duration = durations.get(currentIrrigatorIndex);
		final long currentDuration = (now.getTime() - startOfCurrentIrrigator.getTime())
				/ (1000 * 60);
		final boolean intervalEnded = currentDuration >= duration;
		
		return intervalEnded;
		
	}

    private void calculateDurations(int minutesPassed) {
		
		durations = new HashMap<Integer, Integer>();
		
		// calculate areas
		final List<List<Irrigator>> cycleIrrigators = cycle.getIrrigators();
		int totalArea = 0;
		for (int i = 0; i < cycleIrrigators.size(); ++i) {
			
			final List<Irrigator> irrigators = cycleIrrigators.get(i);
			
			int cycleArea = 0;
			
			for (final Irrigator irrigator : irrigators) {
				
				final int area = irrigator.getArea();
				totalArea += area;
				
				// adopt area according humanity sensor value
				// this will also effect the duration 
				final HumanitySensor humanitySensor = irrigator.getHumanitySensor();
				final int adjustedArea;
				if (humanitySensor == null) {
					adjustedArea = area;
				} else {
					adjustedArea = (int)((area) * humanitySensor.getValue());
				}
				
				cycleArea += adjustedArea;
				
			}
			
			durations.put(i, cycleArea);
			
		}
		
		for (int i = 0; i < cycleIrrigators.size(); ++i) {
			
			final int cycleArea = durations.get(i);
			
			// calculate duration according cycle length
			final int duration = (int)(((float) cycle.getDuration()) / totalArea * cycleArea);
			

			durations.put(i, duration);
					
		}
		
	}
	
}
