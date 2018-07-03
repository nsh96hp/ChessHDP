package com.onezeros.chinesechess;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int VERSION = 1;
    public static final String DATABASE_NAME = "DBChess";
    public static final String TABLE_NAME="tblChess";
    public static final String ID="id";
    public static final String TIME="time";
    public static final String RESULT="result";
    public static final String LEVEL="level";




    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }
    public static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+" (\n" +
            ID + "        INTEGER       PRIMARY KEY AUTOINCREMENT,\n" +
            TIME + "         VARCHAR(128),\n" +
            RESULT + "        INTEGER (1),\n" +
            LEVEL + "          INTEGER (1)\n" +
            ");\n";
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
