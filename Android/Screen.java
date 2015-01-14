/*
 * Screen.java
 * Represents a different screen in Sock Matcher
 * Copyright Joseph M. Caplan Dec. 28, 2014
 */
package com.penguin.sockmatcher;

import android.view.View;

import com.penguin.sockmatcher.SockMatcherGame;

public abstract class Screen {
	protected final SockMatcherGame game;
	
	public Screen(SockMatcherGame game) {
		this.game = game;
	}

	public abstract void pause();
	
	public abstract void resume();
	
	public abstract void dispose();

    public abstract void onBackPressed();

    public abstract View getInitialView();
}
