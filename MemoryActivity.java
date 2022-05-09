package com.playware.exercise2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.livelife.motolibrary.Game;
import com.livelife.motolibrary.GameType;
import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.MotoSound;
import com.livelife.motolibrary.OnAntEventListener;

import org.json.JSONException;
import org.json.JSONObject;

public class MemoryActivity extends AppCompatActivity implements OnAntEventListener {
    MotoConnection connection = MotoConnection.getInstance();
    MotoSound motoSound = MotoSound.getInstance();
    LinearLayout gameTypeContainer;
    MemoryGame game;

    int points_scored; //Points scored by the player
    int round_number = 0;
    int max_score = 0;

    String endpoint = "https://centerforplayware.com/api/index.php";
    String deviceToken = "Laura123";

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        game.stopGame();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory);

        sharedPref = getSharedPreferences("application", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        connection.registerListener(this);
        connection.setAllTilesToInit();

        game = new MemoryGame();
        gameTypeContainer = findViewById(R.id.specialOneLayout);

        for (final GameType gt : game.getGameTypes()) {
            Button b = new Button(this);
            b.setText(gt.getName());
            b.setOnClickListener(v -> {
                motoSound.playStart();
                game.selectedGameType = gt;
                game.startGame();
            });
            gameTypeContainer.addView(b);
        }

        final TextView game_round = findViewById(R.id.round_number); // Shows what round the user is on
        final TextView player_score = findViewById(R.id.score_value); // Where the player's score is displayed
        final TextView highest_score = findViewById(R.id.max_score);

        highest_score.setText(String.valueOf(sharedPref.getInt("memory_score", 0)));


        game.setOnGameEventListener(new Game.OnGameEventListener() {
            @Override

            public void onGameTimerEvent(int i) {
            }

            @Override
            public void onGameScoreEvent(int i, int i1) {
                points_scored = i;
                round_number = round_number + 1;

                if(points_scored>max_score){
                    max_score = i;
                }

                MemoryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        player_score.setText(String.valueOf(points_scored));
                        game_round.setText(String.valueOf(round_number));
                    }
                });
            }

            @Override
            public void onGameStopEvent() {
                postGameSession();

                if(sharedPref.getInt("memory_score", 0) < game.getPlayerScore()[0]){
                    editor.putInt("memory_score", game.getPlayerScore()[0]);
                    editor.apply();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        highest_score.setText(String.valueOf(sharedPref.getInt("memory_score", 0)));
                    }
                });
            }

            @Override
            public void onSetupMessage(String s) {

            }

            @Override
            public void onGameMessage(String s) {

            }

            @Override
            public void onSetupEnd() {

            }
        });

    }

    @Override
    public void onMessageReceived(byte[] bytes, long l) {
        game.addEvent(bytes);
    }

    @Override
    public void onAntServiceConnected() {

    }

    @Override
    public void onNumbersOfTilesConnected(int i) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connection.unregisterListener(this);
    }

    private void postGameSession() {
        RemoteHttpRequest requestPackage = new RemoteHttpRequest();
        requestPackage.setMethod("POST");
        requestPackage.setUrl(endpoint);
        requestPackage.setParam("method","postGameSession");
        requestPackage.setParam("group_id","5");
        requestPackage.setParam("game_id", String.valueOf(game.getGameId()));
        requestPackage.setParam("game_type_id", String.valueOf(game.selectedGameType.getType()));
        requestPackage.setParam("game_score", String.valueOf(game.getPlayerScore()[0]));
        requestPackage.setParam("game_time",String.valueOf(game.getDuration()));
        requestPackage.setParam("num_tiles",String.valueOf(game.connection.connectedTiles.size()));
        requestPackage.setParam("device_token", deviceToken);

        MemoryActivity.Downloader downloader = new MemoryActivity.Downloader();

        downloader.execute(requestPackage);
    }

    private static class Downloader extends AsyncTask<RemoteHttpRequest, String, String> {
        @Override
        protected String doInBackground(RemoteHttpRequest... params) {
            return HttpManager.getData(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                                Log.i("TAG", result);
                JSONObject jsonObject = new JSONObject(result);
                String message = jsonObject.getString("message");


                if(jsonObject.getString("method") == "postGameSession") {
                    Log.i("sessions",message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}