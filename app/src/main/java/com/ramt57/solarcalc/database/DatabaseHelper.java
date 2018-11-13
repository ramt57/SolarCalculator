package com.ramt57.solarcalc.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

import com.ramt57.solarcalc.model.PlacesModel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "places_db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PlacesModel.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PlacesModel.TABLE_NAME);
        onCreate(db);
    }

    public long insertPlaces(PlacesModel placesModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PlacesModel.COLUMN_NAME, placesModel.getName());
        values.put(PlacesModel.COLUMN_Address, placesModel.getAddress());
        values.put(PlacesModel.COLUMN_LAT, placesModel.getLat());
        values.put(PlacesModel.COLUMN_LNG, placesModel.getLng());
        long id = db.insert(PlacesModel.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public PlacesModel getPlace(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(PlacesModel.TABLE_NAME,
                new String[]{PlacesModel.COLUMN_ID, PlacesModel.COLUMN_NAME, PlacesModel.COLUMN_Address,
                        PlacesModel.COLUMN_LAT, PlacesModel.COLUMN_LNG},
                PlacesModel.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        PlacesModel placesModel = new PlacesModel();
        placesModel.setId(cursor.getInt(cursor.getColumnIndex(PlacesModel.COLUMN_ID)));
        placesModel.setName(cursor.getString(cursor.getColumnIndex(PlacesModel.COLUMN_NAME)));
        placesModel.setAddress(cursor.getString(cursor.getColumnIndex(PlacesModel.COLUMN_Address)));
        placesModel.setLat(Double.parseDouble(cursor.getString(cursor.getColumnIndex(PlacesModel.COLUMN_LAT))));
        placesModel.setLng(Double.parseDouble(cursor.getString(cursor.getColumnIndex(PlacesModel.COLUMN_LNG))));

        cursor.close();
        return placesModel;
    }

    public List<PlacesModel> getAllPlaces() {
        List<PlacesModel> placesModelArrayList = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + PlacesModel.TABLE_NAME + " ORDER BY " +
                PlacesModel.COLUMN_ID + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                PlacesModel placesModel = new PlacesModel();
                placesModel.setId(cursor.getInt(cursor.getColumnIndex(PlacesModel.COLUMN_ID)));
                placesModel.setName(cursor.getString(cursor.getColumnIndex(PlacesModel.COLUMN_NAME)));
                placesModel.setAddress(cursor.getString(cursor.getColumnIndex(PlacesModel.COLUMN_Address)));
                placesModel.setLat(Double.parseDouble(cursor.getString(cursor.getColumnIndex(PlacesModel.COLUMN_LAT)).trim()));
                placesModel.setLng(Double.parseDouble(cursor.getString(cursor.getColumnIndex(PlacesModel.COLUMN_LNG)).trim()));
                placesModelArrayList.add(placesModel);
            } while (cursor.moveToNext());
        }

        db.close();

        return placesModelArrayList;
    }

    public int getPlacesCount() {
        String countQuery = "SELECT  * FROM " + PlacesModel.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void deletePlace(PlacesModel placesModel) {
        Log.w("Loc",placesModel.getLng()+" : "+placesModel.getLng()+" : "+placesModel.getId());
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(PlacesModel.TABLE_NAME, PlacesModel.COLUMN_ID + " = ?",
                new String[]{String.valueOf(placesModel.getId())});
        db.close();
    }
}
