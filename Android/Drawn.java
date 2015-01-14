/*
 * Drawn.java
 * Model representing a drawn object with a position in Sock Matcher game
 * Created by Joseph M. Caplan Dec. 28, 2014
 */
package com.penguin.sockmatcher;

public class Drawn {
	private int baseX, baseY, x, y;
	protected int val;

    /**
     * Default Constructor
     * Sets all values to zero
     */
	public Drawn() {
		baseX = 0;
		baseY = 0;
		val = 0;
		x = 0;
		y = 0;
	}

    /**
     * Argumented Contructor
     * @param baseX int baseX
     * @param baseY int baseY
     * @param val int val
     * @param x int x
     * @param y int y
     */
	public Drawn(int baseX, int baseY, int val, int x, int y) {
		this.baseX = baseX;
		this.baseY = baseY;
		this.val = val;
		this.x = x;
		this.y = y;
	}

    /**
     * Public accessor for baseX
     * @return int baseX
     */
	public final int getBaseX() {
		return baseX;
	}

    /**
     * Public accessor for baseY
     * @return int baseY
     */
	public final int getBaseY() {
		return baseY;
	}

    /**
     * Public accessor for val
     * @return int val
     */
	public final int getVal() {
		return val;
	}

    /**
     * Public accessor for x
     * @return int x
     */
	public final int getX() {
		return x;
	}

    /**
     * Public accessor for y
     * @return int y
     */
	public final int getY() {
		return y;
	}

    /**
     * Public mutator for val
     * @param v int val
     */
	public void setVal(int v) {
		val = v;
	}

    /**
     * Sets baseX and baseY values to arguments
     * @param bX int new baseX value
     * @param bY int new baseY value
     */
	public void setBaseXY(int bX, int bY) {
		baseX = bX;
		baseY = bY;
	}

    /**
     * Sets x and y values to arguments
     * @param x int new x value
     * @param y int new y value
     */
	public void setXY(int x, int y) {
		this.x = x;
		this.y = y;
	}

    /**
     * Translates x value by argument
     * @param xTranslation int value to translate x by
     */
	public void translateX(int xTranslation) {
		this.x += xTranslation;
	}

    /**
     * Translates y value by argument
     * @param yTranslation int value to translate y by
     */
	public void translateY(int yTranslation) {
		this.y += yTranslation;
	}

    /**
     * Translates x and y values by arguments
     * @param xTranslate int to translate x value by
     * @param yTranslate int to translate y value by
     */
	public void move(int xTranslate, int yTranslate) {
		x += xTranslate;
		y += yTranslate;
	}

    /**
     * Resets x and y values back to base values
     */
	public void returnToBase() {
		x = baseX;
		y = baseY;
	}

    /**
     * Compares Drawn object to another to check for equality by comparing base position values
     * @param obj Object to compare for equality
     * @return true if objects have same base position values; false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        Drawn drawn = ((Drawn)obj);
        return this.baseX == drawn.baseX && this.baseY == drawn.baseY;
    }
}
