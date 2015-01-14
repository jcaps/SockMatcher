/*
 * SockGame.java
 * Game model for Sock Matcher
 * Copyright Joseph M. Caplan Dec. 28, 2014
 */
package com.penguin.sockmatcher;

import android.media.SoundPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.penguin.sockmatcher.GameScreen.GameState;

public class SockGame {
	enum Selection {
		SOCK, ITEM
	}
	
	GameScreen gs;
	Selection selected;

    private boolean hasItem, isSelected, newHighScore, soundOn;
	private int cols, fireballCountdown, halfHeight, halfWidth, highScore, newSockCountdown,
            numDiffSocks, points, score, size, sockHeight, sockWidth, time;
    private int[] soundIds;
	private Drawn item;
    private Drawn[] effects;
    private float itemRot;
    private List<Drawn> effectsToDraw, socksToDraw, validSocks;
	private List<Node> neighbors, newNeighbors;
    private ScheduledFuture fireballCountdownScheduledFuture, fireballSpinnerScheduledFuture,
            sockAdderScheduledFuture, timerScheduledFuture;
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private Sock selectedSock;
	private Sock[] socks;
    private SoundPool soundPool;
    private String itemString;

    /**
     * Constructor
     * @param gs GameScreen running SockGame
     */
	public SockGame(GameScreen gs) {
		this.gs = gs;

        cols = 5;
		hasItem = false;
        highScore = gs.getHighScore();
        isSelected = false;
        item = new Drawn(1080, 400, 0, 1080, 400);
        itemRot = 0;
        itemString = "";
		newHighScore = false;
        newSockCountdown = 15;
        numDiffSocks = 15;
		points = 0;
		score = 0;
        size = 25;
        sockHeight = 160;
        sockWidth = 216;
        halfHeight = sockHeight/2;
        halfWidth = sockWidth/2;
		time = 60;
		neighbors = new ArrayList<Node>();
		newNeighbors = new ArrayList<Node>();
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(20);
        effectsToDraw = new CopyOnWriteArrayList<Drawn>();
        socksToDraw = new CopyOnWriteArrayList<Drawn>();
        soundIds = gs.getSoundIds();
        soundOn = gs.getSoundOn();
        soundPool = gs.getSoundPool();

        initSockAndEffectArrays();
	}

    /**
     * Initializes Socks and Effects
     * Precondition: sockWidth, sockHeight, and numDiffSocks are initialized
     * Post-condition: socks and effects are initialized as arrays of Sock objects and Drawn objects
     *   respectively
     */
    private void initSockAndEffectArrays() {
        int n, x, y, xPos, yPos;

        socks = new Sock[size];
        effects = new Drawn[size];

        //Loops through size of game board and initializes each object
        for (int i = 0; i < 25; i++) {
            x = i%5;
            y = i/5;
            xPos = x*sockWidth;
            yPos = y*sockHeight;
            socks[i] = new Sock(i, (int)(Math.random()*numDiffSocks), xPos, yPos);
            effects[i] = new Drawn(xPos, yPos, 0, xPos, yPos);

            //If not at top of board, add neighbor from previous row
            if (y > 0) {
                n = i - 5;
                socks[i].addNeighbor(socks[n]);
                socks[n].addNeighbor(socks[i]);

                //If not at left edge of board, add top left corner neighbor from previous row
                if (x > 0) {
                    n =  i - 5 - 1;
                    socks[i].addCorner(socks[n]);
                    socks[n].addCorner(socks[i]);
                }

                //If nt at far right ofr board, add top right corner neighbor from previous row
                if (x < 5-1) {
                    n = i - 5 + 1;
                    socks[i].addCorner(socks[n]);
                    socks[n].addCorner(socks[i]);
                }
            }

            //If not at far left of board, add neighbor from previous node
            if (x > 0) {
                n = i - 1;
                socks[i].addNeighbor(socks[n]);
                socks[n].addNeighbor(socks[i]);
            }
        }
    }

    //State-changing methods

