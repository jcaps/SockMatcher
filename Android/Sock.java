/*
 * Sock.java
 * Represents an individual sock in game; extends Drawn; implements Node
 * Copyright Joseph M. Caplan Dec. 28, 2014
 */
package com.penguin.sockmatcher;

import java.util.ArrayList;
import java.util.List;

public class Sock extends Drawn implements Node {
	private boolean visited;
	private int index;
	private List<Node> neighbors;
	private List<Node> corners;

    /**
     * Default Constructor
     */
	public Sock() {
		super();
		visited = false;
		index = 0;
		neighbors = new ArrayList<Node>();
		corners = new ArrayList<Node>();
	}

    /**
     * Argumented Constructor
     * @param index int index of sock in board
     * @param val int value of sock
     * @param x int x coord
     * @param y int y coord
     */
	public Sock(int index, int val, int x, int y) {
        super(x, y, val, x, y);
		this.index = index;
		visited = false;
		neighbors = new ArrayList<Node>();
		corners = new ArrayList<Node>();
	}

    /**
     * Public accessor for visited
     * @return boolean visited
     */
	@Override
	public final boolean isVisited() {
		return visited;
	}

    /**
     * Public accessor for index
     * @return int index
     */
	public final int getIndex() {
		return index;
	}

    /**
     * Public accessor for neighbors
     * @return List<Node> neighbors
     */
	@Override
	public final List<Node> getNeighbors() {
		return neighbors;
	}

    /**
     * Public accessor for corners
     * @return List<Node> corners
     */
	@Override
	public final List<Node> getCorners() {
		return corners;
	}

    /**
     * Sets visited to true (used for traversal)
     */
	@Override
	public void visit() {
		visited = true;
	}

    /**
     * Sets visited to false
     */
	@Override
	public void unvisit() {
		visited = false;
	}

    /**
     * Adds neighbor node to neighbors
     * @param node Node neighbor node
     */
	@Override
	public void addNeighbor(Node node) {
		// TODO Auto-generated method stub
		neighbors.add(node);
	}

    /**
     * Adds corner node
     * @param node Node corner node
     */
	@Override
	public void addCorner(Node node) {
		corners.add(node);
	}

    /**
     * Checks to see if a Sock's value matches another Sock's value
     * @param other Sock to compare values with
     * @return true if Socks have same value; false otherwise
     */
	public boolean matches(Sock other) {
		return val == other.val;
	}

    /**
     * Checks two Socks for equality (same object)
     * @param obj Object to compare for equality
     * @return true if Socks are the same; false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        return this.index == ((Sock)obj).index;
    }
}