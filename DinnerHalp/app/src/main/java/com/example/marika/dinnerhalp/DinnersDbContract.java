package com.example.marika.dinnerhalp;

//Created on 22 Aug 2018
//Thanks to https://developer.android.com/training/data-storage/sqlite#DefineContract

import android.provider.BaseColumns;

public final class DinnersDbContract {
    // To prevent someone from accidentally instantiating the contract class,
    // the constructor is private

    private DinnersDbContract() {
    }

    /* Inner class that defines the table contents */
    public static class DinnerEntry implements BaseColumns {
        public static final String DATABASE_TABLE = "dinners";
        public static final String KEY_NAME = "name";
        public static final String KEY_METHOD = "method";
        public static final String KEY_TIME = "time";
        public static final String KEY_SERVINGS = "servings";
        public static final String KEY_PICPATH = "picpath";
        public static final String KEY_PICDATA = "picdata";
        public static final String KEY_RECIPE = "recipe";
        public static final String KEY_ROWID = "_id";

        private static final String TAG = "DinnersDbContract";
    }
}
