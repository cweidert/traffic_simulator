package com.heliomug.job.traffic;

public class VehicleQueue {
	private Node head;
	private Node tail;
	
	public VehicleQueue() {
		head = tail = null;
	}
	
	public boolean isEmpty() {
		return (head == null);
	}
	
	public Vehicle dequeue() {
		if (head == null) {
			return null;
		} else {
			Vehicle toRet = head.vehicle;
			head = head.next;
			return toRet;
		}
	}
	
	public void enqueue(Vehicle v) {
		if (head == null) {
			head = new Node(v);
			tail = head;
		} else {
			Node n = new Node(v);
			tail.next = n;
			tail = n;
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Node n = head;
		while (n != null) {
			sb.append(n.vehicle.toString());
		}
		return sb.toString();
	}
	
	private class Node {
		Vehicle vehicle;
		Node next;
		
		public Node(Vehicle v) {
			vehicle = v;
		}
	}
}
