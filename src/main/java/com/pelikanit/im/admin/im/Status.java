package com.pelikanit.im.admin.im;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "status")
public class Status {

	private boolean stopped;
	
	private List<Irrigator> irrigators;
	
	public boolean isStopped() {
		return stopped;
	}

	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}

	public List<Irrigator> getIrrigators() {
		return irrigators;
	}
	
	public void setIrrigators(List<Irrigator> irrigators) {
		this.irrigators = irrigators;
	}
	
}
