package com.heliomug.job.traffic;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.heliomug.utils.WeidertPanel;

@SuppressWarnings("serial")
public class TrafficPanel extends JPanel implements Runnable {
	public static final BasicStroke STANDARD_STROKE = new BasicStroke(.125f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND); 

	private static final long serialVersionUID = 3835146602141298672L;
	private static final Color GRASS_COLOR = new Color(0, 128, 0);
	
	private static final boolean DEFAULT_SHOW_TURRETS = true;
	private static final boolean DEFAULT_SHOW_COLORS = true;
	private static final boolean DEFAULT_SHOW_ROUTES = false;
	private static final boolean DEFAULT_SHOW_TREE = false;
	private static final boolean DEFAULT_SHOW_TRAILS = false;
	
	private static final int SLEEP_DELAY = 40;
	private static final int MAX_MAX_VEHICLES = 500;
	private static final int MAX_VEHICLES_PER_SECOND = 20;
	private static final int MAX_LIGHT_DURATION = 20;
	
	private World world;

	private boolean showTree;
	private boolean showRoutes;
	private boolean showTrueColors;
	private boolean showTurrets;
	private boolean showTrails; 
	
	public TrafficPanel(World world) {
		this.world = world;
		showTree = DEFAULT_SHOW_TREE;
		showRoutes = DEFAULT_SHOW_ROUTES;
		showTrueColors = DEFAULT_SHOW_COLORS;
		showTurrets = DEFAULT_SHOW_TURRETS;
		showTrails = DEFAULT_SHOW_TRAILS;
		
		setupGUI();
	}

