/*
 * Node.java
 * Interface to represent individual nodes in a graph data structure
 * Created by Joseph M. Caplan Dec. 28, 2014
 */
package com.penguin.sockmatcher;

import java.util.List;

public interface Node {

	public boolean isVisited();
	
	public List<Node> getNeighbors();
	
	public List<Node> getCorners();
	
	public void visit();
	
	public void unvisit();

	public void addNeighbor(Node node);
	
	public void addCorner(Node node);
}
