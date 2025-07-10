package com.example.trabalhofinal;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back button
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Handle back button in toolbar
        return true;
    }
}