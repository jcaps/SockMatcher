/*
 * GameScreen.java
 * Screen for main gameplay; contains SockGame object and handles user input when screen is present
 * Created by Joseph M. Caplan Dec. 28, 2014
 */
package com.penguin.sockmatcher;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.SoundPool;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class GameScreen extends Screen implements GLSurfaceView.Renderer, View.OnTouchListener {
    //Enum representing current game state
    enum GameState {
		Ready, Running, Paused, GameOver
	}

	GameState state = GameState.Ready;

    private boolean soundOn;
    private Context mContext;
    private float scaleX, scaleY, textSize;
	private float[] orthoMatrix = new float[16];
	private float[] modelMatrix = new float[16];
	private float[] translationMatrix = new float[16];
    private GL10 gl;
    private GLSurfaceView glView;
    private int highScore;
    private int[] soundIds, texturenames;
    private List<Drawn> effectsToDraw, socksToDraw;
    private SockGame sockGame;
    private SoundPool soundPool;
    private TextureSquare bg;
    private TextureSquare[] effects;
	private TextureSquare[] socks;
	private TextureSquare[] items;
    private TextView itemTextView, timeTextView, scoreTextView;

    /**
     * Constructor
     * Initializes glView and  loads GL textured drawables
     * @param game SockMatcherGame main activity
     */
	public GameScreen(SockMatcherGame game) {
		super(game);
		mContext = game.getContext();

        //Init glView
        glView = new GLGameSurfaceView(mContext);
        glView.setPreserveEGLContextOnPause(true);  //Save GL content when view is paused
        glView.setRenderer(this);

        //Load texture objects
        effects = loadTextureSquares(160.0f, 216.0f, 0.5f, 2, 3, 3);
        socks = loadTextureSquares(160.0f, 216.0f, 0.2f, 5, 24, 1);
        items = loadTextureSquares(200.0f, 200.0f, 0.5f, 2, 3, 2);
        bg = loadBackground();

        scaleX = game.getScaleX();
        scaleY = game.getScaleY();

        //Set sound objects to those initialized in main activity
        soundPool = game.getSounds();
        soundIds = game.getSoundIds();

        textSize = game.getTextSize();

        game.setContentView(glView);
        glView.onPause();
	}

    /**
     * Prepares the view to appear on screen; initializes SockGame
     */
    public void prepareToAppear() {
        soundOn = game.getSoundOn();
        highScore = game.getHighScore();
        sockGame = new SockGame(this);
        this.effectsToDraw = new ArrayList<Drawn>();
        this.socksToDraw = new ArrayList<Drawn>();
        glView.onResume();
    }

    /**
     * Returns the initial view for the screen, which is the Ready View
     * @return View Ready View
     */
    public View getInitialView() {
        return getReadyView();
    }

    /**
     * Changes game state of GameScreen to enumerated value passed as argument; also removes current
     * overlay view from main activity and replaces it with the one corresponding to the new state
     * @param gameState GameState enumerated value representing state to change to
     */
    public void changeGameState(GameState gameState) {
        game.removeOverlayView();

        switch (gameState) {
            case Ready:
                game.setOverlayView(getReadyView());
                glView.setOnTouchListener(null);
                break;
            case Running:
                game.setOverlayView(getRunningView());
                glView.setOnTouchListener(this);  //Add touchListener only when game is running
                break;
            case Paused:
                game.setOverlayView(getPausedView());
                glView.setOnTouchListener(null);
                break;
            case GameOver:
                game.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        game.setOverlayView(getGameOverView());
                    }
                });
                glView.setOnTouchListener(null);
                break;
            default:
        }

        this.state = gameState;
    }

    /**
     * Prepares and returns Ready View for game
     * @return View ready view
     */
    public View getReadyView() {
        View readyView = game.getLayoutInflater().inflate(R.layout.tap_to_start_layout, null);
        ((TextView)readyView.findViewById(R.id.tapToStartLabel)).setTextSize(textSize);
        readyView.setOnClickListener(new readyOverlayOnClickListener());
        return readyView;
    }

    /**
     * Prepares and returns Running View for game; sets class variables for textviews for in-game
     * manipulation
     * @return View Running View
     */
    public View getRunningView() {
        View runningView = game.getLayoutInflater().inflate
                (R.layout.game_running_overlay, null);

        //timeTextView
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int)(1080/scaleX);
        layoutParams.topMargin = 0;
        layoutParams.width = (int)(200/scaleX);
        layoutParams.height = (int)(200/scaleY);
        timeTextView = (TextView)runningView.findViewById(R.id.timeTextView);
        timeTextView.setLayoutParams(layoutParams);
        timeTextView.setTextSize(textSize);

        //scoreTextView
        layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int)(1080/scaleX);
        layoutParams.topMargin = (int)(200/scaleY);
        layoutParams.width = (int)(200/scaleX);
        layoutParams.height = (int)(200/scaleY);
        scoreTextView = (TextView)runningView.findViewById(R.id.scoreTextView);
        scoreTextView.setLayoutParams(layoutParams);
        scoreTextView.setTextSize(textSize);

        //itemTextView
        layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = (int)(160/scaleY);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        itemTextView = (TextView)runningView.findViewById(R.id.itemTextView);
        itemTextView.setLayoutParams(layoutParams);
        itemTextView.setTextSize(textSize);

        return runningView;
    }

    /**
     * Prepares and returns Paused View
     * @return View paused view
     */
    public View getPausedView() {
        View pausedView = game.getLayoutInflater().inflate(R.layout.game_paused_overlay, null);

        //Resume Button
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.width = (int)(440/scaleX);
        layoutParams.height = (int)(300/scaleY);
        layoutParams.leftMargin = (int)(100/scaleX);
        layoutParams.topMargin = (int)(250/scaleY);
        View childView = pausedView.findViewById(R.id.resumeButton);
        childView.setLayoutParams(layoutParams);
        childView.setOnClickListener(new resumeButtonOnClickListener());

        //Main Menu Button
        layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.width = (int)(440/scaleX);
        layoutParams.height = (int)(300/scaleY);
        layoutParams.leftMargin = (int)(740/scaleX);
        layoutParams.topMargin = (int)(250/scaleY);
        childView = pausedView.findViewById(R.id.mainMenuButton);
        childView.setLayoutParams(layoutParams);
        childView.setOnClickListener(new mainMenuButtonOnClickListener());

        //"Game Paused" TextView
        layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = (int)(100/scaleY);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        childView = pausedView.findViewById(R.id.gamePausedTextView);
        childView.setLayoutParams(layoutParams);
        ((TextView)childView).setTextSize(textSize);

        return pausedView;
    }

    /**
     * Prepares and returns game over view
     * @return View game over view
     */
    public View getGameOverView() {
        View gameOverView = game.getLayoutInflater().inflate(R.layout.game_over_overlay, null);

        //Play Again Button
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.width = (int)(440/scaleX);
        layoutParams.height = (int)(300/scaleY);
        layoutParams.leftMargin = (int)(100/scaleX);
        layoutParams.topMargin = (int)(250/scaleY);
        View childView = gameOverView.findViewById(R.id.playAgainButton);
        childView.setLayoutParams(layoutParams);
        childView.setOnClickListener(new playAgainButtonOnClickListener());

        //Main Menu Button
        layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.width = (int)(440/scaleX);
        layoutParams.height = (int)(300/scaleY);
        layoutParams.leftMargin = (int)(740/scaleX);
        layoutParams.topMargin = (int)(250/scaleY);
        childView = gameOverView.findViewById(R.id.gameOverMainMenuButton);
        childView.setLayoutParams(layoutParams);
        childView.setOnClickListener(new mainMenuButtonOnClickListener());

        //Game Over Text View
        layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = (int)(50/scaleY);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        childView = gameOverView.findViewById(R.id.gameOverTextView);
        childView.setLayoutParams(layoutParams);
        ((TextView)childView).setTextSize(textSize);

        //Final Score Text View
        layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = (int)(150/scaleY);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        childView = gameOverView.findViewById(R.id.finalScoreTextView);
        childView.setLayoutParams(layoutParams);
        ((TextView)childView).setTextSize(textSize);

        //If new high score, persists new high score through main activity and displays
        //corresponding text
        if (sockGame.getNewHighScore()) {
            ((TextView)gameOverView.findViewById(R.id.finalScoreTextView)).setText(
                    "New High Score: " + Integer.toString(sockGame.getScore()));
            game.setHighScore(sockGame.getScore());
        } else {
            ((TextView)gameOverView.findViewById(R.id.finalScoreTextView)).setText(
                    "Final Score: " + Integer.toString(sockGame.getScore()));
        }

        return gameOverView;
    }

    /**
     * GLSurfaceView.Renderer called when surface created; sets background to black, loads shaders,
     * and loads textures
     * @param gl10 GL10
     * @param eglConfig EGLConfig
     */
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        gl = gl10;
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        loadShaders();
        loadTextures();
    }

    /**
     * GLSurfaceView.Renderer called when surface changes; sets viewport and orthogonic matrix
     * @param gl10 GL10
     * @param width int width
     * @param height int height
     */
    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Matrix.orthoM(orthoMatrix, 0, 0, 1280, 0, 800, -1, 1);
    }

    /**
     * GLSurfaceView.Renderer called repeatedly; calls update and present methods
     * @param gl10
     */
    @Override
    public void onDrawFrame(GL10 gl10) {
        update();
        present();
    }

    /**
     * If game is running, updates UI TextViews, sock array, and effects array to match SockGame
     * model
     */
    public void update() {
        if (state == GameState.Running) {
            game.setTextInTextView(itemTextView, sockGame.getItemString());
            game.setTextInTextView(this.timeTextView, Integer.toString(this.sockGame.getTime()));
            game.setTextInTextView(this.scoreTextView, Integer.toString(this.sockGame.getScore()));
            this.socksToDraw.clear();
            this.socksToDraw.addAll(this.sockGame.getSocksToDraw());
            this.effectsToDraw.clear();
            this.effectsToDraw.addAll(this.sockGame.getEffectsToDraw());
        }
    }

    /**
     * Draws all GL drawable objects
     */
    public void present() {
        if (state == GameState.Running) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            bg.draw(orthoMatrix);  //Draw background image

            //Draws all visible socks in corresponding positions
            for (Drawn sock : this.socksToDraw) {
                Matrix.setIdentityM(modelMatrix, 0);
                Matrix.translateM(modelMatrix, 0, sock.getX(),
                        640 - sock.getY(), 0f);
                Matrix.multiplyMM(translationMatrix, 0, orthoMatrix, 0, modelMatrix, 0);
                socks[sock.getVal()].draw(translationMatrix);
            }

            //Draws all visible effects in corresponding positions
            for (Drawn effect : this.effectsToDraw) {
                Matrix.setIdentityM(modelMatrix, 0);
                Matrix.translateM(modelMatrix, 0, effect.getX(),
                        640 - effect.getY(), 0f);
                Matrix.multiplyMM(translationMatrix, 0, orthoMatrix, 0, modelMatrix, 0);
                effects[effect.getVal()].draw(translationMatrix);
            }

            //If player has an item, draws item in corresponding position
            if (this.sockGame.getHasItem()) {
                Drawn item = this.sockGame.getItem();
                Matrix.setIdentityM(modelMatrix, 0);
                Matrix.translateM(modelMatrix, 0, item.getX(),
                        600 - item.getY(), 0f);
                Matrix.translateM(modelMatrix, 0, 100, 100, 0f);
                Matrix.rotateM(modelMatrix, 0, this.sockGame.getItemRot(), 0f, 0f,
                        1f);
                Matrix.translateM(modelMatrix, 0, -100, -100, 0f);
                Matrix.multiplyMM(translationMatrix, 0, orthoMatrix, 0, modelMatrix,
                        0);
                items[item.getVal()].draw(translationMatrix);
            }

            //If player is holding a sock, draws sock on top of all other objects
            if (sockGame.getIsSelected() && sockGame.getSelected() == SockGame.Selection.SOCK) {
                Matrix.setIdentityM(modelMatrix, 0);
                Matrix.translateM(modelMatrix, 0, sockGame.getSelectedSock().getX(),
                        640 - sockGame.getSelectedSock().getY(), 0f);
                Matrix.multiplyMM(translationMatrix, 0, orthoMatrix, 0, modelMatrix, 0);
                socks[sockGame.getSelectedSock().getVal()].draw(translationMatrix);
            }
        }
    }

    /**
     * Called when game is paused; if game is running, pauses game; paused GLView
     */
	@Override
	public void pause() {
		// TODO Auto-generated method stub
        if (state == GameState.Running) {
            sockGame.pause();
        }

        glView.onPause();
	}

    /**
     * Called when game resumes; resumes GLView
     */
	@Override
	public void resume() {
		// TODO Auto-generated method stub
        glView.onResume();
	}

    /**
     * Called when game is destroyed; calls tearDownGL method
     */
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
        tearDownGL();
	}

    /**
     * Called when back button is pressed; is game running, pauses game
     */
    @Override
    public void onBackPressed() {
        if (state == GameState.Running) {
            sockGame.pause();
        }
    }

    /**
     * Deletes GL textures
     */
    public void tearDownGL()
    {
        if (texturenames != null)
            gl.glDeleteTextures(4, IntBuffer.wrap(texturenames));
    }

    /**
     * Handles user touch input, multiplies by scale, and forwards input to game model
     * @param view View that user input is being sent to
     * @param motionEvent MotionEvent representing user input
     * @return true
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (state == GameState.Running) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    sockGame.handleTouchDown(((int) (motionEvent.getX()*scaleX)),
                            ((int) (motionEvent.getY()*scaleY)));
                    break;
                case MotionEvent.ACTION_MOVE:
                    sockGame.handleTouchDragged(((int) (motionEvent.getX()*scaleX)),
                            ((int) (motionEvent.getY()*scaleY)));
                    break;
                case MotionEvent.ACTION_UP:
                    sockGame.handleTouchUp(((int) (motionEvent.getX()*scaleX)),
                            ((int) (motionEvent.getY()*scaleY)));
                    break;
                default:
            }  //end switch getAction
        }

        return true;
    }

    /**
     * Loads and returns an array of TextureSquare objects based on parameters passed
     * @param height float height of square in world coordinates
     * @param width float width of square in world coordinates
     * @param textDim float percent of texture atlas that each object takes up
     * @param numOfCols int number of columns in texture atlas
     * @param numOfSquares int number of TextureSquares to make
     * @param textureId int id of texture in GL Textures
     * @return TextureSquare[] arrays of TextureSquare objects
     */
    private TextureSquare[] loadTextureSquares(float height, float width, float textDim,
                                               int numOfCols, int numOfSquares, int textureId) {
        float[] uvs = new float[8];
        float[] squareCoords = {
                0f, height, 0.0f,
                0f, 0f, 0.0f,
                width, 0f, 0.0f,
                width, height, 0.0f
        };

        TextureSquare[] textureSquares = new TextureSquare[numOfSquares];  //Init array
        float row, col, u, u2, v, v2;

        for (int i = 0; i < textureSquares.length; i++) {
            row = i / numOfCols;
            col = i % numOfCols;

            u = col * textDim;
            u2 = u + textDim;
            v = row * textDim;
            v2 = v + textDim;

            uvs[0] = uvs[2] = u;
            uvs[1] = uvs[7] = v;
            uvs[3] = uvs[5] = v2;
            uvs[4] = uvs[6] = u2;

            //Init TextureSquare
            textureSquares[i] = new TextureSquare(uvs, squareCoords, textureId);
        }

        return textureSquares;
    }

    /**
     * Loads background TextureSquare
     * @return TextureSquare background TextureSquare
     */
    private TextureSquare loadBackground() {
        float[] squareCoords = {
                0f, 800f, 0.0f,
                0f, 0f, 0.0f,
                1280f, 0f, 0.0f,
                1280f, 800f, 0.0f
        };
        float[] uvs =  {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };

        return new TextureSquare(uvs, squareCoords, 0);
    }

    /**
     * Checks to see if passed coordinates are in passed boundaries
     * @param x int x coordinate to check
     * @param y int y coordinate to check
     * @param left int left boundary
     * @param right int right boundary
     * @param top int top boundary
     * @param bottom int bottom boundary
     * @return true if passed x and y are in boundaries; false otherwise
     */
	private boolean inBounds(int x, int y, int left, int right, int top,
			int bottom) {
		return x > left && x < right && y > top && y < bottom;
	}

    /**
     * Loads Textures into texturenames array
     */
    private void loadTextures() {
        texturenames = new int[4];
        GLES20.glGenTextures(4, texturenames, 0);

        //Load background texture
        int id = mContext.getResources().getIdentifier("drawable/background",
                null, mContext.getPackageName());
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), id);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
        bmp.recycle();

        //Load sock texture atlas
        id = mContext.getResources().getIdentifier("drawable/sockatlas",
                null, mContext.getPackageName());
        bmp = BitmapFactory.decodeResource(mContext.getResources(), id);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + 1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[1]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
        bmp.recycle();

        //Load item texture atlas
        id = mContext.getResources().getIdentifier("drawable/itematlas",
                null, mContext.getPackageName());
        bmp = BitmapFactory.decodeResource(mContext.getResources(), id);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + 2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[2]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
        bmp.recycle();

        //Load effects texture atlas
        id = mContext.getResources().getIdentifier("drawable/effectatlas", null,
                mContext.getPackageName());
        bmp = BitmapFactory.decodeResource(mContext.getResources(), id);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + 3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[3]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
        bmp.recycle();
    }

    /**
     * Loads texture shader
     */
    private void loadShaders() {
        int vertexShader;
        int fragmentShader;

        //Load texture shader
        vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER,
                riGraphicTools.vs_Image);
        fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER,
                riGraphicTools.fs_Image);
        riGraphicTools.textureProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(riGraphicTools.textureProgram, vertexShader);
        GLES20.glAttachShader(riGraphicTools.textureProgram, fragmentShader);
        GLES20.glLinkProgram(riGraphicTools.textureProgram);
    }

    /**
     * Public accessor for highScore variable
     * @return int highScore
     */
    public final int getHighScore() {
        return this.highScore;
    }

    /**
     * Public accessor for soundOn variable
     * @return boolean soundOn
     */
    public final boolean getSoundOn() {
        return soundOn;
    }

    /**
     * Public accessor for soundPool variable
     * @return SoundPool soundPool
     */
    public final SoundPool getSoundPool() {
        return soundPool;
    }

    /**
     * Public accessor for soundIds array
     * @return int[] soundIds
     */
    public final int[] getSoundIds() {
        return soundIds;
    }

    /**
     * Inner class GLGameSurfaceView extends GLSurfaceView
     */
    class GLGameSurfaceView extends GLSurfaceView {
        /**
         * Constructor sets Context and OpenGL version number
         * @param context Context to set
         */
        public GLGameSurfaceView(Context context) {
            super(context);
            setEGLContextClientVersion(2);
        }
    }

    /**
     * Inner class readyOverlayOnClickListener implements View.OnClickListener
     * Used to for when user presses Ready View to start game
     */
    class readyOverlayOnClickListener implements View.OnClickListener {
        /**
         * Starts SockGame model and changes screen state to running
         * @param view View sender
         */
        @Override
        public void onClick(View view) {
            sockGame.start();
            changeGameState(GameState.Running);
        }
    }

    /**
     * Inner class resumeButtonOnClickListener implements View.OnClickListener
     * Used to for when user presses resume button on Paused View
     */
    class resumeButtonOnClickListener implements View.OnClickListener {
        /**
         * Resume SockGame model and sets screen state to Running
         * @param view View sender
         */
        @Override
        public void onClick(View view) {
            sockGame.resume();
            changeGameState(GameState.Running);
        }
    }

    /**
     * Inner class mainMenuButtonOnClickListener implements View.OnClickListener
     * Used to for when user presses main menu button on both Paused View and Game Over View
     */
    class mainMenuButtonOnClickListener implements View.OnClickListener {
        /**
         * Pauses GLView and changes overlay view from GameScreen to MainMenuScreen
         * @param view View sender
         */
        @Override
        public void onClick(View view) {
            glView.onPause();
            game.setScreen(SockMatcherGame.GameScreenType.MAIN_MENU_SCREEN);
        }
    }

    /**
     * Inner class playAgainButtonOnClickListener implements View.OnClickListener
     * Used to for when user presses play again button on Game Over View
     */
    class playAgainButtonOnClickListener implements View.OnClickListener {
        /**
         * Resets SockGame model and changes screen state to Ready
         * @param view View sender
         */
        @Override
        public void onClick(View view) {
            sockGame.reset();
            changeGameState(GameState.Ready);
        }
    }
}