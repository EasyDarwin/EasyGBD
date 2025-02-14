package com.easygbs.easygbd.util;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import java.lang.reflect.Field;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;
import pub.devrel.easypermissions.helper.BaseSupportPermissionsHelper;

public class PeUtil {
    private static String TAG= PeUtil.class.getSimpleName();
    public static final int STORAGE=1;
    public static final int CAMERA=2;
    public static final int RECORDAUDIO=3;
    public static final int INTERNET=4;
    public static final int LOC=5;

    public static boolean ishasPer(Context context,String permission){
        return EasyPermissions.hasPermissions(context, permission);
    }

    public static void requestPer(Activity activity,int code){
        PermissionRequest request=null;
        switch(code){
            case STORAGE:
                 request = new PermissionRequest.Builder(activity, code, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                    .setRationale("启用存储权限")
                    .setPositiveButtonText("确定")
                    .setNegativeButtonText("取消")
                    .build();
            break;
            case CAMERA:
                 request = new PermissionRequest.Builder(activity, code, Manifest.permission.CAMERA)
                        .setRationale("启用相机权限")
                        .setPositiveButtonText("确定")
                        .setNegativeButtonText("取消")
                        .build();
                break;
            case RECORDAUDIO:
                request = new PermissionRequest.Builder(activity, code, Manifest.permission.RECORD_AUDIO)
                        .setRationale("启用录音权限")
                        .setPositiveButtonText("确定")
                        .setNegativeButtonText("取消")
                        .build();
                break;
            case INTERNET:
                request = new PermissionRequest.Builder(activity, code, Manifest.permission.RECORD_AUDIO)
                        .setRationale("启用网络权限")
                        .setPositiveButtonText("确定")
                        .setNegativeButtonText("取消")
                        .build();
                break;
            case LOC:
                request = new PermissionRequest.Builder(activity, code, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)
                        .setRationale("启用位置权限")
                        .setPositiveButtonText("确定")
                        .setNegativeButtonText("取消")
                        .build();
                break;
        }

        try{
            Class clazz = request.getClass();
            Field field = clazz.getDeclaredField("mHelper");
            field.setAccessible(true);
            @SuppressLint("RestrictedApi")
            BaseSupportPermissionsHelper<AppCompatActivity> permissionHelper = (BaseSupportPermissionsHelper<AppCompatActivity>) field.get(request);
            field.set(request, new PeProxy<AppCompatActivity>(permissionHelper));
        }catch(Exception e){
            Log.e(TAG,"requestPer  Exception  "+e.toString());
        }

        EasyPermissions.requestPermissions(request);
    }
}
