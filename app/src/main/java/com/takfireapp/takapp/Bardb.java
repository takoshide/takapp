package com.takfireapp.takapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class Bardb extends SQLiteOpenHelper {

    //データベースのバージョン
    private static final int DATABASE_VERSION = 7;


    //データベース、テーブル、カラムの設定
    private static final String DATABASE_NAME = "BARDB.db";
    public static final String TABLE_NAME = "bardb";
//    private static final String _ID = "id";
    public static final String COLUMN_NAME_BARCODE = "barcode";
    public static final String COLUMN_NAME_COUNT = "count";
    public static final String COLUMN_NAME_PRODUCT = "product";
    public static final String COLUMN_NAME_CATEGORY = "category";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME +"(" +
//                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_BARCODE + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_COUNT + " TEXT," +
                    COLUMN_NAME_PRODUCT + " TEXT," +
                    COLUMN_NAME_CATEGORY + " TEXT)";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;



    //コンストラクタの設定
    Bardb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                SQL_CREATE_ENTRIES
        );
    }



    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(
                SQL_DELETE_ENTRIES
        );
        onCreate(db);
    }


    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }



}
