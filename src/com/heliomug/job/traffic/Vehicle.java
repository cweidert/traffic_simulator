package com.heliomug.job.traffic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import com.heliomug.utils.MiscUtils;

public class Vehicle extends CircleSprite implements Runnable {
	public static final int COUNTER_CLOCKWISE = 1;
	public static final int STRAIGHT = 0;
	public static final int CLOCKWISE = -1;

	private static final boolean DEFAULT_SHOW_TRUE_COLOR = true;
	private static final boolean DEFAULT_SHOW_TURRET = false;
	private static final boolean DEFAULT_SHOW_TRAIL = false;

	private static final int SLEEP_LENGTH = 10;

	private static final int LOOK_AHEAD_LENGTHS = 6;
	private static final double MAX_SPEED_LENGTHS = 10;
	private static final double ZERO_SPEED_LENGTHS = 2;

	private static final double ANGULAR_SPEED = 4 * Math.PI;
	private static final double MAX_SPEED = 5;
	private static final double MIN_SPEED_TO_TURN = 1;
	private static final double ACCEL = 30;
	
	private static final double DEFAULT_RADIUS = .25;

	private static final double TURRET_LENGTH = DEFAULT_RADIUS * 2;
	private static final Color TURRET_COLOR = Color.WHITE;
	private static final Color BRAKE_COLOR = Color.RED;
	private static final Color FREE_COLOR = Color.GREEN;
	private static final Color FROZEN_COLOR = Color.BLUE;
	private static final double COLOR_CYCLE_LENGTH = 2.0;
	
	private static int vehiclesEverBorn = 0;
	private static int vehiclesReachedGoal = 0;
	
	private int number;
	
	public boolean logging;
	
	private Color color;
	private boolean highlighted;
	
	private Route route;
	private Point2D endGoal;
	private Point2D nextGoal;
	private Point2D prevGoal;
	private Point2D lastGreenLight;
	
	private double heading;
	private double speed;
	
	private long lastTimeMoved;
	
	private boolean completedMission;
	private boolean isAlive;
	
	private QuadTree<Vehicle> vehicleTree;
	private Map map;
	
	private Intersection currentIntersection;
	private boolean isFrozen;

	public static int getVehiclesEverBorn() { return vehiclesEverBorn; }
	public static int getVehiclesReachedGoal() { return vehiclesReachedGoal; }

	private Vehicle() {
		super(null, DEFAULT_RADIUS);
		vehiclesEverBorn++;
		number = vehiclesEverBorn;
		speed = heading = 0;
		isAlive = true;
		completedMission = false;
		color = MiscUtils.getRandomColor(); 
		lastTimeMoved = System.currentTimeMillis();
		vehicleTree = null;
		isFrozen = false;
		lastGreenLight = null;
		prevGoal = null;
	}
	
	public Vehicle(Map map, QuadTree<Vehicle> quadTree, Point2D start, Point2D finish) {
		this();
		this.setLocation(start);
		this.speed = 0;
		this.heading = 0;
		this.endGoal = finish;
		this.heading = MiscUtils.heading(start, finish);
		this.vehicleTree = quadTree;
		this.map = map;
		setRoute(start, finish);
	}

	public synchronized boolean isDead() { return !this.isAlive; }
	public synchronized boolean isAlive() { return this.isAlive; }
	public int getNumber() { return number; }
	public int getTimeSinceMoved() { return (int)(System.currentTimeMillis() - lastTimeMoved); }
	public boolean completedMission() { return this.completedMission; }
	public Point2D getNextGoal() { return this.nextGoal; }
	public Point2D getPrevGoal() { return this.prevGoal; }
	public Point2D getLastGreenLight() { return this.lastGreenLight; }
	private boolean isFrozen() { return this.isFrozen; }
	public void setHighlighted(boolean b) { this.highlighted = b; } 

	public void setLastGreenLight(Point2D p) { this.lastGreenLight = p; }
	
	public void setRoute(Point2D start, Point2D finish) {
		this.route = map.getRoute(start, finish);
		completedMission = false;
	}
	
	public void setNewGoals() {
		Point2D dest = map.getRandomIntersection().getLocation();
		setRoute(getLocation(), dest);
	}

	public void freeze(Intersection intersection) {
		this.isFrozen = true;
		//this.currentIntersection = intersection;
	}
	
	public void unfreeze() {
		this.isFrozen = false;
	}
	
	private void enterIntersection(Intersection intersection) {
		if (currentIntersection == null && isAlive) {
			intersection.takeIn(this);
			getDt();
			currentIntersection = intersection;
		}
	}
	
	private void leaveIntersection() {
		if (currentIntersection != null) {
			currentIntersection.kickOut(this);
			currentIntersection = null;
		}
	}
	
	
	private double distanceTo(Vehicle other) {
		return getLocation().distance(other.getLocation());
	}

	/*
	private double getRelativeSpeed(Vehicle other) {
		double theta = other.heading - this.heading;
		return other.speed * Math.cos(theta) - speed;
	}
	*/
	
	
	private Rectangle2D aheadBounds(double lengths) {
		Point2D trans = MiscUtils.polar(lengths * getRadius() * 2, heading);
		return MiscUtils.translate(getBounds(), trans);
	}
	
	
	private Vehicle getClosestAhead() {
		List<Vehicle> li = new ArrayList<Vehicle>();
		for (int i = 0 ; i < LOOK_AHEAD_LENGTHS ; i++) {
			li.addAll(vehicleTree.getOverlappers(aheadBounds(i)));
		}
		Vehicle minV = null;
		double minDist = Double.POSITIVE_INFINITY;
		for (Vehicle v : li) {
			double dist = distanceTo(v); 
			if (v != this && dist < minDist) {
				minDist = distanceTo(v);
				minV = v;
			}
		}
		return minV;
	}
	
