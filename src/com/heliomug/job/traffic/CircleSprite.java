package com.heliomug.job.traffic;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class CircleSprite extends Ageable implements Boundable {
	private double radius;
	private Point2D location;

	public CircleSprite() {
		location = null;
		radius = 0;
	}
	
	public CircleSprite(Point2D p, double r) {
		location = p;
		radius = r;
	}
	
	public Point2D getLocation() { return location; }
	public double getX() { return location.getX(); }
	public double getY() { return location.getY(); }
	public double getRadius() { return radius; }; 
	
	public void setLocation(Point2D p) { location = p; }
	public void setLocation(double x, double y) { setLocation(new Point2D.Double(x, y)); }
	public void translate(double dx, double dy) { setLocation(getX() + dx, getY() + dy); }
	
	public boolean intersects(CircleSprite other) {
		double d = location.distance(other.getLocation());
		return d < radius + other.radius;
	}
	
	public Rectangle2D getBounds() {
		return new Rectangle2D.Double(location.getX() - radius, location.getY() - radius, radius * 2, radius * 2);
	}
	
	public boolean contains(Point2D p) {
		double dx = this.location.getX() - p.getX();
		double dy = this.location.getY() - p.getY();
		return dx * dx + dy * dy < radius * radius;
	}

	public boolean contains(CircleSprite sprite) {
		double dist = location.distance(sprite.location);
		return dist + sprite.radius < radius;
	}
	
	public void draw(Graphics2D g) {
		g.fill(new Ellipse2D.Double(getX() - radius, getY() - radius, radius * 2, radius * 2));
	}
}
