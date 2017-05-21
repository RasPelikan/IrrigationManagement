package com.pelikanit.im.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActiveCycle {

	private Cycle cycle;
	
	private Map<Integer, Integer> durations;
	
	private int currentIrrigatorIndex = -1;
	
	private Date startOfCurrentIrrigator = null;
	
	public ActiveCycle(final Cycle cycle) {
	
		this.cycle = cycle;
		calculateDurations();
		
	}
	
	private boolean isIrrigatorActive() {
		
		return startOfCurrentIrrigator != null;
		
	}
	
	public ActiveCycle update(final Date now) {
		
		if (isIrrigatorActive()) {
			
			final Integer duration = durations.get(currentIrrigatorIndex);
			final long currentDuration = (now.getTime() - startOfCurrentIrrigator.getTime())
					/ (1000 * 60);
			if (currentDuration >= duration) {
				
				final List<Irrigator> irrigators = cycle.getIrrigators().get(currentIrrigatorIndex);
				for (final Irrigator irrigator : irrigators) {
					irrigator.off();
				}
				
				startOfCurrentIrrigator = null;
				
			}
			
		}
		
		
		if (!isIrrigatorActive()) {
		
			currentIrrigatorIndex += 1;
			if (currentIrrigatorIndex == cycle.getIrrigators().size()) {
				return null;
			}
			
			startOfCurrentIrrigator = now;
			
			final List<Irrigator> irrigators = cycle.getIrrigators().get(currentIrrigatorIndex);
			for (final Irrigator irrigator : irrigators) {
				irrigator.on();
				System.err.println(durations.get(currentIrrigatorIndex));
			}
			
		}
		
		return this;
		
	}

	private void calculateDurations() {
		
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
					adjustedArea = (int)(((float) area) * humanitySensor.getValue());
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
