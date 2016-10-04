package com.heliomug.job.traffic;

import java.awt.Color;
import java.awt.Graphics2D;

public class IntersectionAllWay extends Intersection {
	private static final int SLEEP_TIME = 10;
	private static final Color FREE_COLOR = Color.BLACK; //new Color(63, 63, 63);
	private static final Color OCCUPIED_COLOR = Color.BLACK; //new Color(127, 0, 0);

	private VehicleQueue q;

	public IntersectionAllWay(double x, double y) {
		super(x, y);
		q = new VehicleQueue();
	}

	public void takeIn(Vehicle v) {
		if (!currentlyIn(v)) {
			q.enqueue(v);
			v.freeze(this);
		}
		super.takeIn(v);
	}

	public void grantEntry(Vehicle v) {
		v.unfreeze();
		super.grantEntry(v);
	}
	
	public void kickOut(Vehicle v) {
		super.kickOut(v);
	}

	public void run() {
		while (true) {
			if (isClear() && !q.isEmpty()) {
				Vehicle v = q.dequeue();
				grantEntry(v);
			}
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				System.err.println("Intersection interrupted somehow!");
				e.printStackTrace();
			}
		}
	}

	public void start() {
		Thread t = new Thread(this);
		t.start();
	}

	public void draw(Graphics2D g) {
		if (isClear()) {
			g.setColor(FREE_COLOR);
		} else {
			g.setColor(OCCUPIED_COLOR);
		}
		super.draw(g);
	}

}
