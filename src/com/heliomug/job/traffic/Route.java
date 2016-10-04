package com.heliomug.job.traffic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.heliomug.utils.MiscUtils;

public class Route {
	private static final Color DEFAULT_ROUTE_COLOR = Color.WHITE;
	
	private List<Point2D> points;
	private double mid;
	
	public Route() {
		points = new ArrayList<Point2D>();
		mid = Math.random();
	}
	
	public boolean isEmpty() {
		return points.size() == 0;
	}

	public int size() {
		return points.size();
	}
	
	public void push(Point2D point) {
		points.add(point);
	}
	
	public Point2D peek() {
		return points.get(points.size() - 1);
	}
	
	public Point2D pop() {
		return points.remove(points.size() - 1);
	}

	public void draw(Graphics2D g, Point2D start) {
		draw(g, start, DEFAULT_ROUTE_COLOR);
	}
	
	public void draw(Graphics2D g, Point2D start, Color c) {
		if (points.size() < 1) return;
		
		g.setColor(c);
		g.draw(new Line2D.Double(start, peek()));
		draw(g);
	}
	
	private void draw(Graphics2D g) {
		for (int i = 0 ; i < points.size() - 1 ; i++) {
			Point2D to = points.get(i);
			Point2D from = points.get(i + 1);
			g.draw(new Line2D.Double(from, to));
			MiscUtils.drawArrow(g, MiscUtils.extrapolate(from, to, mid), MiscUtils.heading(from, to), .5);
		}
	}
	
}
