package com.easygbs.easygbd.viewmodel.activity

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.easygbs.easygbd.R
import com.easygbs.easygbd.activity.MainActivity
import com.easygbs.easygbd.push.MediaStream
import com.easygbs.easygbd.viewmodel.BaseViewModel

class MainViewModel(application: Application) : BaseViewModel(application) {
    var TAG: String = MainViewModel::class.java.getSimpleName()
    var mMainActivity: MainActivity? = null
    val selectTopItem: MutableLiveData<Int> = MutableLiveData<Int>(0)
    public val selectBottomItem: MutableLiveData<Int> = MutableLiveData<Int>(-1)
    var isshowbottomObservableField = ObservableField<Boolean>(true)

    fun setMainActivity(mMainActivity: MainActivity) {
        this.mMainActivity = mMainActivity
    }

    fun topfirst() {
        selectTopItem.value = 0
    }

    fun topfirst(v: View) {
        selectTopItem.value = 0
    }

    fun topsecond(v: View) {
        selectTopItem.value = 1
    }

    fun topthird(v: View) {
        selectTopItem.value = 2
    }

    fun bf() {
        try {
            selectBottomItem.value = 0
            mMainActivity!!.mBasicSettingsFragment!!.save()
            mMainActivity!!.mBasicSettingsFragment!!.startOrStopStream()
        } catch (e: Exception) {
            Log.e(TAG, "bf  Exception  " + e.toString());
        }
    }


    fun bf(v: View) {
        try {
            selectBottomItem.value = 0
            mMainActivity!!.mBasicSettingsFragment!!.save()
            mMainActivity!!.mBasicSettingsFragment!!.startOrStopStream()
        } catch (e: Exception) {
            Log.e(TAG, "bf  Exception  " + e.toString());
        }
    }

    private var lastClickTime: Long = 0

    fun bs(v: View) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < 1000) {
            return
        }
        mMainActivity!!.mMediaStream!!.stopStream();
        mMainActivity!!.mActivityMainlandBinding!!.ivbottomfirst!!.setBackgroundResource(R.mipmap.ic_unreg)
        mMainActivity!!.mActivityMainlandBinding!!.tvbottomfirst!!.setTextColor(
            mMainActivity!!.resources.getColor(
                R.color.color_808080
            )
        )
        lastClickTime = currentTime  // 更新最后一次点击时间

        try {
            selectBottomItem.value = 1
        } catch (e: Exception) {
            Log.e(TAG, "bs Exception " + e.toString())
        }
    }

    fun bt(v: View) {
        selectBottomItem.value = 2
    }
}