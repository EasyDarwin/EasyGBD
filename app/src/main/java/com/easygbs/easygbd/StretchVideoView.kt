package com.easygbs.easygbd

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class StretchVideoView(context: Context, attrs: AttributeSet? = null) : VideoView(context, attrs) {

    private lateinit var seekBar: SeekBar
    private lateinit var playPauseButton: ImageView
    private lateinit var remainingTimeText: TextView // 添加 TextView 显示剩余时间
    private lateinit var buttonContainer: LinearLayout
    private val progressHandler = Handler(Looper.getMainLooper())

    private var mPlaying = false;

    init {
        // 延迟初始化，确保父布局加载完成
        post {
            // 外部容器：用于设置半透明背景
            val outerContainer = FrameLayout(context).apply {
                setBackgroundColor(Color.parseColor("#99000000")) // 半透明背景
            }

            // 父布局是 RelativeLayout
            val parentLayout = parent as? RelativeLayout
            if (parentLayout != null) {
                // 初始化播放/暂停按钮，使用 ImageView 显示图片
                playPauseButton = ImageView(context).apply {
                    id = View.generateViewId() // 生成唯一ID
                    setImageResource(R.mipmap.playing) // 设置图片资源，替换为你的图标
                    setOnClickListener {
                        if (isPlaying) {
                            pause()
                            setImageResource(R.mipmap.pause)
                            progressHandler.removeCallbacksAndMessages(null) // 停止更新
                        } else {
                            start()
                            setImageResource(R.mipmap.playing)
                            startProgressHandler() // 启动进度条更新
                        }
                    }
                }

                // 初始化进度条
                seekBar = SeekBar(context).apply {
                    max = 0 // 初始值为 0，后续会动态设置
                    id = View.generateViewId()
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    ) // 设置宽度为 0，权重为 1，使其填满父布局
                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            if (fromUser) {
                                seekTo(progress)
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                            mPlaying = isPlaying
                            pause() // 用户拖动时暂停播放
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                            if (mPlaying) start()
                            updateProgress()
                            startProgressHandler() // 启动进度条更新
                        }
                    })
                }

                // 设置进度条未加载部分颜色为白色
                val layerDrawable = seekBar.progressDrawable as LayerDrawable
                // 设置进度条未加载部分的颜色
                val backgroundDrawable = layerDrawable.getDrawable(0)
                backgroundDrawable.setTint(Color.WHITE)


                // 初始化剩余时间显示的 TextView
                remainingTimeText = TextView(context).apply {
                    id = View.generateViewId()
                    setTextColor(Color.WHITE) // 设置文本颜色为白色
                    textSize = 16f // 设置文本大小
                    text = formatTime(duration) // 初始显示剩余时间
                }


                // 初始化 LinearLayout 容器
                buttonContainer = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL // 设置为水平布局
                    gravity = Gravity.CENTER_VERTICAL // 内容居中
                    id = View.generateViewId() // 生成唯一ID
                    setPadding(50, 32, 50, 32) // 内边距
                    addView(playPauseButton) // 将图片按钮添加到容器中
                    addView(seekBar) // 将进度条添加到容器中
                    addView(remainingTimeText) // 将剩余时间文本添加到容器中
                }


                // 将 LinearLayout 添加到外部容器
                outerContainer.addView(buttonContainer)

                // 设置外部容器的布局参数，将其定位到底部
                val layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(RelativeLayout.ALIGN_PARENT_BOTTOM) // 底部对齐
                    bottomMargin = 0 // 距离底部的间距（可根据需求调整）
                }

                // 将外部容器添加到父布局
                parentLayout.addView(outerContainer, layoutParams)

                setListener()
            }
        }
    }

    private fun setListener() {
        // 设置视频播放完成的监听器
        setOnCompletionListener {
            // 播放完成后，更新按钮图片为播放图标
            playPauseButton.setImageResource(R.mipmap.pause)
            seekBar.progress = 0 // 重置进度条
            remainingTimeText.text = formatTime(duration) // 重置剩余时间文本
            progressHandler.removeCallbacksAndMessages(null) // 停止更新任务
        }

        setOnPreparedListener {
            start()
            playPauseButton.setImageResource(R.mipmap.playing) // 更新为暂停按钮
            seekBar.max = duration // 设置 SeekBar 最大值为视频总时长
            Log.d("StretchVideoView", "duration: $duration")
            remainingTimeText.text = formatTime(duration) // 初始化显示剩余时间
            startProgressHandler() // 启动进度条更新
        }
    }

    fun updateProgress() {
        val progress = (currentPosition.toFloat() / duration * 100).toInt()
        seekBar.progress = currentPosition // 更新进度条
        remainingTimeText.text = formatTime(duration - currentPosition) // 初始化显示剩余时间
        Log.d(
            "StretchVideoView",
            "Current: $currentPosition, Duration: $duration, Progress: $progress"
        )
    }

    // 更新进度条
    private fun startProgressHandler() {
        progressHandler.postDelayed(object : Runnable {
            override fun run() {
                updateProgress();
                progressHandler.postDelayed(this, 1000) // 每 500 毫秒更新一次
            }
        }, 200)
    }

    // 格式化时间为分钟:秒的形式
    private fun formatTime(timeInMillis: Int): String {
        val seconds = timeInMillis / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    // 重写 onMeasure，强制拉伸 VideoView
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}



