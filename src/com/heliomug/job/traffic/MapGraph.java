package com.heliomug.job.traffic;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapGraph {
	private HashMap<Intersection, Vertex> vertices;
	
	public MapGraph(List<Intersection> intersections, List<Lane> lanes) {
		vertices = new HashMap<Intersection, Vertex>();
		
		for (Lane lane : lanes) {
			Intersection start = null;
			Intersection finish = null;
			for (Intersection intersection : intersections) {
				if (lane.startsAt(intersection)) {
					start = intersection;
				}
				if (lane.finishesAt(intersection)) {
					finish = intersection;
				}
			}
			if (start != null && finish!= null) {
				addOneWayStreet(start, finish, lane);
			}
		}
	}
	
			
	private void checkPut(Intersection intersection) {
		if (!vertices.containsKey(intersection)) {
			vertices.put(intersection, new Vertex(intersection));
		}
	}
	
	public void addOneWayStreet(Intersection v1, Intersection v2, Lane s) {
		checkPut(v1);
		checkPut(v2);
		vertices.get(v1).addEdge(v2, s);
	}
	
	public void addTwoWayStreet(Intersection v1, Intersection v2, Lane s) {
		checkPut(v1);
		checkPut(v2);
		vertices.get(v1).addEdge(v2, s);
		vertices.get(v2).addEdge(v1, s);
	}

	private void resetAll() {
		for (Vertex v : vertices.values()) {
			v.reset();
		}
	}
	
	private void bfs(Intersection source) {
		resetAll();
		PriorityQueue<Intersection, Double> q = new PriorityQueue<Intersection, Double>();
		vertices.get(source).distance = 0;
		q.addOrUpdate(source, 0.0);
		//vertices.get(source).isVisited = true;
		
		while (!q.isEmpty()) {
			Intersection fromIntersection = q.extractMin();
			Vertex vFrom = vertices.get(fromIntersection);
			
			for (Intersection toIntersection : vFrom.getNeighbors()) {
				Vertex vTo = vertices.get(toIntersection);
				double dist = vFrom.distance + vFrom.laneTo(toIntersection).getLength();
				if (dist < vTo.distance) {
					vTo.parent = fromIntersection;
					vTo.distance = dist;
					q.addOrUpdate(toIntersection, dist);
				}
			}
		}
	}

	public Intersection closestIntersectionTo(Point2D p) {
		double minDist = Double.POSITIVE_INFINITY;
		Intersection minInt = null;
		
		for (Intersection i : vertices.keySet()) {
			double dist = i.getLocation().distance(p); 
			if (dist < minDist) {
				minDist = dist;
				minInt = i;
			}
		}
		return minInt;
	}
	
	
	public Route getRoute(Point2D start, Point2D finish) {
		Intersection source = closestIntersectionTo(start);
		Intersection target = closestIntersectionTo(finish);

		Route route = new Route();
		
		if (source == target) {
			route.push(source.getLocation());
		} else {

			bfs(source);
	
			if (vertices.get(target).parent == null) {
				System.out.println("no route from " + source + " to " + target + "!");
				return null;
			}
			Vertex v = vertices.get(target);
			
			while (v.intersection != source) {
				//route.push(v.intersection.getLocation());
				Lane lane = vertices.get(v.parent).laneTo(v.intersection);
				route.push(lane.getFinish());
				route.push(lane.getMid());
				route.push(lane.getStart());
				v = vertices.get(v.parent);
			}
			route.push(source.getLocation());
		}
	
		return route;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Vertex v : vertices.values()) {
			sb.append(v.intersection.toString() + " --> ");
			for (Intersection neigh : v.getNeighbors()) {
				sb.append(neigh.toString());
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	private class Vertex {
		public HashMap<Intersection, Lane> edges;
		
		public Intersection intersection;
		
		public double distance;
		//public int start;
		//public int finish;
		//public boolean isVisited;
		public Intersection parent;

		public Vertex(Intersection intersection) {
			edges = new HashMap<Intersection, Lane>();
			this.intersection = intersection;
			reset();
		}

		public void reset() {
			this.distance = Double.POSITIVE_INFINITY;
			//this.start = 0;
			//this.finish = 0;
			//this.isVisited = false;
			this.parent = null;
		}

		public List<Intersection> getNeighbors() {
			List<Intersection> toRet = new ArrayList<Intersection>();
			for (Intersection i : edges.keySet()) {
				toRet.add(i);
			}
			return toRet;
		}
		
		public Lane laneTo(Intersection other) {
			return edges.get(other);
		}
		
		public void addEdge(Intersection other, Lane lane) {
			edges.put(other, lane);
		}
		
	}

}
