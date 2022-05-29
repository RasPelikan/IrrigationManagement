package com.pelikanit.im.model;

public abstract class Irrigator {

    public static enum Type {
        URL, GPIO, LOG
    };
	
	private int id;
	
	private Type type;
	
	private boolean on;
	
    protected abstract void switchOn();
	
    protected abstract void switchOff();

    public Irrigator(
            final int id,
            final Type type) {
        
        this.id = id;
        this.type = type;

    }
    
    public void on() {
		
		if (isOn()) {
			return;
		}
		// mark as switched on before doing the switch-on
		// why? see off()
		on = true;
		switchOn();
		
	}
	
    public void off() {
		
		if (!isOn()) {
			return;
		}
		// mark as switched off before doing the switch-off
		// is for safety reasons: if switching off fails
		// the next keep-alive
		on = false;
		switchOff();
		
	}
	
	public void keepAlive() throws Exception {

//	    if (isOn()) {
//			switchOn();
//		} else {
//			switchOff();
//		}
		
	}

	public boolean isOn() {
		return on;
	}

	@Override
	public String toString() {
        return "irrigator " + id + " is " + (on ? "on" : "off");
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
		if (!(obj instanceof Irrigator)) {
			return false;
		}
		return id == ((Irrigator) obj).getId();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
