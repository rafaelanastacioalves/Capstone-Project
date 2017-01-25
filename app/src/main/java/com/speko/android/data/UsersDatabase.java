package com.speko.android.data;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.IfNotExists;
import net.simonvt.schematic.annotation.OnCreate;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by rafaelalves on 14/01/17.
 */

@Database(version = UsersDatabase.VERSION)
public class UsersDatabase {
    private UsersDatabase(){

    }

    public static final int VERSION = 1;

    @Table(UserColumns.class)   @IfNotExists public static final String USERS_TABLE = "users";

    @OnCreate
    public static void onCreate(SQLiteDatabase db) {
        Log.i("UsersDatabase", "onCreate" );
    }

    }
