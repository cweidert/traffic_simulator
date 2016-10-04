package com.heliomug.job.traffic;

public class Ageable {
	private long birthday;
	private long lastUpdate;

	public Ageable() {
		birthday = lastUpdate = System.currentTimeMillis();
	}

	public double getAge() { 
		return (System.currentTimeMillis() - birthday) / 1000.0; 
	}

	public double getDt() {
		long steps = System.currentTimeMillis() - lastUpdate; 
		lastUpdate += steps;
		return steps / 1000.0;
	}
}
