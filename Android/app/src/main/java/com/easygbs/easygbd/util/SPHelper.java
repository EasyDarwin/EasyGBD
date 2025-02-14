package com.easygbs.easygbd.util;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SPHelper {
    private static String TAG= SPHelper.class.getSimpleName();
    private static SPHelper _instance = null;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public static SPHelper instance(Context mContext) {
        if (_instance == null) {
            _instance = new SPHelper(mContext);
        }
        return _instance;
    }

    public SPHelper(Context context) {
        try {
            mSharedPreferences = context.getSharedPreferences("data",Activity.MODE_PRIVATE);
            mEditor = mSharedPreferences.edit();
        } catch (Exception e) {
            Log.e(TAG,"SPHelper  Exception  "+e.toString());
        }
    }

    public boolean put(String key, Object object) {
        if (object instanceof String) {
            mEditor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            mEditor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            mEditor.putBoolean(key, (Boolean) object);
        } else {
            mEditor.putString(key, object.toString());
        }
        return mEditor.commit();
    }

    public Object get(String key, Object defaultObject) {
        if (defaultObject instanceof String) {
            return mSharedPreferences.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return mSharedPreferences.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return mSharedPreferences.getBoolean(key, (Boolean) defaultObject);
        } else {
            return mSharedPreferences.getString(key, null);
        }
    }
}