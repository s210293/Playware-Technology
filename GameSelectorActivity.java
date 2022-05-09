package com.playware.exercise2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class GameSelectorActivity extends AppCompatActivity {

    Button memoryButton, frequencyButton, listButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_selector);


        memoryButton = findViewById(R.id.memoryButton);
        frequencyButton = findViewById(R.id.frequencyButton);
        listButton = findViewById(R.id.listButton);

        memoryButton.setOnClickListener(v -> {
            Intent i = new Intent(GameSelectorActivity.this, MemoryActivity.class);
            startActivity(i);
        });

        frequencyButton.setOnClickListener(v -> {
            Intent i = new Intent(GameSelectorActivity.this, FreqActivity.class);
            startActivity(i);
        });

        listButton.setOnClickListener(v -> {
            Intent i = new Intent(GameSelectorActivity.this, PrettyListActivity.class);
            startActivity(i);
        });
    }
}