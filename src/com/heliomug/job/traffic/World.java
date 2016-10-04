package com.heliomug.job.traffic;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class World extends Ageable implements Runnable {
	
	private static final int SLEEP_TIME = 40;
	private static final int DEFAULT_SPAWN_DELAY = 400;
	private static final int DEFAULT_MAX_VEHICLES = 10; 
	private static final boolean DEFAULT_SHOW_TREE = false;
	private static final boolean DEFAULT_SHOW_ROUTES = false;
	private static final boolean DEFAULT_SHOW_TURRETS = false;
	private static final boolean DEFAULT_SHOW_TRUE_COLORS = true;
	private static final boolean DEFAULT_SHOW_TRAILS = false;
	
	private static final int SPAWN_DIST = 10;
	
	
	private long lastSpawn;
	
	private List<Vehicle> vehicles;
	private QuadTree<Vehicle> vehicleTree; 
	
	private Map map;
	
	private int spawnDelay;
	private int maxVehicles;
	
	public World() {
		reset();
	}

	public void reset() {
		map = new Map();
		vehicles = new ArrayList<Vehicle>();
		vehicleTree = new QuadTree<Vehicle>();
		lastSpawn = System.currentTimeMillis();
		spawnDelay = DEFAULT_SPAWN_DELAY;
		maxVehicles = DEFAULT_MAX_VEHICLES;
	}
	
	public int getMaxVehicles() { return maxVehicles; }
	public int getVehiclesPerSecond() { return 1000 / spawnDelay; }
	public int getNumberOfVehicles() { return vehicles.size(); }
	public void setMaxVehicles(int n) { maxVehicles = n; }
	public void setSpawnDelay(int t) { 
		spawnDelay = t;
		lastSpawn = System.currentTimeMillis();
	}

	public Rectangle2D getMapBounds() {	return map.getBounds(); } 

	public void setLightDuration(int dur) { map.setLightDuration(dur); }
	
	public synchronized void updateVehicleTree() {
		vehicleTree.refill(vehicles);
	}
	
	private void step(double dt) {
		updateVehicleTree();
		maybeSpawn();
		dealWithFinishedVehicles();
	}

	private void maybeSpawn() {
		if (System.currentTimeMillis() - lastSpawn > spawnDelay) {
			if (vehicles.size() < maxVehicles) {
				Rectangle2D bounds = getBounds();
				double x = ((int)(Math.random() * 2) * 2 - 1) * (bounds.getWidth() / 2 + SPAWN_DIST); 
				double y = ((int)(Math.random() * 2) * 2 - 1) * (bounds.getHeight() / 2 + SPAWN_DIST);
				x += bounds.getX() + bounds.getWidth() / 2;
				y += bounds.getY() + bounds.getHeight() / 2;
				Point2D p = new Point2D.Double(x, y);
				if (vehicleTree.getOverlappers(p).size() == 0) {
					spawnCarAt(p.getX(), p.getY());
					lastSpawn += spawnDelay;
				}
			}
		}
	}
	
	/*
	private synchronized void spawnRandomCar() {
			Intersection start = map.getRandomIntersection();
			Intersection finish = map.getRandomIntersection();
			Point2D s = new Point2D.Double(start.getX() - 18, start.getY() + 10);
			Point2D f = new Point2D.Double(finish.getX(), finish.getY());
			Vehicle v = new Vehicle(map, vehicleTree, s, f);
			if (vehicleTree.getOverlappers(v).size() == 0) {
				addVehicle(v);
			}
	}
	*/
	
	public synchronized void spawnCarAt(double x, double y) {
		Point2D finish = map.getRandomIntersection().getLocation();
		Point2D s = new Point2D.Double(x, y);
		Vehicle v = new Vehicle(map, vehicleTree, s, finish);
		if (vehicleTree.getOverlappers(v).size() == 0) {
			addVehicle(v);
		}
	}
	
	public void start() {
		Thread t = new Thread(this);
		t.start();
		map.start();
	}
	
	public void run() {
		while (true) {
			step(getDt());
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				System.err.println("Painting OVERRRR (Interrupted)");
				e.printStackTrace();
			}
		}
	}

	public Rectangle2D getBounds() {
		return map.getBounds();
	}
	
	private synchronized void dealWithFinishedVehicles() {
		for (Vehicle v : vehicles) {
			if (v.completedMission()) {
				if (getNumberOfVehicles() <= maxVehicles) {
					v.setNewGoals();
				} else {
					v.die();
				}
			}
		}
		for (int i = 0 ; i < vehicles.size() ; i++) {
			Vehicle v = vehicles.get(i);
			if (v.isDead()) {
				vehicles.remove(v);
				i--;
			}
		}
	}
	
	public synchronized void draw(Graphics2D g) {
		draw(g, DEFAULT_SHOW_TREE, DEFAULT_SHOW_TRUE_COLORS, DEFAULT_SHOW_ROUTES, DEFAULT_SHOW_TURRETS, DEFAULT_SHOW_TRAILS);
	}	
	
	public synchronized void draw(Graphics2D g, boolean showTree, boolean showTrueColors, boolean showRoutes, boolean showTurrets, boolean showTrails) {
		map.draw(g);
		if (showTree) {
			vehicleTree.draw(g);
		}
		if (showRoutes) {
			for (Vehicle v : vehicles) {
				v.drawRoute(g, showTrueColors);
			}
		}
		for (Vehicle v : vehicles) {
			v.draw(g, showTrueColors, showTurrets, showTrails);
		}
	}

	public synchronized void addVehicle(Vehicle v) {
		vehicles.add(v);
		Thread t = new Thread(v);
		t.start();
		updateVehicleTree();
	}
	
	public synchronized Vehicle getVehicleAt(double x, double y) {
		List<Vehicle> vehicles = vehicleTree.getOverlappers(x, y);
		if (vehicles.size() > 0) {
			return vehicles.get(0);
		}
		return null;
	}
	
	public synchronized void killVehicleAt(double x, double y) {
		Vehicle v = getVehicleAt(x, y);
		if (v != null) v.die();
	}
	
	public synchronized void logAt(double x, double y) {
		Vehicle v = getVehicleAt(x, y);
		if (v != null) v.logging = true;
	}
	
	public static World smallWorld() {
		World m = new World();
		Intersection one = new IntersectionLit(0, 0);
		Intersection two = new IntersectionLit(30, 0);
		Intersection three = new IntersectionLit(60, 0);
		m.map.addIntersection(one);
		m.map.addIntersection(two);
		m.map.addIntersection(three);
		m.map.addTwoWayStreet(one, two, 1);
		m.map.addTwoWayStreet(three, two, 1);
		
		Vehicle v = new Vehicle(m.map, m.vehicleTree, new Point2D.Double(10, 10), new Point2D.Double(30, 30));
		m.addVehicle(v);
		m.setSpawnDelay(Integer.MAX_VALUE);
		
		return m;
	}
	
	public static World circleWorld(int steps, double r) {
		World w = new World();
		for (int i = 0 ; i < steps ; i++) {
			double theta = Math.PI * 2 * i / steps;
			w.map.addIntersection(new Intersection(r * Math.cos(theta), r * Math.sin(theta)));
		}
		for (int i = 0 ; i < steps ; i++) {
			double theta = Math.PI * 2 * i / steps;
			double theta2 = Math.PI * 2 * (i + 1) / steps;
			Intersection one = w.map.getClosestIntersection(new Point2D.Double(r * Math.cos(theta), r * Math.sin(theta)));
			Intersection two = w.map.getClosestIntersection(new Point2D.Double(r * Math.cos(theta2), r * Math.sin(theta2)));
			w.map.addTwoWayStreet(one, two, Lane.DEFAULT_LANE_WIDTH / 2);
		}
		return w;
	}
	
	public static World defaultWorld() {
		int grid = 4;
		double spacing = 40;
		
		World m = new World();
		
		for (int i = 0 ; i < grid ; i++) {
			for (int j = 0 ; j < grid ; j++) {
				m.map.addIntersection(new IntersectionLit(i * spacing, j * spacing));
			}
		}

		for (int i = 0 ; i < grid ; i++) {
			for (int j = 0 ; j < grid - 1 ; j++) {
				Intersection start = m.map.getClosestIntersection(new Point2D.Double(i * spacing, j * spacing));
				Intersection finish = m.map.getClosestIntersection(new Point2D.Double(i * spacing, (j + 1) * spacing));
				m.map.addTwoWayStreet(start, finish, Lane.DEFAULT_LANE_WIDTH / 2);
			}
		}
		
		for (int i = 0 ; i < grid - 1; i++) {
			for (int j = 0 ; j < grid ; j++) {
				Intersection start = m.map.getClosestIntersection(new Point2D.Double(i * spacing, j * spacing));
				Intersection finish = m.map.getClosestIntersection(new Point2D.Double((i + 1) * spacing, j * spacing));
				m.map.addTwoWayStreet(start, finish, Lane.DEFAULT_LANE_WIDTH / 2);
			}
		}
		
		
		for (int i = 0 ; i < grid - 1; i++) {
			for (int j = 0 ; j < grid - 1; j++) {
				Intersection start;
				Intersection finish;
				if ((i + j) % 2 == 0) {
					start = m.map.getClosestIntersection(new Point2D.Double((i + 1) * spacing, j * spacing));
					finish = m.map.getClosestIntersection(new Point2D.Double(i * spacing, (j + 1) * spacing));
				} else {
					start = m.map.getClosestIntersection(new Point2D.Double(i * spacing, j * spacing));
					finish = m.map.getClosestIntersection(new Point2D.Double((i + 1) * spacing, (j + 1) * spacing));
				}
				m.map.addTwoWayStreet(start, finish, Lane.DEFAULT_LANE_WIDTH / 2);
			}
		}
		
		m.setSpawnDelay(250);
		m.setMaxVehicles(1);
		return m;
	}

}