    /**
     * Adds all Socks to validSocks and socksToDraw and starts both the timer and the countdown to
     * the next type of sock being added
     * Precondition: socks and scheduledThreadPoolExecutor have been initialized
     * Post-condition: validSocks and socksToDraw are initialized with all of the Socks in socks;
     *   timerScheduledFuture and sockAdderScheduledFuture are scheduled and running
     */
	public void start() {
        validSocks = new CopyOnWriteArrayList<Drawn>(Arrays.asList((Drawn[])socks));
        socksToDraw = new CopyOnWriteArrayList<Drawn>(Arrays.asList((Drawn[])socks));
        timerScheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(
                new TimerRunnable(), 0, 1, TimeUnit.SECONDS);
        sockAdderScheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(
                new SockAdderRunnable(), 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Resumes a game that has been paused
     * Precondition: SockGame has already been initialized, started, and paused
     * Post-condition: Main game timer running; sockAdder timer and fireball spinner running if
     *   applicable
     */
    public void resume() {
        //Resume game timer
        timerScheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(
                new TimerRunnable(), 0, 1, TimeUnit.SECONDS);

        //If number of different socks less than maximum, resume sock adder timer
        if (numDiffSocks < 24)
            sockAdderScheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(
                    new SockAdderRunnable(), 0, 1, TimeUnit.SECONDS);

        //If player has item and item is fireball, resume fireball spinner
        if (hasItem && item.val == 2)
            fireballSpinnerScheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(
                    new FireballSpinnerRunnable(), 0, 25, TimeUnit.MILLISECONDS);
    }

    /**
     * Pauses a running game
     * Precondition: timerScheduledFuture and sockAdderScheduledFuture have been instantiated
     * Post-condition: timerScheduledFuture and sockAdderScheduledFuture are cancelled; if running,
     *   fireballSpinnerScheduledFuture is cancelled; if player is holding an object, object is
     *   released and returned to based position; GameScreen state is set to paused
     */
    public void pause() {
        //Cancel main game timer and sock adder timer
        timerScheduledFuture.cancel(false);
        sockAdderScheduledFuture.cancel(false);

        //If fireball spinner is running, cancel
        if (fireballSpinnerScheduledFuture != null)
            fireballSpinnerScheduledFuture.cancel(false);

        //Change gameScreen state to paused
        gs.changeGameState(GameState.Paused);

        //If player is holding an object, release object and return to base
        if (isSelected) {
            switch (selected) {
                case SOCK:
                    selectedSock.returnToBase();
                    selectedSock = null;
                    break;
                case ITEM:
                    item.returnToBase();
                    break;
                default:
            }

            isSelected = false;
        }
    }

    /**
     * Ends game
     * Precondition: SockGame has been initialized
     * Post-condition: all runnables are cancelled; high score is updated if reached; gameScreen
     *   state is changed to GameOver
     */
    public void endGame() {
        //Cancel all running runnables
        timerScheduledFuture.cancel(false);
        sockAdderScheduledFuture.cancel(false);
        if (fireballSpinnerScheduledFuture != null)
            fireballSpinnerScheduledFuture.cancel(false);

        //Player has set new high score
        newHighScore = score > highScore;

        if (newHighScore) {
            highScore = score;
        }

        gs.changeGameState(GameState.GameOver);
    }

    /**
     * Resets game to prepare for new game
     * Precondition: SockGame has been initialized
     * Post-condition: SockGame has been reset to beginning settings
     */
    public void reset() {
        numDiffSocks = 15;
        //Reset all Socks to new random values, unvisit, and returns to base position
        for (Sock sock : socks) {
            sock.setVal((int) (Math.random() * numDiffSocks));
            sock.unvisit();
            sock.returnToBase();
        }
        item.returnToBase();
        hasItem = false;
        isSelected = false;
        score = 0;
        time = 60;
        newSockCountdown = 15;
        selectedSock = null;
    }

    //Touch Handler Methods

    /**
     * Handles a user's touch down gesture
     * @param x int x coord of touch down gesture
     * @param y int y coord of touch down gesture
     */
	public void handleTouchDown(int x, int y) {
        //Only responds to touch down if user has no object selected
        if (!isSelected) {
            //If in sock area
            if (inBounds(x, y, 0, 1080, 0, 800)) {
                //Retrieve sock from coordinates of touch
                Sock sock = getSockFromXAndY(x, y);

                //If sock is valid, select
                if (this.validSocks.contains(sock)) {
                    selectSock(sock, x, y);
                }
            }  //End if in sock area

            //If player has item and touches in item area, select item
            else if (this.hasItem && inBounds(x, y, 1080, 1280, 400, 600)) {
                selectItem(x, y);
            }

            //If player touches pause button
            else if (inBounds(x, y, 1080, 1280, 600, 800))
                pause();
        }  //End if not selected
	}

    /**
     * Handles a user's touch dragged gesture
     * @param x int x coord of touch dragged gesture
     * @param y int y coord of touch dragged gesture
     */
	public void handleTouchDragged(int x, int y) {
        //Only responds if user drags while holding an object
        if (isSelected) {
            //Switch statement alters behavior depending on whether a sock or item is selected
            switch (selected) {
                //If sock selected, update position to user's touch
                case SOCK:
                    selectedSock.setXY(x - halfWidth, y - halfHeight);
                    break;
                //If item selected, update item position to user's touch
                case ITEM:
                    item.setXY(x - 100, y - 100);

                    //If user has fireball, burn highlighted sock
                    if (item.getVal() == 2) {
                        if (inBounds(x, y, 0, 1080, 0, 800)) {
                            Sock sock = getSockFromXAndY(x, y);

                            if (validSocks.contains(sock)) {
                                validSocks.remove(sock);
                                scheduledThreadPoolExecutor.execute(new SockBurnerRunnable(sock));
                            }
                        }
                    }
            }
        }
	}

    /**
     * Handles user's touch up gesture
     * @param x int x coord of touch up gesture
     * @param y int y coord of touch up gesture
     */
	public void handleTouchUp(int x, int y) {
        //Check if user has object selected
        if (isSelected) {
            //Switch whether user is holding sock or item
            switch (selected) {
                case SOCK:
                    boolean matchFound = false;

                    //Load with four inner corners of selected sock
                    int[] xys = {
                            this.selectedSock.getX() + 20,
                            this.selectedSock.getY() + 20,
                            this.selectedSock.getX() + 196,
                            this.selectedSock.getY() + 20,
                            this.selectedSock.getX() + 20,
                            this.selectedSock.getY() + 140,
                            this.selectedSock.getX() + 196,
                            this.selectedSock.getY() + 140};

                    //Check all four corners for a match
                    for (int i = 0; i < xys.length - 1; i += 2) {
                        //Check if corner is in sock area
                        if (inBounds(xys[i], xys[i+1], 0, 1080, 0, 800)) {
                            Sock sock = getSockFromXAndY(xys[i], xys[i + 1]);

                            //If highlighted sock is valid and matches, match socks and break loop
                            if (this.validSocks.contains(sock) && this.selectedSock.matches(sock)) {
                                matchFound = true;
                                matchSocks(this.selectedSock, sock);
                                break;
                            }
                        }  //end if in sock area
                    }

                    //If no match if found, return sock to valid and visible
                    if (!matchFound) {
                        this.validSocks.add(selectedSock);
                        this.socksToDraw.add(selectedSock);
                    }

                    //Return sock to base position
                    this.selectedSock.returnToBase();
                    break;
                case ITEM:
                    //Switch statement for item type
                    switch (item.getVal()) {
                        case 0: //Bleach
                            if (inBounds(x, y, 0, 1080, 0, 800)) {
                                Sock sock = getSockFromXAndY(x, y);

                                //If released on valid sock, bleach socks
                                if (this.validSocks.contains(sock)) {
                                    scheduledThreadPoolExecutor.execute(
                                            new SockBleacherRunnable(sock));
                                    hasItem = false;
                                }  //end if released on valid sock
                            }
                            break;
                        case 1: //Sock Remover
                            if (inBounds(x, y, 0, 1080, 0, 800)) {
                                Sock sock = getSockFromXAndY(x, y);

                                //If released on valid sock use sock remover
                                if (validSocks.contains(sock)) {
                                    scheduledThreadPoolExecutor.execute(
                                            new SockRemoverRunnable(sock));
                                    hasItem = false;
                                }
                            }
                            break;
                        case 2: //Fireball
                            //If fireball countdown is running, cancel
                            if (fireballCountdownScheduledFuture != null) {
                                fireballCountdownScheduledFuture.cancel(false);
                                fireballCountdownScheduledFuture = null;
                            }
                            break;
                        default:
                            System.out.println("Inavlid Item Type");
                    }  //end item type switch statement

                    item.returnToBase();  //Reset item position to base position
                    break;
                default:
                    System.out.println("Invalid selected type");
            }  //End selected switch statement

            isSelected = false;
        }
	}  //end method handleTouchUp()

    //Utility Methods

    /**
     * Sets all socks to unvisited
     * Precondition: socks is initialized
     * Post-condition: all socks' visited attribute are set to false
     */
    private void unvisitNodes() {
        for (Sock sock : this.socks)
            sock.unvisit();
    }

    /**
     * Checks to see if passed coordinates fall in specified area
     * @param x int x coord to check
     * @param y int y coord to check
     * @param left int left boundary
     * @param right int right boundary
     * @param top int top boundary
     * @param bottom int bottom boundary
     * @return true if passed x and y coordinates are in specified boundaries; false otherwise
     */
	private boolean inBounds(int x, int y, int left, int right, int top,
			int bottom) {
		return x > left && x < right && y > top && y < bottom;
	}

    /**
     * Determines sock from x and y coordinates
     * @param x int x coord
     * @param y int y coord
     * @return Sock determined by position
     */
    private Sock getSockFromXAndY(int x, int y) {
        return socks[(cols * (y/sockHeight) + (x/sockWidth))];
    }

    /**
     * Selects Sock and updates position based on x and y coordinates
     * @param sock Sock to be selected
     * @param x int x coordinate
     * @param y int y coordinate
     */
	private void selectSock(Sock sock, int x, int y) {
        if (soundOn)                                       //If player has sound on,
            soundPool.play(soundIds[4], 1f, 1f, 1, 0, 1f); //play select sock (pop) noise

		validSocks.remove(sock);               //Remove selected sock from valid socks
		socksToDraw.remove(sock);              //Remove from socks to draw
		sock.setXY(x-halfWidth, y-halfHeight); //Move to touched coordinates
		isSelected = true;
		selectedSock = sock;
		selected = Selection.SOCK;
	}

    /**
     * Selects item and updates to x and y coordinates
     * @param x int x coordinate
     * @param y int y coordinate
     */
	private void selectItem(int x, int y) {
		item.setXY(x-100,y-100);  //Update position to touch coordinates
		isSelected = true;
		selected = Selection.ITEM;

        //If fireball selected, start countdown
        if (item.getVal() == 2) {
            fireballCountdownScheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(
                    new FireballCountdownRunnable(), 0, 1, TimeUnit.SECONDS);
        }
	}

    /**
     * Matches two socks and preforms a breadth-first traversal to match any socks surrounding the
     * second sock; adds appropriate item, points, and time if necessary
     * @param sock Sock selected sock to match
     * @param otherSock Sock highlighted sock to match
     */
	private void matchSocks(Sock sock, Sock otherSock) {
  		points = 2;
        int val = sock.getVal();
        List<Sock> socksToFill = new LinkedList<Sock>();

        this.validSocks.remove(otherSock);
        this.socksToDraw.remove(otherSock);

        socksToFill.add(sock);
        socksToFill.add(otherSock);

        sock.visit();
        otherSock.visit();

        this.neighbors.addAll(otherSock.getNeighbors());
		
		//While neighbors i not empty, while loop loops visiting unvisited
		//neighbors checking for matches and adding potential new neighbors
		while (!neighbors.isEmpty()) {
			for (Node nod : neighbors) {
				if (!nod.isVisited()) {
					nod.visit();
                    Sock neighborSock = (Sock)nod;

                    //If neighbor sock is valid and matches
                    if (this.validSocks.contains(neighborSock) && neighborSock.matches(otherSock)) {
                        this.validSocks.remove(neighborSock);
                        this.socksToDraw.remove(neighborSock);
                        socksToFill.add(neighborSock);

                        newNeighbors.addAll(neighborSock.getNeighbors());

                        points++;
                    }
				}  //end if node unvisited
			}  //end for nodes in neighbors
			
			neighbors.clear();
			
			if (!newNeighbors.isEmpty()) {
				neighbors.addAll(newNeighbors);
				newNeighbors.clear();
			}
				
		}  //end while neighbors not empty

        newNeighbors.clear();
        unvisitNodes();

        //Start sock replenisher
        scheduledThreadPoolExecutor.schedule(new SockReplenisher(socksToFill), 250,
                TimeUnit.MILLISECONDS);

        //Bleach
        if (val == 11) {
            if (soundOn)
                soundPool.play(soundIds[0], 1f, 1f, 0, 0, 1f);

            if (fireballSpinnerScheduledFuture != null) {
                fireballSpinnerScheduledFuture.cancel(true);
                fireballSpinnerScheduledFuture = null;
            }
            itemRot = 0f;
            item.setVal(0);
            hasItem = true;

            itemString = "Bleach";
            scheduledThreadPoolExecutor.schedule(new ItemStringRemoverRunnable(), 1,
                    TimeUnit.SECONDS);
        }

        //Sock Remover
        else if (val == 12) {
            if (soundOn)
                soundPool.play(soundIds[5], 1f, 1f, 0, 0, 1f);

            if (fireballSpinnerScheduledFuture != null) {
                fireballSpinnerScheduledFuture.cancel(true);
                fireballSpinnerScheduledFuture = null;
            }
            itemRot = 0f;
            item.setVal(1);
            hasItem = true;

            itemString = "Sock Remover";
            scheduledThreadPoolExecutor.schedule(new ItemStringRemoverRunnable(), 1,
                    TimeUnit.SECONDS);
        }

        //Fireball
        else if (val == 13) {
            if (soundOn)
                soundPool.play(soundIds[3], 1f, 1f, 0, 0, 1f);

            if (fireballSpinnerScheduledFuture != null) {
                fireballSpinnerScheduledFuture.cancel(true);
                fireballSpinnerScheduledFuture = null;
            }
            fireballCountdown = 5;
            item.setVal(2);
            fireballSpinnerScheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(
                    new FireballSpinnerRunnable(), 0, 25, TimeUnit.MILLISECONDS);
            hasItem = true;

            itemString = "Fireball";
            scheduledThreadPoolExecutor.schedule(new ItemStringRemoverRunnable(), 1,
                    TimeUnit.SECONDS);
        }

        //Time
        else if (val == 14) {
            this.time += points*5;
        }

		score += points;
	}  //end method matchSocks()

    //Inner class runnables

    /**
     * Countdowns fireball time while user is using fireball
     */
    class FireballCountdownRunnable implements Runnable {
        /**
         * Decrements fireball countdown time by 1; if firebal countdown time reaches zero, removes
         * fireball, cancels timer, cancels spinner, and sets item rotation to 0
         */
        @Override
        public void run() {
            //Decrements fireball countdown time and checks to see if below 1
            if (--fireballCountdown < 1) {
                //If user has item and item is fireball
                if (hasItem && item.getVal() == 2) {
                    //If fireball countdown timer is running, cancel
                    if (fireballCountdownScheduledFuture != null) {
                        fireballCountdownScheduledFuture.cancel(false);
                        fireballCountdownScheduledFuture = null;
                    }
                    hasItem = false;

                    //If fireball spinner is running, cancel
                    if (fireballSpinnerScheduledFuture != null) {
                        fireballSpinnerScheduledFuture.cancel(true);
                        fireballSpinnerScheduledFuture = null;
                    }
                    itemRot = 0.0f;

                    //If user has item selected, set selected to false and return item to base
                    if (isSelected && selected == Selection.ITEM) {
                        isSelected = false;
                        item.returnToBase();
                    }
                }
            }
        }
    }

    /**
     * Spins fireball
     */
    class FireballSpinnerRunnable implements Runnable {
        /**
         * Rotates item by -10 degrees; if item reaches -360 degrees rotation, resets to 0
         */
        @Override
        public void run() {
            if ((SockGame.this.itemRot -= 10f) < -360f) {
                SockGame.this.itemRot = 0f;
            }
        }
    }

    /**
     * Sets item string to empty string, therefore rendering invisible
     */
    class ItemStringRemoverRunnable implements Runnable {
        /**
         * Sets item string to empty string
         */
        @Override
        public void run() {
            itemString = "";
        }
    }

    /**
     * Decrements newSockCountdown by 1; if newSockCountdown is less than 1, cancels countdown,
     * increments numDiffSocks, and if numDiffSocks is less than maximum number of different socks,
     * resets and starts sock adder countdown
     */
    class SockAdderRunnable implements Runnable {
        @Override
        public void run() {
            if (--newSockCountdown < 1) {
                sockAdderScheduledFuture.cancel(false);
                if (++numDiffSocks < 24) {
                    newSockCountdown = 15;
                    sockAdderScheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(
                            new SockAdderRunnable(), 0, 1, TimeUnit.SECONDS);
                }
            }
        }
    }

    /**
     * Bleaches sock and socks around it
     */
    class SockBleacherRunnable implements Runnable {
        private Sock sock;

        /**
         * Constructor
         * @param sock Sock to bleach
         */
        public SockBleacherRunnable(Sock sock) {
            this.sock = sock;
        }

        /**
         * Bleaches sock and socks around it
         */
        public void run() {
            //Plays bleach sound (drop) if sound is on
            if (soundOn)
                soundPool.play(soundIds[2], 1f, 1f, 1, 0, 1f);

            //Set sock value to white and adds bleach effect to effects to draw
            this.sock.setVal(0);
            Drawn effect = effects[sock.getIndex()];
            effect.setVal(0);
            SockGame.this.effectsToDraw.add(effect);

            //Bleaches all valid neighbor socks and adds bleach effect over each one
            for (Node sockNode : sock.getNeighbors()) {
                Sock neighborSock = ((Sock)sockNode);
                if (SockGame.this.validSocks.contains(neighborSock)) {
                    neighborSock.setVal(0);
                    effect = SockGame.this.effects[neighborSock.getIndex()];
                    effect.setVal(0);
                    SockGame.this.effectsToDraw.add(effect);
                }
            }

            //Bleaches all valid corner socks and adds bleach effect over each one
            for (Node sockNode : sock.getCorners()) {
                Sock cornerSock = ((Sock)sockNode);
                if (SockGame.this.validSocks.contains(cornerSock)) {
                    cornerSock.setVal(0);
                    effect = SockGame.this.effects[cornerSock.getIndex()];
                    effect.setVal(0);
                    SockGame.this.effectsToDraw.add(effect);
                }
            }

            //Wait 250 milliseconds
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            //Remove bleach effects
            SockGame.this.effectsToDraw.clear();
        }
    }

    /**
     * Runnable for burning socks with fireball
     */
    class SockBurnerRunnable implements Runnable {
        private Sock sockToBurn;

        /**
         * Constructor
         * @param sockToBurn Sock to burn
         */
        public SockBurnerRunnable(Sock sockToBurn) {
            this.sockToBurn = sockToBurn;
        }

        /**
         * Plays burn sound if sound is on, adds burn effect, removes burned sock, increments score,
         * resets sock
         */
        @Override
        public void run() {
            //If sound is on, play burn sound
            if (soundOn)
                soundPool.play(soundIds[1], 1f, 1f, 1, 0, 1f);

            //Add burn effect
            Drawn effect = SockGame.this.effects[sockToBurn.getIndex()];
            effect.setVal(2);
            SockGame.this.effectsToDraw.add(effect);

            //Wait 250 milliseconds
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            //Removes sock from socks to draw and effect from effects to draw
            SockGame.this.socksToDraw.remove(sockToBurn);
            SockGame.this.effectsToDraw.remove(effect);

            //Increment score by one
            score++;

            //Wait 250 seconds
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            //Reset sock
            sockToBurn.setVal(((int)(Math.random()*numDiffSocks)));
            SockGame.this.validSocks.add(sockToBurn);
            SockGame.this.socksToDraw.add(sockToBurn);
        }
    }

    /**
     * Runnable for sock remover item
     */
    class SockRemoverRunnable implements Runnable {
        private Sock sockToRemove;

        /**
         * Constructor
         * @param sockToRemove Sock to use Sock Remover on
         */
        public SockRemoverRunnable(Sock sockToRemove) {
            this.sockToRemove = sockToRemove;
        }

        /**
         * Plays Sock Remover sound if sound is on, checks for matching socks, adds effects where
         * matches, removes and replenished matching socks, and adds appropriate points
         */
        @Override
        public void run() {
            //If sound is on, play Sock Remover sound
            if (soundOn)
                soundPool.play(soundIds[6], 1f, 1f, 1, 0, 1f);

            List<Sock> socksToRemove = new LinkedList<Sock>();
            int val = sockToRemove.getVal();  //Get sock value
            points = 1;

            //Add effect to effects to draw and sock to socks to remove list
            Drawn effect = SockGame.this.effects[sockToRemove.getIndex()];
            effect.setVal(1);
            SockGame.this.effectsToDraw.add(effect);
            SockGame.this.validSocks.remove(sockToRemove);
            socksToRemove.add(sockToRemove);

            //Loop through all socks and check for matches
            for (Sock sock : SockGame.this.socks) {
                if (SockGame.this.validSocks.contains(sock) && sock.getVal() == val) {
                    effect = SockGame.this.effects[sock.getIndex()];
                    effect.setVal(1);
                    SockGame.this.effectsToDraw.add(effect);
                    SockGame.this.validSocks.remove(sock);
                    socksToRemove.add(sock);
                    points++;
                }
            }

            //Wait 250 milliseconds
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            //Remove all matching socks and add points
            SockGame.this.socksToDraw.removeAll(socksToRemove);
            SockGame.this.effectsToDraw.clear();
            score += points;

            //Wait 250 milliseconds
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            //Replenishes removed socks
            for (Sock sock : socksToRemove) {
                sock.setVal(((int)(Math.random()*numDiffSocks)));
                SockGame.this.validSocks.add(sock);
                SockGame.this.socksToDraw.add(sock);
            }
        }
    }

    /**
     * Runnable for replenishing socks
     */
    class SockReplenisher implements Runnable {
        private List<Sock> socksToReplenish;

        /**
         * Constructor
         * @param socksToReplenish
         */
        public SockReplenisher(List<Sock> socksToReplenish) {
            this.socksToReplenish = socksToReplenish;
        }

        /**
         * Resets all socks in socks to replenish by resetting value to random value and adding to
         * valid socks and socks to draw
         */
        public void run() {
            for (Sock sockToReplenish : socksToReplenish) {
                sockToReplenish.setVal(((int)(Math.random()*numDiffSocks)));
                SockGame.this.validSocks.add(sockToReplenish);
                SockGame.this.socksToDraw.add(sockToReplenish);
            }
        }
    }

    /**
     * Runnable for game timer
     */
    class TimerRunnable implements Runnable {
        /**
         * Decrements game time by one and checks if time is less than one; when time reaches zero,
         * ends game
         */
        @Override
        public void run() {
            if (--time < 1) {
                endGame();
            }
        }
    }

    //Getters/Setters

    public final List<Drawn> getEffectsToDraw() {
        return this.effectsToDraw;
    }

    public final List<Drawn> getSocksToDraw() {
        return this.socksToDraw;
    }

    public final Drawn getSelectedSock() {
        return this.selectedSock;
    }

    public final boolean getHasItem() {
        return this.hasItem;
    }

    public final Drawn getItem() {
        return this.item;
    }

    public final float getItemRot() {
        return this.itemRot;
    }

    public final int getTime() {
        return this.time;
    }

    public final int getScore() {
        return this.score;
    }

    public final boolean getNewHighScore() {
        return this.newHighScore;
    }

    public final String getItemString() {
        return itemString;
    }

    public final boolean getIsSelected() {
        return isSelected;
    }

    public final Selection getSelected() {
        return selected;
    }
}
