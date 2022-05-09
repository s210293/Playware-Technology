package com.playware.exercise2;

import static com.livelife.motolibrary.AntData.EVENT_PRESS;
import static com.livelife.motolibrary.AntData.LED_COLOR_BLUE;
import static com.livelife.motolibrary.AntData.LED_COLOR_INDIGO;
import static com.livelife.motolibrary.AntData.LED_COLOR_OFF;
import static com.livelife.motolibrary.AntData.LED_COLOR_RED;
import static com.livelife.motolibrary.AntData.LED_COLOR_GREEN;
import static com.livelife.motolibrary.AntData.LED_COLOR_WHITE;
import static com.livelife.motolibrary.AntData.LED_COLOR_ORANGE;
import static com.livelife.motolibrary.AntData.LED_COLOR_VIOLET;


import android.os.Handler;
import android.util.AndroidRuntimeException;
import android.util.Log;

import com.livelife.motolibrary.AntData;
import com.livelife.motolibrary.Game;
import com.livelife.motolibrary.GameType;
import com.livelife.motolibrary.MotoConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class FreqGame extends Game {
    MotoConnection connection = MotoConnection.getInstance();

    private ArrayList<Integer> tiles_list = new ArrayList<>(); //Array containing the number of flashes of each tile in random order
    private ArrayList<Integer> tiles_flashes = new ArrayList<>(); // Array with the number of times each tile flashes

    int tile_flashes_most = 0; //Tile number that flashes more times

    FreqGame() {
        setName("Frequency Game");

        GameType gt = new GameType(1, GameType.GAME_TYPE_TIME, 30, "1 player 30 sec", 1);
        addGameType(gt);

        GameType gt2 = new GameType(2, GameType.GAME_TYPE_TIME, 60, "1 player 1 min", 1);
        addGameType(gt2);

        GameType gt3 = new GameType(3, GameType.GAME_TYPE_TIME, 60 * 2, "1 player 2 min", 1);
        addGameType(gt3);

    }

    @Override
    public void onGameStart() {
        super.onGameStart();

        clearPlayersScore();

        connection.setAllTilesIdle(LED_COLOR_OFF);
        //Tiles numbers with the number of times they appear
        int tile1 = 0;
        int tile2 = 0;
        int tile3 = 0;
        int tile4 = 0;

        //Create the random array and store the number of times that flashes for each tile
        for (int i = 0; i < 15; i++){
            tiles_list.add(i, connection.randomIdleTile());
            if(tiles_list.get(i) == 1){
                tile1 ++;
            }
            if(tiles_list.get(i) == 2){
                tile2 ++;
            }
            if(tiles_list.get(i) == 3){
                tile3 ++;
            }
            if(tiles_list.get(i) == 4){
                tile4 ++;
            }
        }

        //Put the number of flashes of each tile in an array
        tiles_flashes.add(0, 0);
        tiles_flashes.add(1, tile1);
        tiles_flashes.add(2, tile2);
        tiles_flashes.add(3, tile3);
        tiles_flashes.add(4, tile4);

        //Check which tile flashes the most
        int max = 0;
        //tile that flashes the most number of times
        for (int i = 0; i < 5; i++){
            if(tiles_flashes.get(i) >= max) {
                max = tiles_flashes.get(i);
                tile_flashes_most = i; //Tile number that flashes more times
            }
        }

        //Make tiles flash in a random order
        for (int i = 0; i < 15; i++){
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connection.setAllTilesColor(0);
            //connection.setTileBlink(1, LED_COLOR_RED, tiles_list.get(i));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connection.setTileColor(LED_COLOR_BLUE, tiles_list.get(i));
        }
        connection.setAllTilesColor(0);

    }

    // Put game logic here
    @Override
    public void onGameUpdate(byte[] message) {
        super.onGameUpdate(message);

        int event = AntData.getCommand(message); // To store and check for EVENT_PRESS

        if (event == EVENT_PRESS)
        {
            int pressed_tile = AntData.getId(message);
            if (pressed_tile == tile_flashes_most) { // Checking if the correct tile was pressed
                incrementPlayerScore(10, 0);

                connection.setAllTilesIdle(LED_COLOR_OFF);
                generate_next_set_of_tile();
            }

            else {
                this.stopGame();
            }
        }
    }


    // Some animation on the tiles once the game is over
    @Override
    public void onGameEnd()
    {
        super.onGameEnd();
        connection.setAllTilesBlink(4,LED_COLOR_GREEN);
    }

    public void generate_next_set_of_tile() {

        //Tiles numbers with the number of times they appear
        int tile1 = 0;
        int tile2 = 0;
        int tile3 = 0;
        int tile4 = 0;

        //Create the random array and store the number of times that flashes for each tile
        for (int i = 0; i < 15; i++){
            tiles_list.add(i, connection.randomIdleTile());
            if(tiles_list.get(i) == 1){
                tile1 ++;
            }
            if(tiles_list.get(i) == 2){
                tile2 ++;
            }
            if(tiles_list.get(i) == 3){
                tile3 ++;
            }
            if(tiles_list.get(i) == 4){
                tile4 ++;
            }
        }

        //Put the number of flashes of each tile in an array
        tiles_flashes.add(0, 0);
        tiles_flashes.add(1, tile1);
        tiles_flashes.add(2, tile2);
        tiles_flashes.add(3, tile3);
        tiles_flashes.add(4, tile4);

        //Check which tile flashes the most
        int max = 0;
        //tile that flashes the most number of times
        for (int i = 0; i < 5; i++){
            if(tiles_flashes.get(i) >= max) {
                max = tiles_flashes.get(i);
                tile_flashes_most = i; //Tile number that flashes more times
            }
        }

        final int[] loop = {0};
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(loop[0] == 15) {
                    connection.setAllTilesColor(LED_COLOR_OFF);

                    handler.removeCallbacks(this);
                } else {
                    connection.setAllTilesColor(0);
                    connection.setTileColor(LED_COLOR_BLUE, tiles_list.get(loop[0]));
                    loop[0]++;
                    handler.postDelayed(this, 800L);
                }

            }
        };
        handler.post(runnable);

    }
}