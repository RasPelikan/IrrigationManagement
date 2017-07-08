package com.pelikanit.im.admin.im;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "status")
public class Status {

	private List<Irrigator> irrigators;
	
	public List<Irrigator> getIrrigators() {
		return irrigators;
	}
	
	public void setIrrigators(List<Irrigator> irrigators) {
		this.irrigators = irrigators;
	}
	
}
