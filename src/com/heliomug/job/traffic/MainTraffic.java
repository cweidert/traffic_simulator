package com.heliomug.job.traffic;

import java.awt.EventQueue;

import javax.swing.JFrame;

public class MainTraffic {
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				World world = World.defaultWorld();
				//World world = World.smallWorld();
				//World world = World.circleWorld(12, 30);
				TrafficPanel panel = new TrafficPanel(world);
				
				JFrame frame = new JFrame("Map");
				frame.add(panel);
				frame.pack();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
				panel.start();
				world.start();
			}
		});
	}
}
