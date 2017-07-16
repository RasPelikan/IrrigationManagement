package com.pelikanit.im.admin.im;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "status")
public class Status {

	private boolean paused;
	
	private List<Irrigator> irrigators;

	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	public boolean isPaused() {
		return paused;
	}
	
	public List<Irrigator> getIrrigators() {
		return irrigators;
	}
	
	public void setIrrigators(List<Irrigator> irrigators) {
		this.irrigators = irrigators;
	}
	
}
