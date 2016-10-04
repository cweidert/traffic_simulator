package com.heliomug.job.traffic;

import java.awt.geom.Point2D;

public class Light extends CircleSprite {
	private static final double DEFAULT_LIGHT_RADIUS = .4;  
	
	public static final int GREEN = 2;
	public static final int YELLOW = 1;
	public static final int RED = 0;
	
	private int state;
			
	public Light(Point2D p) {
		super(p, DEFAULT_LIGHT_RADIUS);
		this.state = RED;
	}

	public void changeState(int state) {
		this.state = state;
	}
	
	public boolean isGreen() { return state == GREEN; }
	public boolean isYellow() { return state == YELLOW; }
	public boolean isRed() { return state == RED; }
}
