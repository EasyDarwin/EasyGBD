package com.easygbs.easygbd.util;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import pub.devrel.easypermissions.helper.BaseSupportPermissionsHelper;

public class PeProxy<T> extends BaseSupportPermissionsHelper {
    public String TAG= PeProxy.class.getSimpleName();

    private BaseSupportPermissionsHelper<T> permissionsHelper;

    @SuppressLint("RestrictedApi")
    public PeProxy(BaseSupportPermissionsHelper<T> permissionHelper) {
        super(permissionHelper.getHost());
        this.permissionsHelper = permissionHelper;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public FragmentManager getSupportFragmentManager() {
        return permissionsHelper.getSupportFragmentManager();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void directRequestPermissions(int requestCode, @NonNull String... perms) {
        permissionsHelper.directRequestPermissions(requestCode,perms);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String perm) {
        return permissionsHelper.shouldShowRequestPermissionRationale(perm);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public Context getContext() {
        return permissionsHelper.getContext();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void showRequestPermissionRationale(@NonNull String rationale, @NonNull String positiveButton, @NonNull String negativeButton, int theme, int requestCode, @NonNull String... perms) {

    }
}
