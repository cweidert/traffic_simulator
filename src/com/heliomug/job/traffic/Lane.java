package com.heliomug.job.traffic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import com.heliomug.utils.MiscUtils;

public class Lane {
	public static final double DEFAULT_LANE_WIDTH = 1;

	private static final Color LANE_COLOR = new Color(0, 0, 0);
	
	private BasicStroke stroke;
	
	private Point2D start;
	private Point2D finish;
	private double width;
	
	public Lane(double x1, double y1, double x2, double y2) {
		this.start = new Point2D.Double(x1, y1);
		this.finish = new Point2D.Double(x2, y2);
		this.width = DEFAULT_LANE_WIDTH;
		this.stroke = new BasicStroke((float)this.width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	}

	public Point2D getStart() { return this.start; }
	public Point2D getFinish() { return this.finish; }
	public Point2D getMid() {
		return MiscUtils.midpoint(start, finish);
	}
	
	public double getHeading() {
		return MiscUtils.heading(start, finish);
	}
	
	
	public boolean startsAt(Intersection intersection) {
		return intersection.contains(start);
	}
	
	public boolean finishesAt(Intersection intersection) {
		return intersection.contains(finish);
	}
	
	public double getLength() {
		double dx = start.getX() - finish.getX();
		double dy = start.getY() - finish.getY();
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	public void draw(Graphics2D g) {
		g.setStroke(stroke);
		g.setColor(LANE_COLOR);
		g.draw(new Line2D.Double(start, finish));
		g.setColor(Color.WHITE);
		g.setStroke(TrafficPanel.STANDARD_STROKE);
		MiscUtils.drawArrow(g, getMid(), getHeading(), width / 2);
	}
}
