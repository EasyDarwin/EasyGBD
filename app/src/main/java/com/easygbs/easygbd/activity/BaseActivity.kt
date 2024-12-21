package com.easygbs.easygbd.activity
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import org.greenrobot.eventbus.EventBus
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks

abstract class BaseActivity: FragmentActivity(), PermissionCallbacks {
    var BaseActivityTAG: String = BaseActivity::class.java.getSimpleName()

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    abstract fun init()

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int,perms: List<String?>) {
        Pergran(requestCode)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        for (i in perms.indices) {
            var d: Boolean = EasyPermissions.somePermissionDenied(this,perms.get(i))
            if (d) {
                val deniedPermission = perms[i]
                Perteden(deniedPermission)
            }
        }

        var sp:Boolean=EasyPermissions.somePermissionPermanentlyDenied(this,perms)
        if (sp) {
            for (j in perms.indices) {
                val per = perms[j]
                Perpeden(per)
            }
        }
    }

    open fun Pergran(requestCode: Int) {}

    open fun Perteden(deniedPermission: String) {}

    open fun Perpeden(deniedPermission: String) {}

}