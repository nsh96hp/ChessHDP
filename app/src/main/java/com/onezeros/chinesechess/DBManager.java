package com.onezeros.chinesechess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DBManager extends DatabaseHelper {
    Context context;
    public DBManager(Context context) {
        super(context);
    }

    public void Add_RESULT(ChessResult c){
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues values= new ContentValues();
        values.put(DatabaseHelper.TIME,c.getTime());
        values.put(DatabaseHelper.RESULT,c.getResult());
        values.put(DatabaseHelper.LEVEL,c.getLevel());
        db.insert(DatabaseHelper.TABLE_NAME,null,values);
        db.close();
    }

    public ArrayList<ChessResult> getResultAll(){
        SQLiteDatabase db= this.getWritableDatabase();
        String sql="SELECT * FROM "+DatabaseHelper.TABLE_NAME+";";
        Cursor cursor = db.rawQuery(sql,null);
        ArrayList<ChessResult> lst= new ArrayList<ChessResult>();
        if(cursor.moveToFirst()){
            do{
                ChessResult chess=new ChessResult(cursor.getInt(0),
                        cursor.getString(1),cursor.getInt(2),cursor.getInt(3));
                lst.add(chess);
            }while (cursor.moveToNext());
        }
        db.close();
        return lst;
    }

    public ArrayList<ChessResult> getResultWithLV(int lv){
        SQLiteDatabase db= this.getWritableDatabase();
        String sql="SELECT * FROM "+DatabaseHelper.TABLE_NAME+" WHERE "+DatabaseHelper.LEVEL+"="+lv+"";
        Cursor cursor = db.rawQuery(sql,null);
        ArrayList<ChessResult> lst= new ArrayList<ChessResult>();
        if(cursor.moveToFirst()){
            do{
                ChessResult chess=new ChessResult(cursor.getInt(0),
                        cursor.getString(1),cursor.getInt(2),cursor.getInt(3));
                lst.add(chess);
            }while (cursor.moveToNext());
        }
        db.close();
        return lst;
    }


}
