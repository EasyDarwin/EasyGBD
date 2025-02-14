package com.journeyapps.util;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.Window;

import com.gyf.immersionbar.ImmersionBar;

public class StatusBarUtils {

    //获取状态栏的高度
    public static int getStatusBarHeight(Activity activity){
        int barHeight = getStatusBarHeight2(activity);
        if(barHeight == 0){
            return getStatusBarHeight1(activity);
        }
        return barHeight;
    }

    public static int getStatusBarHeight1(Activity activity){
        Rect rectangle= new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        return rectangle.top;
    }

    public static int getStatusBarHeight2(Activity activity){
        int resourceId = activity.getResources().getIdentifier("status_bar_height","dimen","android");
        if(resourceId > 0){
            return activity.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    //透明颜色
    public static void setStatusBarTransparent(Activity activity){
        ImmersionBar.with(activity)
                .statusBarDarkFont(true)  //黑色字体
                .init();
    }

    //恢复为透明颜色
    public static void setStatusBarTransparentHasReset(Activity activity){
        ImmersionBar.with(activity)
                .reset()
                .statusBarDarkFont(true)  //黑色字体
                .init();
    }

    //状态栏背景颜色为指定的颜色，白色字体
    public static void setStatusBarWhiteFont(Activity activity, int color){
        ImmersionBar.with(activity)
                .statusBarColor(color)       //状态栏颜色，不写默认透明色
                .statusBarDarkFont(false)    //白色字体
                .init();
    }

    //状态栏背景颜色为指定的颜色，黑色字体
    public static void setStatusBarBlackFont(Activity activity, int color){
        ImmersionBar.with(activity)
                .statusBarColor(color)       //状态栏颜色，不写默认透明色
                .statusBarDarkFont(true)     //黑色字体
                .init();
    }

    //顶部布局在状态栏之下，状态栏背景颜色为指定的颜色，黑色字体
    public static void setStatusBarBlackFonttitleBarMarginTop(Activity activity, int color, View view){
        ImmersionBar.with(activity)
                .statusBarColor(color)       //状态栏颜色，不写默认透明色
                .titleBarMarginTop(view)     //解决状态栏和布局重叠问题，任选其一
                .statusBarDarkFont(true)     //黑色字体
                .init();
    }

}
