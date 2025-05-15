package com.easygbs.easygbd.activity

import ViewPagerAdapter
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.easygbs.easygbd.R
import com.easygbs.easygbd.StretchVideoView
import com.easygbs.easygbd.application.App
import com.easygbs.easygbd.databinding.ActivityRecordListBinding
import com.easygbs.easygbd.util.ScrUtil
import com.easygbs.easygbd.viewmodel.activity.RecordListViewModel
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class RecordListActivity : BaseActivity() {
    private val TAG = RecordListActivity::class.java.simpleName
    private lateinit var mRecordListViewModel: RecordListViewModel
    private lateinit var mActivityRecordListBinding: ActivityRecordListBinding
    lateinit var mViewPagerAdapter: ViewPagerAdapter

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun init() {
        // 初始化布局绑定
        mActivityRecordListBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_record_list)

        // 初始化 ViewModel
        mRecordListViewModel = RecordListViewModel(App.getInstance())
        mRecordListViewModel.setRecordListActivity(this)

        mActivityRecordListBinding.mRecordListViewModel = mRecordListViewModel

        // 设置状态栏样式
        ImmersionBar.with(this)
            .statusBarColor(R.color.d9f4eb) // 设置状态栏背景颜色为白色
            .statusBarDarkFont(true)
            .init()

        // 设置根布局顶部边距适配状态栏高度
        mActivityRecordListBinding.root.layoutParams =
            (mActivityRecordListBinding.root.layoutParams as ViewGroup.MarginLayoutParams).apply {
                topMargin = ScrUtil.getStatusBarHeight(this@RecordListActivity)
            }

        // 加载录像文件列表
        loadRecordFilesAsync()

    }

    /**
     * 异步加载视频文件
     */
    private fun loadRecordFilesAsync() {
        // 使用 CoroutineScope 在主线程中启动协程
        CoroutineScope(Dispatchers.Main).launch {
            // 显示加载中的 UI
//            showLoading()

            // 在 IO 线程中加载视频文件
            val mp4Files = withContext(Dispatchers.IO) {
                getMp4FilesInFilesDir()
            }

            // 分页处理：每页 18 个视频
            val videoPages = mp4Files.chunked(18)

            // 更新 UI
            mViewPagerAdapter = setupViewPager(videoPages)
//            hideLoading()
        }
    }


    /**
     * 设置 ViewPager2
     */
    private fun setupViewPager(videoPages: List<List<File>>): ViewPagerAdapter {
        val adapter = ViewPagerAdapter(this@RecordListActivity,
            videoPages,
            mActivityRecordListBinding.llRoot,
            { file ->
                showVideoDialog(file)
            },
            {
                updatePageUI()
            }
        )

        val viewPager = mActivityRecordListBinding.rcvRecording
        viewPager.adapter = adapter
        viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        return adapter
    }


    private fun getMp4FilesInFilesDir(): List<File> {
        // 获取应用内部的 files 目录
        val filesDir = File(getExternalFilesDir(null), "easygbd")

        // 过滤出扩展名为 .mp4 的文件，并按文件最后修改时间倒序排列
        return filesDir.listFiles { _, name ->
            name.endsWith(".mp4", true)
        }?.sortedByDescending { it.lastModified() } ?: emptyList()

    }

    private fun showVideoDialog(file: File) {
        // 创建无边距的 Dialog
        val dialog = Dialog(this, R.style.FullWidthDialog).apply {
            setContentView(R.layout.dialog_video_player)
            setCancelable(false)
        }

        // 获取屏幕宽度
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        // 设置 Dialog 宽高
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, // 宽度占满屏幕
            dpToPx(this, 280) // 高度 280dp
        )

        // 获取 VideoView
        val videoView: StretchVideoView = dialog.findViewById(R.id.videoView)
        videoView.setVideoURI(Uri.fromFile(file))
        videoView.setOnPreparedListener { videoView.start() }

        // 关闭按钮
        dialog.findViewById<ImageView>(R.id.closeRecordBtn).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updatePageUI() {
        loadRecordFilesAsync()
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }


//    private fun View.setOnClickListener() {
//        mViewPagerAdapter.mVideoGridAdapter.allSelection()
//    }
}

