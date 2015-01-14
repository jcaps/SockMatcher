/*
 * MainMenuScreen.java
 * Created by Joseph M. Caplan Dec. 28, 2014
 */
package com.penguin.sockmatcher;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static com.penguin.sockmatcher.R.drawable;

public class MainMenuScreen extends Screen {
    private boolean musicOn, soundOn;
    private int highScore;
    private View view;

    /**
     * Constructor
     * @param game SockMatcherGame main activity
     * @param musicOn boolean musicOn
     * @param soundOn boolean soundOn
     * @param highScore int highScore
     */
	public MainMenuScreen(final SockMatcherGame game, boolean musicOn, boolean soundOn,
                          int highScore) {
        super(game);

        this.musicOn = musicOn;
        this.soundOn = soundOn;
        this.highScore = highScore;

        float scaleX = game.getScaleX();
        float scaleY = game.getScaleY();

        view = game.getLayoutInflater().inflate(R.layout.main_menu_layout, null);

        //Play Button
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = (int)(310/scaleY);
        layoutParams.leftMargin = (int)(185/scaleX);
        layoutParams.width = (int)(423/scaleX);
        layoutParams.height = (int)(286/scaleY);
        View button = view.findViewById(R.id.playButton);
        button.setLayoutParams(layoutParams);
        button.setOnClickListener(new playButtonOnClickListener());

        //Music Button
        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int)(608/scaleX);
        layoutParams.topMargin = (int)(310/scaleY);
        layoutParams.width = (int)(209/scaleX);
        layoutParams.height = (int)(143/scaleY);
        button = view.findViewById(R.id.musicButton);
        button.setLayoutParams(layoutParams);
        button.setOnClickListener(new musicButtonOnClickListener());
        if (musicOn) {
            button.setBackground(game.getResources().getDrawable(R.drawable.musicbuttonon));
        } else {
            button.setBackground(game.getResources().getDrawable(R.drawable.musicbuttonoff));
        }

        //Sound Button
        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int)(608/scaleX);
        layoutParams.topMargin = (int)(453/scaleY);
        layoutParams.width = (int)(209/scaleX);
        layoutParams.height = (int)(143/scaleY);
        button = view.findViewById(R.id.soundButton);
        button.setLayoutParams(layoutParams);
        button.setOnClickListener(new soundButtonOnClickListener());
        if (soundOn) {
            view.findViewById(R.id.soundButton).setBackground(game.getResources().getDrawable(
                    drawable.soundbuttonon));
        } else {
            view.findViewById(R.id.soundButton).setBackground(game.getResources().getDrawable(
                    R.drawable.soundbuttonoff));
        }

        //How To Play Button
        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int)(817/scaleX);
        layoutParams.topMargin = (int)(382/scaleY);
        layoutParams.width = (int)(279/scaleX);
        layoutParams.height = (int)(189/scaleY);
        button = view.findViewById(R.id.howtoplayButton);
        button.setLayoutParams(layoutParams);
        button.setOnClickListener(new howToPlayButtonOnClickListener());

        ((TextView)view.findViewById(R.id.HighScoreTextView)).setTextSize(game.getTextSize());
        ((TextView)view.findViewById(R.id.HighScoreNumericTextView)).setTextSize(
                game.getTextSize());
        ((TextView)view.findViewById(R.id.HighScoreNumericTextView)).setText(Integer.toString(
                this.highScore));
	}

    /**
     * Returns initial and sole View
     * @return View initial and only view
     */
    public View getInitialView() {
        return view;
    }

    public void pause()
    {

    }

    public void resume()
    {

    }

    @Override
    public void dispose()
    {

    }

    /**
     * Responds to back button pressed; minimizes application
     */
    @Override
    public void onBackPressed() {
        game.moveTaskToBack(true);
    }

    /**
     * OnClickListener for "Play" button
     */
    class playButtonOnClickListener implements View.OnClickListener {
        /**
         * Changes to game screen
         * @param view View sender
         */
        @Override
        public void onClick(View view) {
            game.setScreen(SockMatcherGame.GameScreenType.GAME_SCREEN);
        }
    }

    /**
     * OnClickListener for music toggle button
     */
    class musicButtonOnClickListener implements View.OnClickListener {
        /**
         * Toggles music on/off; updates button appearance accordingly; persists music on/off
         * through main activity
         * @param view View sender
         */
        @Override
        public void onClick(View view) {
            if (musicOn) {
                musicOn = false;
                view.findViewById(R.id.musicButton).setBackground(game.getResources().getDrawable(
                        R.drawable.musicbuttonoff));
            } else {
                musicOn = true;
                view.findViewById(R.id.musicButton).setBackground(game.getResources().getDrawable(
                        R.drawable.musicbuttonon));
            }

            game.setMusicOn(musicOn);  //Persist through main activity
        }
    }

    /**
     * OnClickListener for sound toggle button
     */
    class soundButtonOnClickListener implements View.OnClickListener {
        /**
         * Toggles sound on/off; updates button appearance accordingly; persists sound on/off
         * through main activity
         * @param view View sender
         */
        @Override
        public void onClick(View view) {
            if (soundOn) {
                soundOn = false;
                view.findViewById(R.id.soundButton).setBackground(game.getResources().getDrawable(
                        R.drawable.soundbuttonoff));
            } else {
                soundOn = true;
                view.findViewById(R.id.soundButton).setBackground(game.getResources().getDrawable(
                        drawable.soundbuttonon));
            }

            game.setSoundOn(soundOn);  //Persist through main activity
        }
    }

    /**
     * OnClickListener for "How To Play" button
     */
    class howToPlayButtonOnClickListener implements View.OnClickListener {
        /**
         * Sets screen to "How To Play" screen
         * @param view View sender
         */
        @Override
        public void onClick(View view) {
            game.setScreen(SockMatcherGame.GameScreenType.HOW_TO_PLAY_SCREEN);
        }
    }
}
