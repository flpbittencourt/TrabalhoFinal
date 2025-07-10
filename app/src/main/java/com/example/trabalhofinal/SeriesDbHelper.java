package com.example.trabalhofinal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SeriesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "series.db";
    private static final int DATABASE_VERSION = 1; // Mantenha ou incremente se for uma atualização real

    public static final String TABLE_SERIES = "series";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_GENRE = "genre";
    public static final String COLUMN_SEASONS = "seasons";
    public static final String COLUMN_IMAGE_PATH = "image_path";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_SERIES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_GENRE + " TEXT, " +
                    COLUMN_SEASONS + " INTEGER, " +
                    COLUMN_IMAGE_PATH + " TEXT);";

    public SeriesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Se você já tem o app instalado e adicionou a coluna, precisaria de uma lógica de migração
        // Para fins de desenvolvimento inicial, pode ser suficiente apagar e recriar.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERIES);
        onCreate(db);
    }

    // --- CRUD Operations ---

    public long addSeries(Series series) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, series.getTitle());
        values.put(COLUMN_GENRE, series.getGenre());
        values.put(COLUMN_SEASONS, series.getSeasons());
        values.put(COLUMN_IMAGE_PATH, series.getImagePath());
        long newId = db.insert(TABLE_SERIES, null, values);
        db.close();
        return newId;
    }

    public List<Series> getAllSeries() {
        List<Series> seriesList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_SERIES;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Series series = new Series();
                series.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                series.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                series.setGenre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENRE)));
                series.setSeasons(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SEASONS)));
                // Verifique se a coluna existe antes de tentar lê-la para compatibilidade
                int imagePathIndex = cursor.getColumnIndex(COLUMN_IMAGE_PATH);
                if (imagePathIndex != -1) {
                    series.setImagePath(cursor.getString(imagePathIndex));
                } else {
                    series.setImagePath(null); // ou uma string vazia
                }
                seriesList.add(series);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return seriesList;
    }

    public int updateSeries(Series series) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, series.getTitle());
        values.put(COLUMN_GENRE, series.getGenre());
        values.put(COLUMN_SEASONS, series.getSeasons());
        values.put(COLUMN_IMAGE_PATH, series.getImagePath());

        // updating row
        int rowsAffected = db.update(TABLE_SERIES, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(series.getId())});
        db.close();
        return rowsAffected;
    }

    public void deleteSeries(long seriesId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SERIES, COLUMN_ID + " = ?",
                new String[]{String.valueOf(seriesId)});
        db.close();
    }
}