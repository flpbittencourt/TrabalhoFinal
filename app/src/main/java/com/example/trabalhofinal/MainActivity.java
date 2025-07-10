package com.example.trabalhofinal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SeriesAdapter.OnItemClickListener {

    private SeriesDbHelper dbHelper;
    private RecyclerView seriesRecyclerView;
    private SeriesAdapter seriesAdapter;
    private TextView noItemsText;
    private static final int REQUEST_CODE_ADD_SERIES = 1;
    private static final int REQUEST_CODE_EDIT_SERIES = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title
        }

        ImageView infoIcon = findViewById(R.id.info_icon);
        infoIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });

        dbHelper = new SeriesDbHelper(this);
        seriesRecyclerView = findViewById(R.id.series_recycler_view);
        noItemsText = findViewById(R.id.no_items_text);
        seriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fabAddSeries = findViewById(R.id.fab_add_series);
        fabAddSeries.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditSeriesActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_SERIES);
        });

        loadSeries();
    }

    private void loadSeries() {
        List<Series> seriesList = dbHelper.getAllSeries();
        if (seriesList.isEmpty()) {
            noItemsText.setVisibility(View.VISIBLE);
            seriesRecyclerView.setVisibility(View.GONE);
        } else {
            noItemsText.setVisibility(View.GONE);
            seriesRecyclerView.setVisibility(View.VISIBLE);
            seriesAdapter = new SeriesAdapter(seriesList, this);
            seriesRecyclerView.setAdapter(seriesAdapter);
        }
    }

    @Override
    public void onItemClick(Series series) {
        // Handle item click for editing
        Intent intent = new Intent(MainActivity.this, AddEditSeriesActivity.class);
        intent.putExtra("SERIES_ID", series.getId());
        intent.putExtra("SERIES_TITLE", series.getTitle());
        intent.putExtra("SERIES_GENRE", series.getGenre());
        intent.putExtra("SERIES_SEASONS", series.getSeasons());
        startActivityForResult(intent, REQUEST_CODE_EDIT_SERIES);
    }

    @Override
    public void onItemLongClick(Series series) {
        // Handle item long click for deletion
        new AlertDialog.Builder(this)
                .setTitle("Excluir Item")
                .setMessage(getString(R.string.confirm_delete_message, series.getTitle()))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    dbHelper.deleteSeries(series.getId());
                    Toast.makeText(MainActivity.this, "Item excluído!", Toast.LENGTH_SHORT).show();
                    loadSeries(); // Refresh the list
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_ADD_SERIES || requestCode == REQUEST_CODE_EDIT_SERIES) {
                loadSeries(); // Reload data when returning from AddEditSeriesActivity
            }
        }
    }
}