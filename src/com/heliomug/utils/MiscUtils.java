package com.heliomug.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

public class MiscUtils {
	public static void drawArrow(Graphics2D g, Point2D p, double heading, double radius) {
		drawArrow(g, p.getX(), p.getY(), heading, radius);
	}
	
	public static void drawArrow(Graphics2D g, double x, double y, double heading, double radius) {
		double theta = heading + Math.PI * 3 / 4;
		g.draw(new Line2D.Double(x, y, Math.cos(theta) * radius + x, Math.sin(theta) * radius + y));
		g.draw(new Line2D.Double(x, y, Math.cos(theta + Math.PI / 2) * radius + x, Math.sin(theta + Math.PI / 2) * radius + y));
	}

	
	public static Point2D extrapolate(Point2D p, Point2D q, double t) {		
		double midX = p.getX() * t + q.getX() * (1 - t);
		double midY = p.getY() * t + q.getY() * (1 - t);
		return new Point2D.Double(midX, midY);
	}
	
	public static Point2D midpoint(Point2D p, Point2D q) {
		return extrapolate(p, q, .5);
	}
	
	public static double heading(Point2D p, Point2D q) {
		return Math.atan2(q.getY() - p.getY(), q.getX() - p.getX());
	}
	
	public static double angleDiff(double theta, double gamma) {
		double diff = theta - gamma;
		while (diff < -Math.PI * 1.01) diff += 2 * Math.PI;
		while (diff > Math.PI * 1.01) diff -= 2 * Math.PI;
		return diff;
	}
	
	public static Point2D polar(double length, double theta) {
		return new Point2D.Double(length * Math.cos(theta), length * Math.sin(theta));
	}
	
	
	public static Point2D translate(Point2D p, double x, double y) {
		return new Point2D.Double(p.getX() + x, p.getY() + y);
	}

	public static Rectangle2D translate(Rectangle2D rect, Point2D trans) {
		return translate(rect, trans.getX(), trans.getY());
	}
	
	public static Rectangle2D translate(Rectangle2D rect, double x, double y) {
		return new Rectangle2D.Double(rect.getX() + x, rect.getY() + y, rect.getWidth(), rect.getHeight());
	}

	
	public static void main(String[] args) {
		System.out.println(angleDiff(Math.PI, Math.PI / 2));
	}

	
	public static Color getRandomColor() {
		Random r = new Random();
		return new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256));
	}
	
	public static Color getCycleColor(double cycleLength) {
		int lengthInMillis = (int)(cycleLength * 1000);
		float h = (float)(System.currentTimeMillis() % lengthInMillis) / lengthInMillis;
		return Color.getHSBColor(h, 1.0f, 1.0f);
	}
	
	
	public static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
