package com.easygbs.easygbd.activity;

import Mp4Adapter
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.easygbs.easygbd.R
import com.easygbs.easygbd.StretchVideoView
import com.easygbs.easygbd.application.App
import com.easygbs.easygbd.databinding.ActivityRecordListBinding
import com.easygbs.easygbd.util.ScrUtil
import com.easygbs.easygbd.viewmodel.activity.RecordListViewModel
import com.gyf.immersionbar.ImmersionBar
import java.io.File


class RecordListActivity : BaseActivity() {
    var TAG: String = RecordListActivity::class.java.getSimpleName()

    lateinit var mRecordListViewModel: RecordListViewModel
    var mActivityRecordListBinding: ActivityRecordListBinding? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun init() {
        mActivityRecordListBinding =
            DataBindingUtil.setContentView(this@RecordListActivity, R.layout.activity_record_list)

        mRecordListViewModel = RecordListViewModel(App.getInstance())
        mRecordListViewModel.setRecordListActivity(this@RecordListActivity)

        mActivityRecordListBinding!!.mRecordListViewModel = mRecordListViewModel

        ImmersionBar.with(this).statusBarDarkFont(true).init()

        val layoutParams =
            mActivityRecordListBinding!!.root.layoutParams as LinearLayout.LayoutParams
        layoutParams.topMargin = ScrUtil.getStatusBarHeight(this@RecordListActivity)
        mActivityRecordListBinding!!.root.layoutParams = layoutParams

        loadRecordFiles()

    }

    private fun loadRecordFiles() {
        // 获取 MP4 文件列表
        val mp4Files = getMp4FilesInFilesDir()
        // 设置适配器
        mActivityRecordListBinding!!.rcvRecording.layoutManager =
            GridLayoutManager(this@RecordListActivity, 3)

        var mMp4Adapter = Mp4Adapter(this, mp4Files) { file ->
            // 点击播放视频
            showVideoDialog(file)
        }
        mActivityRecordListBinding!!.rcvRecording.adapter = mMp4Adapter
    }

    private fun getMp4FilesInFilesDir(): List<File> {
        // 获取应用内部 files 目录
        val filesDir = File(getExternalFilesDir(null), "easygbd")
        // 过滤出扩展名为 .mp4 的文件
        return filesDir.listFiles { _, name ->
            name.endsWith(".mp4", ignoreCase = true)
        }?.toList() ?: emptyList()
    }

    private fun showVideoDialog(file: File) {
        // 创建无边距的 Dialog
        val dialog = Dialog(this, R.style.FullWidthDialog)
        dialog.setContentView(R.layout.dialog_video_player)
        dialog.setCancelable(false)

        // 获取屏幕宽度
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels

        // 设置 Dialog 的宽高
        val window = dialog.window
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, // 宽度占满屏幕
            dpToPx(this, 280)                   // 高度：200dp
        )

        // 获取 VideoView
        val videoView: StretchVideoView = dialog.findViewById(R.id.videoView)

        // 设置 VideoView 播放路径
        videoView.setVideoURI(Uri.fromFile(file))
        videoView.setOnPreparedListener {
            videoView.start() // 视频准备好后自动播放
        }

        val closeRecordBtn: ImageView = dialog.findViewById(R.id.closeRecordBtn)

        closeRecordBtn.setOnClickListener(View.OnClickListener {
            dialog.cancel()
        })

        // 显示 Dialog
        dialog.show()
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun finish() {
        super.finish()
    }

}
