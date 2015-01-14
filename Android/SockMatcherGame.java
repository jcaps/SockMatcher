/*
 * SockMatcherGame.java
 * Main activity for Sock Matcher
 * Copyright Joseph M. Caplan Dec. 28, 2014
 */
package com.penguin.sockmatcher;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class SockMatcherGame extends Activity {
    enum GameScreenType {
        MAIN_MENU_SCREEN,
        GAME_SCREEN,
        HOW_TO_PLAY_SCREEN
    }

    private boolean musicOn, soundOn;
    private float scaleX, scaleY, textSize;
	private int highScore;
    private int[] soundIds;
    private GameScreen gameScreen;
    private MediaPlayer bgMusicPlayer;
    private Screen screen;
    private SoundPool sounds;
    private View overlayView;

    /**
     * Main Constructor
     * @param SavedInstance
     */
	@Override
	public void onCreate(Bundle SavedInstance) {
		super.onCreate(SavedInstance);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SharedPreferences prefs = this.getSharedPreferences("socksPrefsKey",
				Context.MODE_PRIVATE);
        this.musicOn = prefs.getBoolean("socksMusicOn", true);
        this.soundOn = prefs.getBoolean("socksSoundOn", true);
        this.highScore = prefs.getInt("socksHighScore", 0);

        loadMusic();
        loadSounds();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int pixelWidth = metrics.widthPixels;
        int pixelHeight = metrics.heightPixels;
        scaleX = (float)1280/pixelWidth;
        scaleY = (float)800/pixelHeight;

        int density = metrics.densityDpi;
        double screenInches = Math.sqrt(Math.pow(((double)pixelWidth/(double)density), 2) +
                Math.pow(((double)pixelHeight/(double)density), 2));
        textSize = (float)(32 * screenInches / 4.65);

        gameScreen = new GameScreen(this);

        setScreen(GameScreenType.MAIN_MENU_SCREEN);
	}

    /**
     * Reacts to application pause
     */
    @Override
    public void onPause() {
        super.onPause();

        //If music is on, pause music
        if (musicOn) {
            bgMusicPlayer.pause();
        }

        //Pause current screen
        screen.pause();
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * Reacts to application resume
     */
    @Override
    public void onResume() {
        super.onResume();

        //If music is on, resume music
        if (musicOn) {
            bgMusicPlayer.start();
        }

        //Resume current screen
        screen.resume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * Reacts to application being destroyed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        gameScreen.dispose();
    }

    /**
     * Reacts to back button being pressed
     */
    @Override
    public void onBackPressed() {
        screen.onBackPressed();
    }

    /**
     * Loads background music and if music on, starts playback
     */
    public void loadMusic() {
        bgMusicPlayer = MediaPlayer.create(this, R.raw.bgmusic);
        bgMusicPlayer.setLooping(true);

        //If music is on, start playing
        if (this.musicOn) {
            bgMusicPlayer.start();
        }
    }

    /**
     * Initialized SoundPool sounds and loads game sounds
     */
    public void loadSounds() {
        soundIds = new int[7];
        sounds = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        soundIds[0] = sounds.load(this, R.raw.bleach, 1);
        soundIds[1] = sounds.load(this, R.raw.burn, 1);
        soundIds[2] = sounds.load(this, R.raw.drop, 1);
        soundIds[3] = sounds.load(this, R.raw.fireball, 1);
        soundIds[4] = sounds.load(this, R.raw.pop, 1);
        soundIds[5] = sounds.load(this, R.raw.sockremover, 1);
        soundIds[6] = sounds.load(this, R.raw.spray, 1);
    }

    /**
     * Removes current screen and sets overlay to specified screen
     * @param gameScreenType GameScreenType representing screen to change to
     */
    public void setScreen(GameScreenType gameScreenType) {
        if (overlayView != null)
            removeOverlayView();

        switch (gameScreenType) {
            case MAIN_MENU_SCREEN:
                screen = new MainMenuScreen(this, musicOn, soundOn, highScore);
                setOverlayView(screen.getInitialView());
                break;
            case GAME_SCREEN:
                screen = gameScreen;
                gameScreen.prepareToAppear();
                setOverlayView(gameScreen.getInitialView());
                break;
            case HOW_TO_PLAY_SCREEN:
                screen = new HowToPlayScreen(this);
                setOverlayView(screen.getInitialView());
                break;
            default:
        }
    }

    /**
     * Sets the overlay view to View passed as parameter; runs on UIThread
     * @param overlayView View to set overlay view to
     */
    public void setOverlayView(final View overlayView) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SockMatcherGame.this.addContentView(overlayView, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                SockMatcherGame.this.overlayView = overlayView;
            }
        });
    }

    /**
     * Removes the current overlay view; runs on UIThread
     */
    public void removeOverlayView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewGroup rootView = (ViewGroup)(SockMatcherGame.this.overlayView.getParent());
                rootView.removeView(overlayView);
                overlayView = null;
            }
        });
    }

    /**
     * Sets the text in the TextView passed to the String passed; runs on UIThread
     * @param view TextView to set text
     * @param text String to set text in TextView to
     */
    public void setTextInTextView(final TextView view, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setText(text);
            }
        });
    }

    /*
     * Public Accessors/Mutators
     */

    /**
     * Public accessor for Context
     * @return Context of main activity
     */
	public Context getContext() {
		return this;
	}

    /**
     * Public accessor for highScore
     * @return int high score
     */
	public final int getHighScore() {
		return this.highScore;
	}

    /**
     * Public mutator for highScore; updates value in app's shared preferences
     * @param highScore int value to set as high score
     */
	public void setHighScore(int highScore) {
		this.highScore = highScore;
		
		SharedPreferences prefs = this.getSharedPreferences("socksPrefsKey", 
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putInt("socksHighScore", this.highScore);
		editor.commit();
	}

    /**
     * Public mutator for musicOn; plays music if music is set to on; pauses music if music is set
     * to off; updates boolean value in app's shared preferences
     * @param musicOn boolean whether or not music is on
     */
    public void setMusicOn(boolean musicOn) {
        this.musicOn = musicOn;

        if (musicOn) {
            bgMusicPlayer.start();
        } else {
            bgMusicPlayer.pause();
        }

        SharedPreferences prefs = this.getSharedPreferences("socksPrefsKey", Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putBoolean("socksMusicOn", musicOn);
        editor.commit();
    }

    /**
     * Public mutator for soundOn; sets shared preferences
     * @param soundOn boolean whether or not sound is on
     */
    public void setSoundOn(boolean soundOn) {
        this.soundOn = soundOn;

        SharedPreferences prefs = this.getSharedPreferences("socksPrefsKey", Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putBoolean("socksSoundOn", soundOn);
        editor.commit();
    }

    public final float getScaleX() {
        return this.scaleX;
    }

    public final float getScaleY() {
        return this.scaleY;
    }

    public final float getTextSize() {
        return this.textSize;
    }

    public final boolean getSoundOn() {
        return this.soundOn;
    }

    public final SoundPool getSounds() {
        return sounds;
    }

    public final int[] getSoundIds() {
        return soundIds;
    }
}  //end class SockMatcherGame