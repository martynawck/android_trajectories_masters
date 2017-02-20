package com.wiacek.martyna.mastersresearch.utils;

/**
 * Created by Martyna on 2016-06-10.
 */
import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyLocations.db";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String TABLE_NAME = "locations";

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table "+ TABLE_NAME +
                        " ( "+ COLUMN_ID +" integer primary key, "+ COLUMN_LONGITUDE +" text, "+ COLUMN_LATITUDE +" text, " +
                        COLUMN_TIMESTAMP +" text )"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS contacts");
        onCreate(db);
    }

    public boolean insertLocation (String longitude, String latitude, String timestamp)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TIMESTAMP, timestamp);
        contentValues.put(COLUMN_LATITUDE, latitude);
        contentValues.put(COLUMN_LONGITUDE, longitude);
        db.insert(TABLE_NAME, null, contentValues);
        return true;
    }

    public ArrayList<String> getData(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from " + TABLE_NAME + " where id=" + id + "", null);
        ArrayList<String> list = new ArrayList<>();
        if (res.moveToFirst()){
            while(!res.isAfterLast()){
                  list.add(res.getString(res.getColumnIndex(COLUMN_TIMESTAMP)));
                  list.add(res.getString(res.getColumnIndex(COLUMN_LATITUDE)));
                  list.add(res.getString(res.getColumnIndex(COLUMN_LONGITUDE)));
                  res.moveToNext();
            }
        }
        res.close();
        return list;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        return numRows;
    }

    public Integer deleteLocation (Integer id)
    {
        Log.d("DELETER",Integer.toString(id));
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<String> getAllLocations()
    {
        ArrayList<String> array_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(COLUMN_ID)));
            res.moveToNext();
        }
        return array_list;
    }
}