	private void setupGUI() {
		this.setFocusable(true);
		this.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					System.exit(0);
				}
			}
		});
		
		this.setLayout(new BorderLayout());

		WeidertPanel worldPanel = new WeidertPanel(640, 640, -5, -5, 40, 40) {
			private static final long serialVersionUID = 7080849136809971031L;

			@Override
			public void handleMouseClick(double x, double y, MouseEvent e) {
				TrafficPanel.this.handleMouseClick(x, y, e);
			}
			
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D)g;
				TrafficPanel.this.world.draw(g2, showTree, showTrueColors, showRoutes, showTurrets, showTrails);
			}
		};
		worldPanel.setBackground(GRASS_COLOR);
		worldPanel.setBounds(world.getBounds());
		this.add(worldPanel, BorderLayout.CENTER);
		
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new GridLayout(0, 4));
		infoPanel.setBackground(Color.WHITE);
		infoPanel.add(new JLabel("Current Vehicles:") { 
			@Override
			public void paintComponent(Graphics g) {
				this.setText("Current Vehicles: " + world.getNumberOfVehicles());
				super.paintComponent(g);
			}
		});
		infoPanel.add(new JLabel("Vehicles Born: ") {
			@Override
			public void paintComponent(Graphics g) {
				this.setText(String.format("Trips Finished: %d", Vehicle.getVehiclesReachedGoal()));
				super.paintComponent(g);
			}
		});
		infoPanel.add(new JLabel("Time: ") {
			@Override
			public void paintComponent(Graphics g) {
				this.setText(String.format("Time: %.0f", world.getAge()));
				super.paintComponent(g);
			}
		});
		infoPanel.add(new JLabel("Threads: ") {
			@Override
			public void paintComponent(Graphics g) {
				this.setText(String.format("Threads: %d", Thread.activeCount()));
				super.paintComponent(g);
			}
		});
		this.add(infoPanel, BorderLayout.NORTH);
		
		
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());
		
		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new GridLayout(0, 1));
		final SuperSlider maxVehiclesSlider = new SuperSlider("%s vehicle limit", 0, MAX_MAX_VEHICLES, world.getMaxVehicles(), MAX_MAX_VEHICLES / 5);
		maxVehiclesSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				world.setMaxVehicles(maxVehiclesSlider.getValue());
			}
		});
		sliderPanel.add(maxVehiclesSlider);
		final SuperSlider delaySlider = new SuperSlider("%s new vehicles / second", 0, MAX_VEHICLES_PER_SECOND, world.getVehiclesPerSecond(), MAX_VEHICLES_PER_SECOND/5);
		delaySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				int val = delaySlider.getValue();
				int delay = (val == 0 ? Integer.MAX_VALUE : 1000 / val); 
				world.setSpawnDelay(delay);
			}
		});
		sliderPanel.add(delaySlider);
		int defaultDur = IntersectionLit.DEFAULT_LIGHT_DURATION;
		final SuperSlider lightSlider = new SuperSlider("Light duration: %s sec", 0, MAX_LIGHT_DURATION, defaultDur / 1000, MAX_LIGHT_DURATION / 5);
		lightSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				world.setLightDuration(lightSlider.getValue() * 1000);
			}
		});
		sliderPanel.add(lightSlider);
		controlPanel.add(sliderPanel, BorderLayout.NORTH);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(0, 4));
		JButton button;
		button = new SuperButton("Show QuadTree", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showTree = !showTree;
			}
		});
		buttonPanel.add(button);
		button = new SuperButton("Show Routes", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showRoutes = !showRoutes;
			}
		});
		buttonPanel.add(button);
		button = new SuperButton("Swap Colors", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showTrueColors = !showTrueColors;
			}
		});
		buttonPanel.add(button);
		button = new SuperButton("Show Directions", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showTurrets = !showTurrets;
			}
		});
		buttonPanel.add(button);
		/*
		button = new SuperButton("Show Trails", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showTrails = !showTrails;
			}
		});
		buttonPanel.add(button);
		*/
		controlPanel.add(buttonPanel, BorderLayout.SOUTH);
		this.add(controlPanel, BorderLayout.SOUTH);
	}

	public void handleMouseClick(double x, double y, MouseEvent e) {
		//System.out.println(String.format("Click at (%.2f, %.2f)!", x, y));
		boolean left = SwingUtilities.isLeftMouseButton(e);
		boolean right = SwingUtilities.isRightMouseButton(e);
		boolean center = SwingUtilities.isMiddleMouseButton(e);
		if (center) {
			world.spawnCarAt(x, y);
		}
		
		if (left) {
			world.logAt(x, y);
		}
		
		if (right && !left) {
			world.killVehicleAt(x, y);
		}
	}
	
	public void reset() {
		world = World.defaultWorld();
		setupGUI();
	}
	
	public void start() {
		Thread t = new Thread(this);
		t.start();
	}
	
	public void run() {
		while (true) {
			repaint();
			try {
				Thread.sleep(SLEEP_DELAY);
			} catch (InterruptedException e) {
				System.out.println("Graphics Interrupted!");
				e.printStackTrace();
			}
		}
	}
	
	private class SuperButton extends JButton {
		public SuperButton(String title, ActionListener al) {
			super(title);
			this.addActionListener(al);
			this.setFocusable(false);
		}
	}
	
	private class SuperSlider extends JPanel {

		private JSlider slider;
		private JLabel label;
		
		
		public SuperSlider(String title, int low, int high, int initial, int maj) { //, int min) {
			this.setLayout(new GridLayout(1, 2));
			label = new JLabel(title) { 
				public void paintComponent(Graphics g) {
					this.setText(String.format(title, slider.getValue()));
					super.paintComponent(g);
				}
			};
			label.setHorizontalAlignment(JLabel.CENTER);
			this.add(label);
			slider = new JSlider(low, high, initial);
			slider.setMajorTickSpacing(maj);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			slider.setFocusable(false);
			this.add(slider);
		}
		
		public int getValue() {
			return slider.getValue();
		}
		
		public void addChangeListener(ChangeListener cl) {
			slider.addChangeListener(cl);
		}
	}

}
