package com.heliomug.job.traffic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.heliomug.utils.MiscUtils;

public class IntersectionLit extends Intersection implements Runnable {
	private static final int SLEEP_TIME = 10;
	public static final int DEFAULT_LIGHT_DURATION = 10000;
	private static final int LAST_SINCE_CAR_WAIT = 2000;
	private static final int GAP_WAIT = 500;
	private static final int YELLOW_WAIT = 500;
	private static final double LIGHT_RADIUS = .4;
	
	private List<Point2D> lights;
	private VehicleQueue[] queues;
	private int laneTurn;
	private int lightDuration; 
	private long lastLetVehicleThrough;
	private long lastSwitched;
	private boolean resting; 

	public IntersectionLit(double x, double y) {
		this(x, y, DEFAULT_LIGHT_DURATION);
	}
	
	public IntersectionLit(double x, double y, int lightDuration) {
		super(x, y);
		laneTurn = 0;
		this.lightDuration = lightDuration;
		lights = null;
		queues = null;
		lastSwitched = 0;
		resting = true;
		lastLetVehicleThrough = 0;
	}

	public void setLightDuration(int dur) { lightDuration = dur; }
	
	public void setLights(List<Lane> li) {
		lights = new ArrayList<Point2D>();
		lights.add(null);
		lights.add(getLocation());
		for (Lane el : li) {
			if (this.contains(el.getFinish())) {
				lights.add(el.getFinish());
			}
		}
		queues = new VehicleQueue[lights.size()];
		for (int i = 0 ; i < queues.length ; i++) {
			queues[i] = new VehicleQueue();
		}
	}
	
	private Point2D getGreenGoal() {
		return lights.get(laneTurn % lights.size());
	}
	
	private boolean equalsGreenGoal(Point2D p) {
		Point2D greenGoal = getGreenGoal();
		if (greenGoal == null) {
			return p == null;
		} else {
			return getGreenGoal().equals(p);
		}
	}
	
	public VehicleQueue getGreenQueue() { 
		return queues[laneTurn % queues.length];
	}
	
	public boolean hasCarsWaiting() {
		for (int i = 0 ; i < queues.length ; i++) {
			MiscUtils.sleep(1);
			if (!queues[i].isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	public void takeIn(Vehicle v) {
		Point2D nextGoal = v.getNextGoal();
		int goalIndex = lights.indexOf(nextGoal);
		if (goalIndex == -1) goalIndex = 0;
		queues[goalIndex].enqueue(v);
		super.takeIn(v);
		v.freeze(this);
	}

	public void grantEntry(Vehicle v) {
		v.setLastGreenLight(getGreenGoal());
		v.unfreeze();
		lastLetVehicleThrough = System.currentTimeMillis();
		super.grantEntry(v);
	}
	
	public void kickOut(Vehicle v) {
		super.kickOut(v);
	}
	
	
	
	public boolean isYellow() {
		boolean naturalYellow = (sinceSwitched() > lightDuration - GAP_WAIT - YELLOW_WAIT);
		// boolean noCarYellow = (sinceLetVehicleThrough() > LAST_SINCE_CAR_WAIT - YELLOW_WAIT);
		return naturalYellow; // || noCarYellow;
	}
	
	public boolean isGap() {
		boolean naturalGap = (sinceSwitched() > lightDuration - GAP_WAIT);
		boolean noCarGap = (sinceLetVehicleThrough() > LAST_SINCE_CAR_WAIT);
		return naturalGap || noCarGap;
	}
	
	public int sinceSwitched() {
		return (int)(System.currentTimeMillis() - lastSwitched);
	}
	
	public int sinceLetVehicleThrough() {
		return (int)(System.currentTimeMillis() - lastLetVehicleThrough);
	}
	

	public Color greenColor() {
		if (isGap()) {
			return Color.RED;
		} else if (isYellow()) {
			return Color.YELLOW;
		} else {
			return Color.GREEN;
		}
	}
	
	public void draw(Graphics2D g) {
		super.draw(g);
		double r = LIGHT_RADIUS;
		g.setColor(Color.RED);
		for (Point2D p : lights) {
			if (p != null && p != getLocation()) {
				g.fill(new Ellipse2D.Double(p.getX() - r, p.getY() - r, r * 2, r * 2)); 
			}
		}
		Point2D p = getGreenGoal();
		g.setColor(greenColor());
		if (resting) {
			r = getRadius();
			g.setColor(Intersection.PAVEMENT_COLOR);
			g.fill(new Ellipse2D.Double(getX() - r, getY() - r, r * 2, r * 2));
		} else {
			if (p == getLocation()) {
				r = getRadius();
				g.setStroke(TrafficPanel.STANDARD_STROKE);
				g.draw(new Ellipse2D.Double(getX() - r, getY() - r, r * 2, r * 2));
			} else if (p != null) {
				g.fill(new Ellipse2D.Double(p.getX() - r, p.getY() - r, r * 2, r * 2));
			}
		}
	}

	
	public void start(Map map) {
		setLights(map.getLanes());
		Thread t = new Thread(this);
		t.start();
		lastLetVehicleThrough = lastSwitched = System.currentTimeMillis();
		
	}
	
	private void advanceLane() {
		resting = !hasCarsWaiting();
		if (resting) {
			laneTurn = 0;
		} else {
			lastLetVehicleThrough = lastSwitched = System.currentTimeMillis();
			laneTurn++;
			while (getGreenQueue().isEmpty()) {
				laneTurn++;
			}
		}
	}
	
	public void run() {
		while (true) {
			if (resting) {
				advanceLane();
			} else {
				if (!isGap() && !getGreenQueue().isEmpty()) {
					if (isClear()) {
						grantEntry(getGreenQueue().dequeue());
					} else {
						if (equalsGreenGoal(getADudeInWay().getLastGreenLight())) {
							grantEntry(getGreenQueue().dequeue());
						}
					}
				}
				if (sinceSwitched() > lightDuration || sinceLetVehicleThrough() > LAST_SINCE_CAR_WAIT + GAP_WAIT) {
					advanceLane();
				}
			}
			MiscUtils.sleep(SLEEP_TIME);
		}
	}
}
