package com.heliomug.job.traffic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PriorityQueue<Value, Key extends Comparable<Key>> {
	private List<Node> nodes;
	private HashMap<Value, Node> map; 
	
	public PriorityQueue() {
		nodes = new ArrayList<Node>();
		map = new HashMap<Value, Node>();
	}
	
	public int size() {
		return nodes.size();
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public void addOrUpdate(Value value, Key key) {
		Node n = map.get(value); 
		if (n == null) {
			n = new Node(key, value);
			nodes.add(n);
			map.put(value, n);
			siftUp(nodes.size() - 1);
		} else {
			if (key.compareTo(n.key) < 0) { 
				n.key = key;
				// bad running time here
				siftUp(nodes.indexOf(n));
			}
		}
	}
	
	public Value extractMin() {
		if (size() == 0) {
			return null;
		} else if (size() == 1) {
			Value toRet = nodes.remove(0).value; 
			map.remove(toRet);
			return toRet;
		} else {
			Value toRet = nodes.get(0).value;
			map.remove(toRet);
			nodes.set(0, nodes.get(nodes.size() - 1));
			nodes.remove(nodes.size() - 1);
			siftDown(0);
			return toRet;
		}
	}
	
	private void siftDown(int index) {
		while (index < nodes.size()) {
			Key min = nodes.get(index).key;;
			int minIndex = index;
			int leftIndex = left(index);
			int rightIndex = right(index);
			
			if (leftIndex < nodes.size() && nodes.get(leftIndex).key.compareTo(min) < 0) {
				min = nodes.get(leftIndex).key;
				minIndex = leftIndex;
			} 
			if (rightIndex < nodes.size() && nodes.get(rightIndex).key.compareTo(min) < 0) {
				min = nodes.get(rightIndex).key;
				minIndex = rightIndex;
			}
			
			if (min == nodes.get(index).key) {
				break;
			} else {
				swap(index, minIndex);
				index = minIndex;
			}
		}
	}
	
	private void siftUp(int ind) {
		while (ind > 0) {
			Key parentKey = nodes.get(parent(ind)).key;
			Key childKey = nodes.get(ind).key;
			if (parentKey.compareTo(childKey) > 0) {
				swap(ind, parent(ind));
				ind = parent(ind);
			} else {
				break;
			}
		}
	}
	
	private void swap(int i, int j) {
		Node temp = nodes.get(i);
		nodes.set(i, nodes.get(j));
		nodes.set(j, temp);
	}
	
	private int left(int ind) { return ind * 2 + 1; }
	private int right(int ind) { return ind * 2 + 2; }
	private int parent(int ind) { return (ind - 1) / 2; }
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Node n : nodes) {
			sb.append(String.format("%s: %s\n", n.key, n.value));
		}
		return sb.toString();
	}
	
	private class Node {
		Key key;
		Value value;
		
		public Node(Key key, Value value) {
			this.key = key;
			this.value = value;
		}
	}
	
	/*
	public static void main(String[] args) {
		PriorityQueue<String, Integer> q = new PriorityQueue<String, Integer>();
		String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		for (int i = 0 ; i < 25 ; i++) {
			q.addOrUpdate(letters.substring(i, i + 1), (int)(Math.random() * 100));
		}
		System.out.println(q);
		while (!q.isEmpty()) {
			System.out.println(q.extractMin());
		}
	}
	*/
}
