package com.example.marika.dinnerhalp;

//Created on 22 Aug 2018
//Thanks to https://developer.android.com/training/data-storage/sqlite#DefineContract

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DinnersDbHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DinnersDbContract.DinnerEntry.DATABASE_TABLE + " (" +
                    DinnersDbContract.DinnerEntry.KEY_ROWID + " integer primary key autoincrement, " +
                    DinnersDbContract.DinnerEntry.KEY_NAME + " text unique not null, " +
                    DinnersDbContract.DinnerEntry.KEY_METHOD + " text not null, " +
                    DinnersDbContract.DinnerEntry.KEY_TIME + " text not null, " +
                    DinnersDbContract.DinnerEntry.KEY_SERVINGS + " text not null, " +
                    DinnersDbContract.DinnerEntry.KEY_PICPATH + " text, " +
                    DinnersDbContract.DinnerEntry.KEY_PICDATA + " blob, " +
                    DinnersDbContract.DinnerEntry.KEY_RECIPE + " text);";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DinnersDbContract.DinnerEntry.DATABASE_TABLE;

    // If you change the database schema, you must increment the database version.
    // Version 2: adds the picdata column
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "dinnerData.db";

    //Do I need these?
    private DinnersDbHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String TAG = "DinnersDbHelper";

    public DinnersDbHelper(Context context) {
        super (context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Updating table from " + oldVersion + " to " + newVersion);
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    //Todo: Add additional methods
    //Todo: I think I need the open() method in order to instantiate a database object
    //in order to run queries on it

    //fetchAllDinners only fetches rowID and name columns, since that's what is needed in
    //DinnerListActivity.
    //It's not recommended to return all columns because that loads more data than needed.

    Cursor fetchAllDinners() {
        //Create string array to hold names of columns to be fetched
        String[] tableColumns = new String[] {
                DinnersDbContract.DinnerEntry.KEY_ROWID,
                DinnersDbContract.DinnerEntry.KEY_NAME
        };

        return
    }
}
