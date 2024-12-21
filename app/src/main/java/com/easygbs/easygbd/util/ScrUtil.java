package com.easygbs.easygbd.util;
import android.app.Activity;
import android.graphics.Rect;
import android.view.Window;

public class ScrUtil {

    public static int getStatusBarHeight(Activity activity){
        int barHeight = activity.getResources().getIdentifier("status_bar_height","dimen","android");
        if(barHeight > 0){
            return activity.getResources().getDimensionPixelSize(barHeight);
        }else{
            Rect rectangle= new Rect();
            Window window = activity.getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
            return rectangle.top;
        }
    }
}
