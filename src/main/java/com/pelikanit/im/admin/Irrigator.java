package com.pelikanit.im.admin;

public class Irrigator {

	private int id;
	
	private boolean on;

	private String type;

	public Irrigator(
	        final com.pelikanit.im.model.Irrigator original) {
	    
	    id = original.getId();
	    on = original.isOn();
	    type = original.getType().toString();
	    
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
