/*
 * HowToPlayScreen.java
 * Screen for "How To Play" screen in Sock Matcher
 * Created by Joseph M. Caplan Dec. 28, 2014
 */
package com.penguin.sockmatcher;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HowToPlayScreen extends Screen {
    private View view;

    /**
     * Constructor initializes layout
     * @param game SockMatcherGame main activity
     */
    public HowToPlayScreen(SockMatcherGame game) {
        super(game);

        float scaleX = game.getScaleX();
        float scaleY = game.getScaleY();

        view = game.getLayoutInflater().inflate(R.layout.howtoplay_layout, null);

        //Main Menu Button
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.width = (int)(178/scaleX);
        layoutParams.height = (int)(121/scaleY);
        View childView = view.findViewById(R.id.tutorialImageButton);
        childView.setLayoutParams(layoutParams);
        childView.setOnClickListener(new MainMenuButtonOnClickListener());

        //How To Play Text
        ((TextView)view.findViewById(R.id.howToPlayTextView)).setTextSize(game.getTextSize());

        //Tutorial1
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams2.width = (int)(1280/scaleX);
        layoutParams2.height = (int)(680/scaleY);
        childView = view.findViewById(R.id.tutorialImageView1);
        childView.setLayoutParams(layoutParams2);
        childView = view.findViewById(R.id.tutorialImageView2);
        childView.setLayoutParams(layoutParams2);
        childView = view.findViewById(R.id.tutorialImageView3);
        childView.setLayoutParams(layoutParams2);
        childView = view.findViewById(R.id.tutorialImageView4);
        childView.setLayoutParams(layoutParams2);
        childView = view.findViewById(R.id.tutorialImageView5);
        childView.setLayoutParams(layoutParams2);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }

    /**
     * Responds to back button pressed; returns to main menu screen
     */
    @Override
    public void onBackPressed() {
        game.setScreen(SockMatcherGame.GameScreenType.MAIN_MENU_SCREEN);
    }

    /**
     * Returns only View
     * @return View initial and sole View
     */
    @Override
    public View getInitialView() {
        return view;
    }

    /**
     * OnClickListener for Main Menu button
     */
    public class MainMenuButtonOnClickListener implements View.OnClickListener {
        /**
         * Returns to main menu screen
         * @param view View sender
         */
        @Override
        public void onClick(View view) {
            game.setScreen(SockMatcherGame.GameScreenType.MAIN_MENU_SCREEN);
        }
    }
}
