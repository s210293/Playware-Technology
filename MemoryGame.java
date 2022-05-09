package com.playware.exercise2;

import com.livelife.motolibrary.AntData;
import com.livelife.motolibrary.Game;
import com.livelife.motolibrary.GameType;
import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.MotoSound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.livelife.motolibrary.AntData.*;

import android.os.Handler;

public class MemoryGame extends Game {

    MotoConnection connection = MotoConnection.getInstance();
    MotoSound motoSound = MotoSound.getInstance();

    ArrayList<Integer> colours = new ArrayList<>();
    Random r = new Random(1);
    ArrayList<Integer> tilenumber = new ArrayList<>();

    //this variable represents the round of the game we are playing
    int round = 0;
    //this variable is for the score
    int score = 0;
    //this variable is used to check that the tile we are pressing is the one that we are
    //supposed to press. In case, it is correct, we add 1 to the tile variable in order to
    //check the next tile we are supposed to press
    int time = 0;

    public MemoryGame() {
        setName("Memory Game");
        setDescription("Another cool game");

        GameType gt = new GameType(1, GameType.GAME_TYPE_SCORE, 10,"SHORT - 10 POINTS",1);
        addGameType(gt);

        GameType gt2 = new GameType(2, GameType.GAME_TYPE_SCORE, 200,"LONG - UNLIMITED",1);
        addGameType(gt2);
        addColours();
    }

    @Override
    public void onGameStart() {
        super.onGameStart();
        clearPlayersScore();
        round = 0;
        time = 0;
        score = 0;
        tilenumber.removeAll(tilenumber);

        connection.setAllTilesIdle(LED_COLOR_OFF);
        int randomtile = connection.randomIdleTile();
        this.tilenumber.add(randomtile);

        //We make blink the tiles in a certain order adding a new random one each time we are correct
        for (int i=0; i<=round;i++){
            connection.setTileColor(LED_COLOR_GREEN,this.tilenumber.get(i));
            try
            {
                Thread.sleep(500);
            }
            catch(InterruptedException e)
            {
                // this part is executed when an exception (in this example InterruptedException) occurs
            }
            connection.setAllTilesIdle(LED_COLOR_OFF);

            try
            {
                Thread.sleep(1000);
            }
            catch(InterruptedException e)
            {
                // this part is executed when an exception (in this example InterruptedException) occurs
            }
        }
        for (int i : connection.connectedTiles) {
            connection.setTileColor(LED_COLOR_RED,i);
        }

        System.out.println(tilenumber);
    }

    private void progressGame(){
        //First we turn off all the tiles
        connection.setAllTilesIdle(LED_COLOR_OFF);
        int randomtile = connection.randomIdleTile();
        while(randomtile == tilenumber.get(tilenumber.size() - 1)) {
            randomtile = connection.randomIdleTile();
        }
        tilenumber.add(randomtile);

        //We make blink the tiles in a certain order adding a new random one each time we are correct
        final int[] loop = {0};
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(loop[0] == round) {
                    connection.setAllTilesColor(0);
                    connection.setTileColor(LED_COLOR_GREEN, tilenumber.get(loop[0]));
                    handler.removeCallbacks(this);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            for (int i : connection.connectedTiles) {
                                connection.setTileColor(LED_COLOR_RED, i);
                            }
                        }
                    }, 500);
                } else {
                    connection.setAllTilesColor(0);
                    connection.setTileColor(LED_COLOR_GREEN, tilenumber.get(loop[0]));
                    loop[0]++;
                    handler.postDelayed(this, 1000L);
                }
            }
        };
        handler.post(runnable);
    }

    private void addColours(){
        colours.add(LED_COLOR_BLUE);
        colours.add(LED_COLOR_ORANGE);
        colours.add(LED_COLOR_GREEN);
        colours.add(LED_COLOR_RED);
        colours.add(LED_COLOR_INDIGO);
    }

    private int getRandomColor(){
       return getRandomColor(-1);
    }

    private int getRandomColor(int ignore){
        int color = ignore;
        while(color == ignore){
            color = this.colours.get(r.nextInt(this.colours.size()));
        }
        return color;
    }

    @Override
    public void onGameUpdate(byte[] message) {
        super.onGameUpdate(message);
        int event = AntData.getCommand(message);
        int id = AntData.getId(message);

        if(event == EVENT_PRESS && id == tilenumber.get(time)){
            if (time == round){
                round++;
                score=score+round;
                motoSound.speak(""+score);

                incrementPlayerScore(round, 0);
                time = 0;
                progressGame();
            }
            else{
                this.time++;
            }

        }
        else{
            this.time=0;
            this.stopGame();
        }
    }

    @Override
    public void onGameEnd() {
        super.onGameEnd();
        round=0;
        motoSound.speak("Game ended, your score was: " + this.score);
        connection.setAllTilesIdle(LED_COLOR_OFF);

    }
}
