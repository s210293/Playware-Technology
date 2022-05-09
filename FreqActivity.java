package com.playware.exercise2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.livelife.motolibrary.Game;
import com.livelife.motolibrary.GameType;
import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.OnAntEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FreqActivity extends AppCompatActivity implements OnAntEventListener {
    MotoConnection connection = MotoConnection.getInstance();
    FreqGame game_object = new FreqGame(); // Game object
    LinearLayout gt_container;

    String endpoint = "https://centerforplayware.com/api/index.php";
    String deviceToken = "Laura123";

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;


    int points_scored; //Points scored by the player
    int round_number;
    int max_score = 0;
    //Stop the game when we exit activity
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        game_object.stopGame();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frequency);

        sharedPref = getSharedPreferences("application", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        connection.registerListener(this);
        connection.setAllTilesToInit();

        gt_container = findViewById(R.id.game_type_container);

        for (final GameType gt : game_object.getGameTypes()) {
            Button b = new Button(this);
            b.setText(gt.getName());
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    game_object.selectedGameType = gt;
                    game_object.startGame();
                }
            });
            gt_container.addView(b);
        }
        final TextView game_round = findViewById(R.id.round_number); // Shows what round the user is on
        final TextView player_score = findViewById(R.id.score_value); // Where the player's score is displayed
        final TextView highest_score = findViewById(R.id.max_score);

        highest_score.setText(String.valueOf(sharedPref.getInt("freq_score", 0)));

        game_object.setOnGameEventListener(new Game.OnGameEventListener() {
            @Override

            public void onGameTimerEvent(int i) {
            }

            @Override
            public void onGameScoreEvent(int i, int i1) {
                points_scored = i;
                round_number = i/10;
                if(points_scored>max_score){
                    max_score = i;
                }

                FreqActivity.this.runOnUiThread(new Runnable() {
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

                if(sharedPref.getInt("freq_score", 0) < game_object.getPlayerScore()[0]){
                    editor.putInt("freq_score", game_object.getPlayerScore()[0]);
                    editor.apply();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        highest_score.setText(String.valueOf(sharedPref.getInt("freq_score", 0)));
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
        game_object.addEvent(bytes);
    }

    @Override
    public void onAntServiceConnected() {

    }

    @Override
    public void onNumbersOfTilesConnected(final int i) {

    }

    private void postGameSession() {
        RemoteHttpRequest requestPackage = new RemoteHttpRequest();
        requestPackage.setMethod("POST");
        requestPackage.setUrl(endpoint);
        requestPackage.setParam("method","postGameSession"); // The method name
        requestPackage.setParam("group_id","5"); // Your group ID
        requestPackage.setParam("game_id", String.valueOf(game_object.getGameId())); // The game ID (From the Game class > setGameId() function
        requestPackage.setParam("game_type_id", String.valueOf(game_object.selectedGameType.getType())); // The game type ID (From the GameType class creation > first parameter)
        requestPackage.setParam("game_score", String.valueOf(game_object.getPlayerScore()[0])); // The game score
        requestPackage.setParam("game_time",String.valueOf(game_object.getDuration())); // The game elapsed time in seconds
        requestPackage.setParam("num_tiles",String.valueOf(game_object.connection.connectedTiles.size())); // The number of tiles used
        requestPackage.setParam("device_token", deviceToken);

        FreqActivity.Downloader downloader = new FreqActivity.Downloader(); //Instantiation of the Async task

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