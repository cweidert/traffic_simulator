package com.heliomug.job.traffic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.HashSet;

public class Intersection extends CircleSprite implements Runnable {
	public static final Color PAVEMENT_COLOR = Color.BLACK;//new Color(64, 64, 64);
	public static final Color FULL_COLOR = new Color(64, 64, 64);
	public static final Color DANGER_COLOR = Color.YELLOW;
	public static final double DEFAULT_RADIUS = 4;
	
	private static final int DANGER_LIMIT_TIME = 3000;
	
	private HashSet<Vehicle> currentlyIn;
	private long lastGrantedEntry;
	
	public Intersection(double x, double y) {
		this(new Point2D.Double(x, y));
		currentlyIn = new HashSet<Vehicle>();
		lastGrantedEntry = System.currentTimeMillis();
	}
	
	public Intersection(Point2D p) {
		super(p, DEFAULT_RADIUS);
	}
	
	public synchronized boolean isClear() {
		return currentlyIn.isEmpty();
	}
	
	public int getLastGrantedEntry() {
		return (int)(System.currentTimeMillis() - lastGrantedEntry);
	}
	
	// weird
	public synchronized Vehicle getADudeInWay() {
		for (Vehicle v : currentlyIn) {
			return v;
		}
		return null;
	}
	
	public synchronized boolean currentlyIn(Vehicle v) {
		return currentlyIn.contains(v);
	}
	
	public synchronized void takeIn(Vehicle v) {
	}
	
	public synchronized void grantEntry(Vehicle v) {
		if (v.isAlive()) {
			lastGrantedEntry = System.currentTimeMillis();
			currentlyIn.add(v);
		}
	}
	
	public synchronized void kickOut(Vehicle v) {
		currentlyIn.remove(v);
	}

	public void run() {
	}
	
	public void start(Map map) {
		Thread t = new Thread(this);
		t.start();
	}
	
	public void draw(Graphics2D g) {
		if (isClear()) {
			g.setColor(PAVEMENT_COLOR);
		} else if (getLastGrantedEntry() > DANGER_LIMIT_TIME) {
			g.setColor(DANGER_COLOR);
		} else {
			g.setColor(FULL_COLOR);
		}
		super.draw(g);
	}
}
