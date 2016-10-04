package com.heliomug.utils;


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

/**
 * 
 * This is a class that's just a panel that you can scroll in.  It handles zooming and panning and so on.  
 * 
 * @author Craig Weidert
 *
 */
public class WeidertPanel extends JPanel  {
	private static final long serialVersionUID = 6395548544912733950L;

	private static final double WHEEL_ZOOM_FACTOR = 1.25;
	
	private double sLeft, sRight, sBottom, sTop;
	
	private boolean isZoomable, isDraggable;
	
	/**
	 * 
	 * Makes a new panel that you can scroll with your mouse and display shapes on an arbitrarily-sized canvas
	 * 
	 * @param pixelWidth Width of panel in pixels
	 * @param pixelHeight Height of panel in pixels
	 * @param left Coordinate on left side of screen
	 * @param right Coordinate on right side of screen
	 * @param bottom Coordinate on bottom side if screen
	 * @param top Coordinate on top side of screen
	 */
	public WeidertPanel(int pixelWidth, int pixelHeight, double left, double bottom, double width, double height) {
		super();
		this.sLeft = left;
		this.sRight = left + width;
		this.sTop = bottom + height;
		this.sBottom = bottom;
		
		this.isZoomable = true;
		this.isDraggable = true;
		
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				fixAspectRatio();
			}
		});
		
		Mouser mouser = new Mouser();
		this.addMouseWheelListener(mouser);
		this.addMouseListener(mouser);
		this.addMouseMotionListener(mouser);

		this.setFocusable(true);
		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) { 
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					System.exit(0);
				}
			}
		});
		
		this.setPreferredSize(new Dimension(pixelWidth, pixelHeight));
		this.setDoubleBuffered(true);
	}
	
	/**
	 * Turns mouse zooming on / off
	 * 
	 * @param isZoomable Whether or not you can zoom with mouse wheel.  
	 */
	public void setZoomable(boolean isZoomable) { this.isZoomable = isZoomable; }

	/**
	 * Turns mouse dragging the screen on / off
	 * 
	 * @param isDraggable Whether or not you can zoom with mouse wheel.  
	 */
	public void setDraggable(boolean isDraggable) { this.isDraggable = isDraggable; }
	
	private Point2D getLocation(int px, int py) { 
		try {
			return getTransform().inverseTransform(new Point2D.Double(px,  py), null);
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	/**
	 * Override to paint.  Don't forget to call super.paint(g);
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setTransform(getTransform());
	}
	
	/**
	 * Scoot the screen window over by amount of pixels.  
	 * 
	 * @param dx horizontal shift
	 * @param dy vertical shift
	 */
	public void translateScreenByPixels(int dx, int dy) {
		AffineTransform t = getTransform();
		translateScreen(dx / t.getScaleX(), dy / t.getScaleY());
	}
	
	/**
	 * Scoot screen over by coordinate amounts
	 * 
	 * @param dx horizontal shift
	 * @param dy vertical shift
	 */
	public void translateScreen(double dx, double dy) {
		sLeft += dx;
		sRight += dx;
		sTop += dy;
		sBottom += dy;
	}

	public void setBounds(Rectangle2D rect) {
		setScreenBounds(rect.getX(), rect.getX() + rect.getWidth(), rect.getY(), rect.getY() + rect.getHeight());
	}
	
	/**
	 * Sets the bounds of the window
	 * 
	 * @param left left
	 * @param right right
	 * @param bottom bottom 
	 * @param top top
	 */
	private void setScreenBounds(double left, double right, double bottom, double top) {
		sLeft = left;
		sRight = right;
		sBottom = bottom;
		sTop = top;
		fixAspectRatio();
	}

	/**
	 * Sets center of screen
	 * 
	 * @param x x coord
	 * @param y y coord
	 */
	public void setCenterOfScreen(double x, double y) {
		double xSpan = (sRight - sLeft) / 2;
		double ySpan = (sTop - sBottom) / 2;
		setScreenBounds(x - xSpan, x + xSpan, y - ySpan, y + ySpan);
	}
	
	/**
	 * Sets the zoom / radius shown on the screen
	 * 
	 * @param r radius to show on the screeen
	 */
	public void setRadiusShown(double r) {
		double xCen = (sLeft + sRight) / 2;
		double yCen = (sTop + sBottom) / 2;
		if (getWidth() > getHeight()) {
			setScreenBounds(xCen - r * getWidth() / getHeight(), xCen + r * getWidth() / getHeight(), yCen - r, yCen + r);
		} else {
			setScreenBounds(xCen - r, xCen + r, yCen - r * getHeight() / getWidth(), yCen + r * getHeight() / getWidth());
		}
	}
	
	/**
	 * Dilate window about a point by a factor.  
	 * 
	 * @param x x coord
	 * @param y y coord
	 * @param s scale factor
	 */
	public void zoom(double x, double y, double s) {
		double l = x - (x - sLeft) * s;
		double r = x + (sRight - x) * s;
		double t = y + (sTop - y) * s;
		double b = y - (y - sBottom) * s;
		setScreenBounds(l, r, b, t);
	}
	
	/**
	 * Handles mouse clicks on the screen.  
	 * You can override this method to do all kinds of things with the mouse like make new sprites when you click.  
	 *  
	 * @param x x coord
	 * @param y y coord
	 * @param e This is a mouse event that contains things like which button was pushed, if you were holding shift, etc.  
	 */
	public void handleMouseClick(double x, double y, MouseEvent e) {
	}

	public final AffineTransform getTransform() {
		double xCoeff = getWidth() / (sRight - sLeft);
		double yCoeff = getHeight() / (sBottom - sTop);
		double xConst = sLeft * getWidth() / (sLeft - sRight);
		double yConst = sTop * getHeight() / (sTop - sBottom);
		return new AffineTransform(xCoeff, 0, 0, yCoeff, xConst, yConst);
	}
	
	private void fixAspectRatio() {
		AffineTransform t = getTransform();
		double xScale = Math.abs(t.getScaleX());
		double yScale = Math.abs(t.getScaleY());
		if (xScale > yScale) {
			double cen = (sLeft + sRight) / 2;
			double halfWidth = getWidth() / yScale / 2; 
			sLeft = cen - halfWidth;    
			sRight = cen + halfWidth;
		} else if (xScale < yScale) {
			double cen = (sTop + sBottom) / 2;
			double halfHeight = getHeight() / xScale / 2; 
			sBottom = cen - halfHeight;    
			sTop = cen + halfHeight;
		}
	}
	
	private class Mouser extends MouseAdapter implements MouseWheelListener {
		private int dragStartX, dragStartY;

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (isZoomable) {
				int notches = e.getWheelRotation();
				Point2D zoomPoint = getLocation(e.getX(), e.getY());
				zoom(zoomPoint.getX(), zoomPoint.getY(), Math.pow(WHEEL_ZOOM_FACTOR, notches));
				repaint();
			}
		}
	
		@Override
		public void mouseDragged(MouseEvent e) {
			if (isDraggable) {
				translateScreenByPixels(dragStartX - e.getX(), dragStartY - e.getY());
				updateDragStart(e);
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if (isDraggable) updateDragStart(e);
			Point2D p = getLocation(e.getX(), e.getY());
			WeidertPanel.this.handleMouseClick(p.getX(), p.getY(), e);
		}
		
		public void updateDragStart(MouseEvent e) {
			dragStartX = e.getX();
			dragStartY = e.getY();
		}
	}
}