	private Intersection getIntersectionOn() {
		List<Intersection> li = map.getIntersectionTree().getOverlappers(this);
		for (Intersection i : li) {
			if (i.intersects(this)) {
				return i;
			}
		}
		return null;
	}
	

	
	private void move(double dt) {
		if (!isFrozen) {
			lastTimeMoved = System.currentTimeMillis();
			translate(Math.cos(heading) * speed * dt, Math.sin(heading) * speed * dt);
		}
	}

	private void moderateSpeed(double dt) {
		Vehicle v = getClosestAhead();
		if (v != null) {
			double lengths = distanceTo(v) / 2 / getRadius();
			speed = Math.max(0, MAX_SPEED * (lengths - ZERO_SPEED_LENGTHS) / (MAX_SPEED_LENGTHS - ZERO_SPEED_LENGTHS));
		} else {
			speed = Math.min(speed + dt * ACCEL, MAX_SPEED);
		}
			
	}
	
	private void handleIntersections() {
		Intersection i = getIntersectionOn();
		if (currentIntersection == null) {
			if (i != null) {
				enterIntersection(i);
			}
		} else {
			if (currentIntersection != i) {
				leaveIntersection();
			}
		}
	}
	
	private void turn(double dt, int direction, double maxTurn) {
		if (direction == COUNTER_CLOCKWISE || direction == CLOCKWISE) {
			double turnAmount;
			if (maxTurn < 0) {
				turnAmount = Math.max(direction * dt * ANGULAR_SPEED, maxTurn);
			} else {
				turnAmount = Math.min(direction * dt * ANGULAR_SPEED, maxTurn);
			}
			heading += turnAmount;
			//if (Math.abs(maxTurn) > .1) System.out.println(String.format("%.2f, %d", maxTurn, direction));
			if (logging) System.out.println(String.format("\t\t\t\t\t\t\tact  %.2f", turnAmount / Math.PI));
		}
	}
	
	private void setGoals(double dt) {
		if (route.isEmpty()) {
			reachGoal();
		} else {
			nextGoal = route.peek();
			if (nextGoal != null) {

				if (!isFrozen && speed > MIN_SPEED_TO_TURN) {
					turnTowards(nextGoal, dt);
				}
				
				List<Intersection> ints = map.getIntersectionTree().getOverlappers(this);
				if (this.contains(nextGoal) || (ints.size() > 0 && ints.get(0).getLocation().equals(nextGoal) && ints.get(0).contains(this))) { 
					prevGoal = route.pop();
				}
			}
		} 
	}

	private void turnTowards(Point2D p, double dt) {
		double toGoal = MiscUtils.heading(getLocation(), p);
		double diff = MiscUtils.angleDiff(toGoal, heading);
		if (logging) System.out.println(String.format("head %.2f \tgoal %.2f \tdiff %.2f", heading / Math.PI, toGoal / Math.PI, diff / Math.PI));
		if (diff > 0) {
			turn(dt, COUNTER_CLOCKWISE, diff);
		} else if (diff < 0) {
			turn(dt, CLOCKWISE, diff);
		}
	}
	
	private void reachGoal() {
		vehiclesReachedGoal++;
		completedMission = true;
	}
	
	public synchronized void die() {
		this.isAlive = false;
		leaveIntersection();
	}
	
	public void step(double dt) {
		setGoals(dt);
		handleIntersections();
		moderateSpeed(dt);
		move(dt);
		if (this.contains(endGoal)) {
			reachGoal();
		}	
	}
	
	public void run() {
		while (isAlive) {
			double dt = getDt();
			step(dt);
			MiscUtils.sleep(SLEEP_LENGTH);
		}
	}
	
	
	public Color getColor(boolean showTrueColor) {
		if (highlighted) {
			return MiscUtils.getCycleColor(COLOR_CYCLE_LENGTH);
		} else if (showTrueColor) {
			return color;
		} else {
			if (isFrozen()) {
				return FROZEN_COLOR;
			} else if (getClosestAhead() != null) {
				return BRAKE_COLOR;
			} else {
				return FREE_COLOR;
			}
		}
	}

	public void draw(Graphics2D g) {
		draw(g, DEFAULT_SHOW_TRUE_COLOR, DEFAULT_SHOW_TURRET, DEFAULT_SHOW_TRAIL);
	}
	
	public void draw(Graphics2D g, boolean showTrueColor, boolean showTurret, boolean showTrail) {
		if (showTurret) {
			g.setStroke(TrafficPanel.STANDARD_STROKE); 
			g.setColor(TURRET_COLOR);
			double tx = Math.cos(heading) * TURRET_LENGTH;
			double ty = Math.sin(heading) * TURRET_LENGTH;
			Point2D turretEnd = MiscUtils.translate(getLocation(), tx, ty);
			g.draw(new Line2D.Double(getLocation(), turretEnd));
			MiscUtils.drawArrow(g, turretEnd, heading, getRadius());
		}
		if (showTrail) {
			g.setColor(Color.GREEN);
			if (lastGreenLight != null) {
				g.draw(new Line2D.Double(getLocation(), lastGreenLight));
			}
		}
		g.setColor(getColor(showTrueColor));
		super.draw(g);
	}

	public void drawRoute(Graphics2D g, boolean showTrueColor) {
		Color c = getColor(showTrueColor);
		this.route.draw(g, getLocation(), c);
	}
}
