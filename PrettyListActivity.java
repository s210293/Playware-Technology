package com.playware.exercise2;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.MotoSound;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PrettyListActivity extends AppCompatActivity {

    ArrayAdapter<String> adapter;
    Button simulateGetGameSessions;
    Button simulatePostGameSession;
    TextView connectedTextView;

    String deviceToken = "Laura123";

    ListView listview;
    ArrayList<String> arrayGames = new ArrayList<>();
    ArrayList<String> listFromJson = new ArrayList<>();


    TextView apiOutput;
    String endpoint = "https://centerforplayware.com/api/index.php";

    public PrettyListActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pretty_list);

        listview = findViewById(R.id.rcviewer);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, arrayGames){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view =super.getView(position, convertView, parent);

                TextView textView=(TextView) view.findViewById(android.R.id.text1);

                textView.setTextColor(Color.WHITE);

                return view;}
        };
        listview.setAdapter(adapter);

        apiOutput = findViewById(R.id.apiOutput);
        connectedTextView = findViewById(R.id.connectedTextView);




        simulateGetGameSessions = findViewById(R.id.simulateGetGameSessions);
        simulateGetGameSessions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGameSessions();

            }
        });

        simulatePostGameSession = findViewById(R.id.simulatePostGameSession);
        simulatePostGameSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                postGameSession();


            }
        });
    }

    private void postGameSession() {
        RemoteHttpRequest requestPackage = new RemoteHttpRequest();
        requestPackage.setMethod("POST");
        requestPackage.setUrl(endpoint);
        requestPackage.setParam("method","postGameSession");
        requestPackage.setParam("group_id","5");
        requestPackage.setParam("game_id","1");
        requestPackage.setParam("game_type_id","1");
        requestPackage.setParam("game_score","30");
        requestPackage.setParam("game_time","60");
        requestPackage.setParam("num_tiles","4");
        requestPackage.setParam("device_token", deviceToken);

        Downloader downloader = new Downloader();

        downloader.execute(requestPackage);
    }
    private void getGameSessions() {
        RemoteHttpRequest requestPackage = new RemoteHttpRequest();
        requestPackage.setMethod("GET");
        requestPackage.setUrl(endpoint);
        requestPackage.setParam("method","getGameSessions");
        requestPackage.setParam("group_id","5");

        Downloader downloader = new Downloader();

        downloader.execute(requestPackage);
    }

    private class Downloader extends AsyncTask<RemoteHttpRequest, String, String> {
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

                // Update UI
                apiOutput.setText(message);
                Log.i("sessions",message);


                if(jsonObject.getString("method").equals("getGameSessions")) {
                    JSONArray sessions = jsonObject.getJSONArray("results");
                    for(int i = 0; i < sessions.length();i++) {
                        JSONObject session = sessions.getJSONObject(i);
                        StringBuilder sb = new StringBuilder();
                        sb.append("Game session ID:").append(session.getString("sid")).append(" Score: ").append(session.getString("game_score")).append(" Group ID:").append(session.getString("group_id")).append(" Number of tiles:").append(session.getString("num_tiles"));
                        listFromJson.add(sb.toString());
                    }
                    arrayGames.addAll(listFromJson);
                    adapter.notifyDataSetChanged();

                }
                else if(jsonObject.getString("method") == "postGameSession") {

                    Log.i("sessions",message);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

}
