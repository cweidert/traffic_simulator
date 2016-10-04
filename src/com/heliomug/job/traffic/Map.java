package com.heliomug.job.traffic;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Map {
	private static final double RADIUS_MEETING_RATIO = .75;
	
	private List<Intersection> intersections;
	private List<Lane> lanes;

	private QuadTree<Intersection> intersectionTree;

	private MapGraph graph;
	private Rectangle2D bounds;
	
	private boolean upToDate;
	
	public Map() {
		intersections = new ArrayList<Intersection>();
		lanes = new ArrayList<Lane>();
		intersectionTree = new QuadTree<Intersection>();
		graph = new MapGraph(intersections, lanes);
		bounds = null;
		upToDate = false;
	}
	
	public MapGraph getGraph() { 
		if (!upToDate) update();
		return this.graph; 
	} 
	
	public List<Lane> getLanes() { return this.lanes; }
	
	public QuadTree<Intersection> getIntersectionTree() { 
		if (!upToDate) update();
		return this.intersectionTree; 
	}

	public Rectangle2D getBounds() {
		if (!upToDate) update();
		return bounds;
	}

	public Point2D getRandomPoint() {
		if (!upToDate) update();
		double x = bounds.getX() + Math.random() * bounds.getWidth();
		double y = bounds.getY() + Math.random() * bounds.getHeight();
		return new Point2D.Double(x, y);
	}
	
	private void computeBounds() {
		double xMin = intersections.get(0).getBounds().getX();
		double yMin = intersections.get(0).getBounds().getX();
		double xMax = intersections.get(0).getBounds().getX() + intersections.get(0).getBounds().getWidth();
		double yMax = intersections.get(0).getBounds().getX() + intersections.get(0).getBounds().getHeight();
		for (int i = 0 ; i < intersections.size() ; i++) {
			double x = intersections.get(i).getBounds().getX();
			double y = intersections.get(i).getBounds().getY();
			xMin = Math.min(xMin, x);
			xMax = Math.max(xMax, x + intersections.get(0).getBounds().getWidth());
			yMin = Math.min(yMin, y);
			yMax = Math.max(yMax, y + intersections.get(0).getBounds().getHeight());
		}
		this.bounds = new Rectangle2D.Double(xMin, yMin, xMax - xMin, yMax - yMin);
	}

	private void update() {
		graph = new MapGraph(intersections, lanes);
		intersectionTree.refill(intersections);
		computeBounds();
		upToDate = true;
	}

	// messy
	public void setLightDuration(int dur) {
		for (Intersection i : intersections) {
			if (i instanceof IntersectionLit) {
				IntersectionLit ill = (IntersectionLit)i;
				ill.setLightDuration(dur);
			}
		}
	}
	
	private void addMultiLaneFreeway(Intersection one, Intersection two, int leavingOne, int leavingTwo, double median) {
		double x1 = one.getLocation().getX();
		double y1 = one.getLocation().getY();
		double x2 = two.getLocation().getX();
		double y2 = two.getLocation().getY();
		double x = x1 - x2;
		double y = y1 - y2;
		double heading = Math.atan2(y, x);
		x1 -= Math.cos(heading) * one.getRadius() * RADIUS_MEETING_RATIO;
		y1 -= Math.sin(heading) * one.getRadius() * RADIUS_MEETING_RATIO;
		x2 += Math.cos(heading) * two.getRadius() * RADIUS_MEETING_RATIO;
		y2 += Math.sin(heading) * two.getRadius() * RADIUS_MEETING_RATIO;
		double xOff = Math.cos(heading + Math.PI/2) * Lane.DEFAULT_LANE_WIDTH;
		double yOff = Math.sin(heading + Math.PI/2) * Lane.DEFAULT_LANE_WIDTH;

		double xMed = Math.cos(heading + Math.PI/2) * median / 2;
		double yMed = Math.sin(heading + Math.PI/2) * median / 2;
		
		for (int i = 0 ; i < leavingOne ; i++) {
			addLane(new Lane(x1 + xOff * i + xOff / 2 + xMed, y1 + yOff * i + yOff / 2 + yMed, x2 + xOff * i + xOff / 2 + xMed, y2 + yOff * i + yOff / 2 + yMed));
		}
		for (int i = 0 ; i < leavingTwo ; i++) {
			addLane(new Lane(x2 - xOff * i - xOff / 2 - xMed, y2 - yOff * i - yOff / 2 - yMed, x1 - xOff * i - xOff / 2 - xMed, y1 - yOff * i - yOff / 2 - yMed));
		}
		upToDate = false;
	}
	
	public void addTwoWayStreet(Intersection one, Intersection two) {
		addTwoWayStreet(one, two, 0);
	}
	
	public void addTwoWayStreet(Intersection one, Intersection two, double median) {
		addMultiLaneFreeway(one, two, 1, 1, median);
		upToDate = false;
	}
	
	private void addLane(Lane lane) {
		lanes.add(lane);
		upToDate = false;
	}

	public Route getRoute(Point2D start, Point2D finish) {
		if (!upToDate) update();
		return graph.getRoute(start, finish);
	}

	public void addIntersection(Intersection i) {
		intersections.add(i);
		upToDate = false;
	}

	public Intersection getClosestIntersection(Point2D p) {
		if (intersections.size() < 1) {
			return null;
		}
		
		double minDist = intersections.get(0).getLocation().distance(p);
		Intersection minInt = intersections.get(0);
		
		for (Intersection i : intersections) {
			double dist = i.getLocation().distance(p); 
			if (dist < minDist) {
				minDist = dist;
				minInt = i;
			}
		}
		return minInt;
	}
	
	public Intersection getRandomIntersection() {
		return intersections.get((int)(Math.random() * intersections.size()));
	}
	

	public void start() {
		for (Intersection i : intersections) {
			i.start(this);
		}
	}
	
	public void draw(Graphics2D g) {
		for (Lane lane : lanes) {
			lane.draw(g);
		}
		for (Intersection intersection : intersections) {
			intersection.draw(g);
		}
	}
	
}
