package com.example.marika.dinnerhalp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

/**
 * Created by marika on 7/25/15.
 * Based on the Notepad Tutorial from developer.android.com.
 */


public class DinnersDbAdapter {

    public static final String KEY_NAME = "name";
    public static final String KEY_METHOD = "method";
    public static final String KEY_TIME = "time";
    public static final String KEY_SERVINGS = "servings";
    public static final String KEY_PICPATH = "picpath";
    public static final String KEY_RECIPE = "recipe";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "DinnersDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     * Todo: Ensure that dinner name cannot be identical to other records
     * I think I do this by putting "unique" after "name text not null"
     */
    private static final String DATABASE_CREATE =
            "create table dinners (_id integer primary key autoincrement, "
                    + "name text not null, method text not null, time text not null, "
                    + "servings text not null, picpath text, recipe text);";

    private static final String DATABASE_NAME = "dinnerData";
    private static final String DATABASE_TABLE = "dinners";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS dinners");
            onCreate(db);
        }

    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public DinnersDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the dinners database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */

    public DinnersDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    /**
     * Create a new dinner using the data provided. If the dinner is
     * successfully created return the new rowId for that dinner, otherwise return
     * a -1 to indicate failure.
     *
     * @param name the name of the dinner
     * @param method the cooking method of the dinner (stovetop, oven, slow cooker)
     * @param time the cook time
     * @param servings the number of servings
     * @param picpath file path for photo (not implemented yet)
     * @param recipe text of recipe
     * @return rowId or -1 if failed
     */
    public long createDinner(String name, String method, String time, String servings,
                             String picpath, String recipe) {
        //If the user tries to create a dinner with no name, this is handled
        //in AddDinnerActivity
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_METHOD, method);
        initialValues.put(KEY_TIME, time);
        initialValues.put(KEY_SERVINGS, servings);
        initialValues.put(KEY_PICPATH, picpath);
        initialValues.put(KEY_RECIPE, recipe);

        //Todo: Put inside a try/catch statement to catch duplicate name fields?
        //Maybe insertOrThrow rather than just insert?
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the dinner with the given rowId
     *
     * @param rowId id of dinner to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteDinner(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all dinners in the database
     *
     * @return Cursor over all dinners
     * The parameters for query() are: table name, column name(s) to return, whereClause,
     * whereArgs, groupBy, having, orderBy
     */

    //fetchAllDinners only fetches rowID and name columns, since that's what is needed in
    //DinnerListActivity.
    //It's not recommended to return all columns because that loads more data than needed.

    public Cursor fetchAllDinners() {

        //Create string array to hold names of columns to be fetched
        String[] tableColumns = new String[] {
                KEY_ROWID,
                KEY_NAME
        };

        return mDb.query(DATABASE_TABLE, tableColumns, null, null,
                null, null, KEY_NAME + " ASC");
    }

    /**
     * Return a Cursor with all dinners that match a query on a particular column
     */
    //fetchDinnerSearch returns only rowID and name columns, since that's what is needed in
    //DinnerListActivity.
    //It's not recommended to return all columns because that loads more data than needed.
    //Todo: Also, shouldn't this throw SQLException?

    public Cursor fetchDinnerSearch(boolean keywordSearch, String whereClause, String searchString) {

        //Create string array to hold names of columns to be fetched
        String[] tableColumns = new String[] {
                KEY_ROWID,
                KEY_NAME
        };

        String[] whereArgs;

        //Number of whereArgs is 1 for all searches except keyword, which needs 2
        if (keywordSearch) {
            whereArgs = new String[] {
                    searchString,
                    searchString
            };

        } else {
            whereArgs = new String[]{
                    searchString
            };
        }

        return mDb.query(DATABASE_TABLE, tableColumns, whereClause, whereArgs, null, null,
                KEY_NAME + " ASC");
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     *
     * @param rowId id of dinner to retrieve
     * @return Cursor positioned to matching dinner, if found
     * @throws SQLException if dinner could not be found/retrieved
     */

    //fetchDinner returns all columns (third parameter of query) because all are needed for
    //ViewDinnerActivity.

    public Cursor fetchDinner(long rowId) throws SQLException {

        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE, null, KEY_ROWID + "=" + rowId,
                        null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }

    /**
     * Update the dinner using the details provided. The dinner to be updated is
     * specified using the rowId, and it is altered to use the
     * values passed in
     *
     * @param rowId id of dinner to update
     * @param name value to set dinner title to
     * @param method value to set cooking method to
     *               etc. etc.
     * @return true if the dinner was successfully updated, false otherwise
     */
    public boolean updateDinner(long rowId, String name, String method, String time,
                                String servings, String picpath, String recipe) {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);
        args.put(KEY_METHOD, method);
        args.put(KEY_TIME, time);
        args.put(KEY_SERVINGS, servings);
        args.put(KEY_PICPATH, picpath);
        args.put(KEY_RECIPE, recipe);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    //Method for deleting all rows in the database table
    public int clearAllDinners() {

        //Passing 1 as the whereClause returns the number of rows deleted
        return mDb.delete(DATABASE_TABLE, "1", null);
    }

}
