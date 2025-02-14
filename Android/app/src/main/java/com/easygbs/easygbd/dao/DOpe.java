package com.easygbs.easygbd.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.easygbs.easygbd.dao.bean.Chan;
import com.easygbs.easygbd.dao.ve.UVO;

import java.io.File;

public class DOpe extends SQLiteOpenHelper {
    private static final String TAG = DOpe.class.getSimpleName();
    private static DOpe instance = null;
    private final SQLiteDatabase mSQLiteDatabase;
    public static int VERSION = 1;
    public ChanOp mChanOp;

    private DOpe(Context mContext, String dir) {
        super(mContext, getName(dir), null, VERSION);

        mSQLiteDatabase = DOpe.this.getWritableDatabase();
        mChanOp = ChanOp.Instance(DOpe.this, mSQLiteDatabase);
    }

    public static DOpe Instance(Context mContext, String dir) {
        if (instance == null) {
            instance = new DOpe(mContext, dir);
        }

        return instance;
    }

    private static String getName(String dir) {
        String dn;
        String ph = dir + File.separator + "de";
        File db = new File(ph);
        if (!db.exists()) {
            db.mkdirs();
        }

        dn = ph + File.separator + "e.db";
        return dn;
    }

    @Override
    public void onCreate(SQLiteDatabase mSQLiteDatabase) {
        UVO.Instance().up(mSQLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase mSQLiteDatabase, int arg1, int arg2) {

    }

    public Object OperaDatabase(int value, Object... obj) {
        Object result = null;
        try {
            switch (value) {
                case DCon.Chanqueryct:
                    result = mChanOp.Chanqueryct();
                    break;
                case DCon.Chanjudinsert:
                    result = mChanOp.Chanjudinsert((Chan) obj[0]);
                    break;
                case DCon.Chaninsert:
                    result = mChanOp.MametInsert((Chan) obj[0]);
                    break;
                case DCon.Chanqueryrow:
                    result = mChanOp.Chanqueryrow((int) obj[0]);
                    break;
                case DCon.Chanqueryeffect:
                    result = mChanOp.Chanqueryeffect();
                    break;
                case DCon.Chanquerymaxuid:
                    result = mChanOp.Chanquerymaxuid();
                    break;
                case DCon.Chanupdatedependuid:
                    result = mChanOp.Chanupdatedependuid((Chan) obj[0]);
                    break;
                case DCon.ChanCount:
                    result = mChanOp.getCount();
                    break;
                case DCon.ChanDelete:
                    result = mChanOp.ChanDelete((Chan) obj[0]);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "OperaDatabase Exception  " + e);
        }

        return result;
    }
}