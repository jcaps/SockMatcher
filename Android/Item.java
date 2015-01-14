/*
 * Item.java
 * Child class of Drawn; adds rotation attribute
 * Created by Joseph M. Caplan Dec. 28, 2014
 */
package com.penguin.sockmatcher;

public class Item extends Drawn {
	private float rot;

    /**
     * Default constructor calls super class default constructor and sets rot to 0
     */
	public Item() {
		super();
		rot = 0.0f;
	}

    /**
     * Argumented constructor
     * @param baseX int baseX
     * @param baseY int baseY
     * @param val int val
     * @param x int x
     * @param y int y
     */
	public Item(int baseX, int baseY, int val, int x, int y) {
		super(baseX, baseY, val, x, y);
		rot = 0.0f;
	}

    /**
     * Public accessor for rot
     * @return float rot
     */
	public final float getRot() {
		return this.rot;
	}

    /**
     * Public mutator for rot
     * @param rot float rot
     */
	public void setRot(float rot) {
		this.rot = rot;
	}
}
