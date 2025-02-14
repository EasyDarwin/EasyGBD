package org.easydarwin.util;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {
    private static final String PREF_NAME = "easy_pref";

    private static final String K_RESOLUTION = "k_resolution";

    public static List<String> getSupportResolution(Context context) {
        List<String> resolutions = new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        String r = sharedPreferences.getString(K_RESOLUTION,"");

        if (!TextUtils.isEmpty(r)) {
            String[] arr = r.split(";");

            if (arr.length > 0) {
                resolutions = Arrays.asList(arr);
            }
        }

        return resolutions;
    }

    public static void saveSupportResolution(Context context, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(K_RESOLUTION, value).commit();
    }
}