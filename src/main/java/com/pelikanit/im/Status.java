package com.pelikanit.im;

public class Status {
	
	private float topSensorValue;
	
	private float middleSensorValue;
	
	private float bottomSensorValue;
	
	private float sensorHeight;
	
	private float bathWaterInLitre;
	
	private float bathWaterTemperature;
	
	private float numberOfBathesPossible;

	public Status(float topSensorValue, float middleSensorValue,
			float bottomSensorValue, float sensorHeight,
			float bathWaterInLitre, float bathWaterTemperature,
			float numberOfBathesPossible) {
		this.topSensorValue = topSensorValue;
		this.middleSensorValue = middleSensorValue;
		this.bottomSensorValue = bottomSensorValue;
		this.sensorHeight = sensorHeight;
		this.bathWaterInLitre = bathWaterInLitre;
		this.bathWaterTemperature = bathWaterTemperature;
		this.numberOfBathesPossible = numberOfBathesPossible;
	}

	public float getTopSensorValue() {
		return topSensorValue;
	}

	public float getMiddleSensorValue() {
		return middleSensorValue;
	}

	public float getBottomSensorValue() {
		return bottomSensorValue;
	}

	public float getSensorHeight() {
		return sensorHeight;
	}
	
	public float getBathWaterInLitre() {
		return bathWaterInLitre;
	}
	
	public float getBathWaterTemperature() {
		return bathWaterTemperature;
	}

	public float getNumberOfBathesPossible() {
		return numberOfBathesPossible;
	}
	
}
