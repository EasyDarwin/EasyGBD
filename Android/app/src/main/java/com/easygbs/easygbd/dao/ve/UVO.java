package com.easygbs.easygbd.dao.ve;
import android.database.sqlite.SQLiteDatabase;

import com.easygbs.easygbd.dao.TCon;

public class UVO {
    private static final String TAG = UVO.class.getSimpleName();
    private static UVO instance = null;
    private SQLiteDatabase mSQLiteDatabase;

    public static UVO Instance(){
        if(instance == null){
            instance = new UVO();
        }
        return instance;
    }

    public UVO(){

    }

    public void up(SQLiteDatabase mSQLiteDatabase){
        this.mSQLiteDatabase = mSQLiteDatabase;
        mSQLiteDatabase.execSQL(TCon.chan);
    }
}