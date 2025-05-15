package com.easygbs.easygbd.viewmodel.activity

import android.app.Application
import android.content.Intent
import android.view.View
import com.easygbs.easygbd.activity.RecordListActivity
import com.easygbs.easygbd.common.Constant
import com.easygbs.easygbd.viewmodel.BaseViewModel


class RecordListViewModel(application: Application) : BaseViewModel(application) {
    var TAG: String = RecordListViewModel::class.java.getSimpleName()
    var mRecordListActivity: RecordListActivity? = null

    fun setRecordListActivity(mMainActivity: RecordListActivity) {
        this.mRecordListActivity = mMainActivity
    }

    fun back(view: View) {

        if (mRecordListActivity!!.mViewPagerAdapter != null && mRecordListActivity!!.mViewPagerAdapter!!.mVideoGridAdapter!!.isSelectionMode) {
            mRecordListActivity!!.mViewPagerAdapter!!.mVideoGridAdapter!!.changeSelectionMode()
            mRecordListActivity!!.mViewPagerAdapter!!.mVideoGridAdapter!!.clearSelection()
        } else {
            val resultIntent = Intent()
            resultIntent.putExtra("key", "value") // 将数据放入 Intent
            mRecordListActivity?.setResult(Constant.RECORD_BACK, resultIntent) // 返回数据，并指示操作成功
            mRecordListActivity?.finish()
        }
    }
}