package com.mp4;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 完整 Mp4Extractor（时间统一为毫秒，支持 ISO 8601251）
 * - 多 MP4 文件连续解析，无感切换
 * - 支持 startTimeStr/endTimeStr 自动计算 seekMs 和结束
 * - 视频回调附加 SPS/PPS
 * - 支持倍速播放（默认 1.0f 倍速）
 * - 修复倍速切换时的负值sleep问题
 */
public class Mp4Extractor {

    private static final String TAG = "Mp4Extractor";

    public interface Callback {
        void onVideoSample(byte[] data, int size, boolean isKeyFrame);

        void onAudioSample(byte[] data, int size);

        void onMediaInfo(MediaInfo info, String path);

        void onCompleted();

        void onCanceled();
    }

    private final Object pauseLock = new Object();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile boolean isPaused = false;
    private volatile boolean isCanceled = false;

    private Callback callback;
    private List<File> videoFiles = new ArrayList<>();
    private boolean isFileDown = false;


    private volatile float playSpeed = 1.0f; // 播放倍速，默认1倍

    public static class MediaInfo {
        public long durationSec;
        public int width;
        public int height;
        public float frameRate;
        public String videoMime;
        public String audioMime;
        public int audioSampleRate;
        public int audioChannelCount;

        @Override
        public String toString() {
            return "MediaInfo{" + "durationSec=" + durationSec + ", width=" + width + ", height=" + height + ", frameRate=" + frameRate + ", videoMime='" + videoMime + '\'' + ", audioMime='" + audioMime + '\'' + ", audioSampleRate=" + audioSampleRate + ", audioChannelCount=" + audioChannelCount + '}';
        }
    }

    // ===========================
    //         公共方法
    // ===========================
    public void pause() {
        isPaused = true;
    }

    public void resume() {
        synchronized (pauseLock) {
            isPaused = false;
            pauseLock.notifyAll();
        }
    }

    public void cancel() {
        isCanceled = true;
        setPlaySpeed(1.0f);
        resume();
    }

    public boolean getIsPaused() {
        return isPaused;
    }

    /**
     * 设置播放倍速
     *
     * @param speed 倍速值，例如 0.5f 表示0.5倍速，1.0f 表示正常速度，2.0f表示2倍速
     */
    public void setPlaySpeed(float speed) {
        if (speed > 0 && speed != this.playSpeed) {
            this.playSpeed = speed;
            Log.d(TAG, "设置播放倍速: " + speed);
        }
    }

    /**
     * 获取当前播放倍速
     */
    public float getPlaySpeed() {
        return playSpeed;
    }

    /**
     * 启动解析
     *
     * @param folder       父目录路径
     * @param startTimeStr 查询开始时间
     * @param endTimeStr   查询结束时间
     * @param cb           回调
     */
    public void start(String folder, String startTimeStr, String endTimeStr, Callback cb) {
        this.callback = cb;
        isPaused = false;
        isCanceled = false;

        // 1. 过滤文件
        videoFiles = Mp4Filter.filterByTime(folder, startTimeStr, endTimeStr);

        if (videoFiles.isEmpty()) {
            Log.e(TAG, "⚠️ 未找到符合时间的 MP4 文件！");
            postMain(() -> {
                if (callback != null) callback.onCompleted();
            });
            return;
        }

        Log.d(TAG, "过滤到 MP4 数量 = " + videoFiles.size() + ",startTimeStr=" + startTimeStr + ",endTimeStr = " + endTimeStr);
        for (File f : videoFiles) {
            Log.d(TAG, "结果文件: " + f.getAbsolutePath());
        }

        // 2. 计算第一个文件的起始毫秒
        long seekMs = 0;
        File firstFile = videoFiles.get(0);
        long fileStartMs = getFileStartMs(firstFile);
        long targetStartMs = parseTimeString(startTimeStr);
        long targetEndMs = parseTimeString(endTimeStr);

        if (targetStartMs > fileStartMs) {
            seekMs = targetStartMs - fileStartMs;
        }

        final long finalSeekMs = seekMs;

        // 3. 提交解析任务
        executor.execute(() -> extractLoop(videoFiles, finalSeekMs, targetEndMs));
    }

    public void start(String folder, String startTimeStr, String endTimeStr, boolean isFileDown, Callback cb) {
        this.callback = cb;
        this.isFileDown = isFileDown;
        isPaused = false;
        isCanceled = false;

        // 1. 过滤文件
        videoFiles = Mp4Filter.filterByTime(folder, startTimeStr, endTimeStr);

        if (videoFiles.isEmpty()) {
            Log.e(TAG, "⚠️ 未找到符合时间的 MP4 文件！");
            postMain(() -> {
                if (callback != null) callback.onCompleted();
            });
            return;
        }

        Log.d(TAG, "过滤到 MP4 数量 = " + videoFiles.size() + ",startTimeStr=" + startTimeStr + ",endTimeStr = " + endTimeStr);
        for (File f : videoFiles) {
            Log.d(TAG, "结果文件: " + f.getAbsolutePath());
        }

        // 2. 计算第一个文件的起始毫秒
        long seekMs = 0;
        File firstFile = videoFiles.get(0);
        long fileStartMs = getFileStartMs(firstFile);
        long targetStartMs = parseTimeString(startTimeStr);
        long targetEndMs = parseTimeString(endTimeStr);

        if (targetStartMs > fileStartMs) {
            seekMs = targetStartMs - fileStartMs;
        }

        final long finalSeekMs = seekMs;

        // 3. 提交解析任务
        executor.execute(() -> extractLoop(videoFiles, finalSeekMs, targetEndMs));
    }

    // ===========================
    //       内部解析方法
    // ===========================
    private void extractLoop(List<File> files, long firstSeekMs, long endMs) {
        for (int i = 0; i < files.size(); i++) {
            if (isCanceled) break;

            File file = files.get(i);
            long startMs = (i == 0) ? firstSeekMs : 0L;

            long endMsInFile = 0L;
            if (i == files.size() - 1) { // 最后一个文件需要截止
                long fileStartMs = getFileStartMs(file);
                endMsInFile = Math.max(0, endMs - fileStartMs);
            }

            Log.i(TAG, "开始解析文件: " + file.getName() + ", startMs =" + startMs + " ,endMsInFile = " + endMsInFile);
            extractSingleFile(file, startMs, endMsInFile);
        }

        postMain(() -> {
            if (isCanceled) {
                if (callback != null) callback.onCanceled();
            } else {
                if (callback != null) callback.onCompleted();
            }
        });
    }

    private void extractSingleFile(File file, long startMs, long endMsInFile) {
        if (file == null || !file.exists() || !file.isFile()) return;

        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "设置数据源失败: " + file.getAbsolutePath(), e);
            return;
        }

        int videoTrack = -1;
        int audioTrack = -1;
        ByteBuffer sps = null;
        ByteBuffer pps = null;

        MediaInfo mediaInfo = new MediaInfo();

        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);

            if (mime != null && mime.startsWith("video/")) {
                videoTrack = i;
                mediaInfo.videoMime = mime;
                mediaInfo.width = format.containsKey(MediaFormat.KEY_WIDTH) ? format.getInteger(MediaFormat.KEY_WIDTH) : 0;
                mediaInfo.height = format.containsKey(MediaFormat.KEY_HEIGHT) ? format.getInteger(MediaFormat.KEY_HEIGHT) : 0;
                mediaInfo.frameRate = format.containsKey(MediaFormat.KEY_FRAME_RATE) ? format.getInteger(MediaFormat.KEY_FRAME_RATE) : 0f;
                sps = format.containsKey("csd-0") ? format.getByteBuffer("csd-0") : null;
                pps = format.containsKey("csd-1") ? format.getByteBuffer("csd-1") : null;
            } else if (mime != null && mime.startsWith("audio/")) {
                audioTrack = i;
                mediaInfo.audioMime = mime;
                mediaInfo.audioSampleRate = format.containsKey(MediaFormat.KEY_SAMPLE_RATE) ? format.getInteger(MediaFormat.KEY_SAMPLE_RATE) : 0;
                mediaInfo.audioChannelCount = format.containsKey(MediaFormat.KEY_CHANNEL_COUNT) ? format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) : 0;
            }

            if (format.containsKey(MediaFormat.KEY_DURATION)) {
                long durationUs = format.getLong(MediaFormat.KEY_DURATION);
                mediaInfo.durationSec = durationUs / 1_000_000L;
            }
        }

        postMain(() -> {
            if (callback != null) callback.onMediaInfo(mediaInfo, file.getAbsolutePath());
        });

        if (videoTrack >= 0) extractor.selectTrack(videoTrack);
        if (audioTrack >= 0) extractor.selectTrack(audioTrack);

        if (startMs > 0) {
            extractor.seekTo(startMs * 1000L, MediaExtractor.SEEK_TO_NEXT_SYNC);
        }

        int maxBuf = Math.max(Math.max(mediaInfo.width, 1) * Math.max(mediaInfo.height, 1) * 2, 512 * 1024);
        ByteBuffer buffer = ByteBuffer.allocateDirect(maxBuf);
        byte[] sampleData = new byte[maxBuf];


        while (true) {
            if (isCanceled) break;

            if (isPaused) {
                synchronized (pauseLock) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
                continue;
            }

            int trackIndex = extractor.getSampleTrackIndex();
            if (trackIndex < 0) break;

            long ptsUs = extractor.getSampleTime();
            long ptsMs = ptsUs / 1000L;

            if (endMsInFile > 0 && ptsMs > endMsInFile) break; // 最后一个文件截止

            buffer.clear();
            int size = extractor.readSampleData(buffer, 0);
            if (size <= 0) {
                extractor.advance();
                continue;
            }

            int flags = extractor.getSampleFlags();
            boolean isKey = (flags & MediaExtractor.SAMPLE_FLAG_SYNC) != 0;

//            //  8.0倍速   跳过非关键帧，继续读取下一帧
//            if (playSpeed >= 8.0f && trackIndex == videoTrack && !isKey && !this.isFileDown) {
//                extractor.advance();
//                continue;
//            }

            buffer.get(sampleData, 0, size);

            long sleepMs = getSleepMs(mediaInfo.frameRate, playSpeed);

            Log.d(TAG, "sleepMs = " + sleepMs + ",playSpeed=" + playSpeed + ",frameRate=" + mediaInfo.frameRate + ",isKeyFrame=" + isKey);

            // 只有正值才等待，负值表示落后了，立即播放
            if (sleepMs > 0) {
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }

            // 视频回调
            if (trackIndex == videoTrack) {
                byte[] out;
                int outSize;
                if (isKey && sps != null && pps != null) {
                    int spsLen = sps.remaining(), ppsLen = pps.remaining();
                    out = new byte[spsLen + ppsLen + size];
                    sps.mark();
                    sps.get(out, 0, spsLen);
                    sps.reset();
                    pps.mark();
                    pps.get(out, spsLen, ppsLen);
                    pps.reset();
                    System.arraycopy(sampleData, 0, out, spsLen + ppsLen, size);
                    outSize = out.length;
                } else {
                    out = new byte[size];
                    System.arraycopy(sampleData, 0, out, 0, size);
                    outSize = size;
                }

                final byte[] finalOut = out;
                final int finalOutSize = outSize;
                final boolean finalKey = isKey;
                postMain(() -> {
                    if (callback != null) callback.onVideoSample(finalOut, finalOutSize, finalKey);
                });
            }
            // 音频回调
            else if (trackIndex == audioTrack) {
                // 8倍速时不发送音频或发送静音音频
                if (playSpeed == 1.0f) {
                    final byte[] audioData = Arrays.copyOf(sampleData, size);
                    final int audioSize = size;
                    postMain(() -> {
                        if (callback != null) callback.onAudioSample(audioData, audioSize);
                    });
                }
            }

            extractor.advance();
        }

        extractor.release();
        Log.d(TAG, "文件解析完成: " + file.getName());
    }

    //8倍速 以上只发关键帧
    private Long getSleepMs(float frameRate, float playSpeed) {
        if (isFileDown) return (long) (1000L / frameRate / playSpeed);
//        if (playSpeed == 8.0f) return 1000L / 8; // 8倍速立即发送关键帧
        else if (playSpeed == 4.0f) return (long) (1000L / frameRate / playSpeed) + 1L;
        else if (playSpeed == 2.0f) return (long) (1000L / frameRate / playSpeed);
        else if (playSpeed == 1.0f) return (long) (1000L / frameRate / playSpeed) - 4L;
        return (long) (1000L / frameRate / playSpeed);
    }

    // ===========================
    //         工具方法
    // ===========================
    private void postMain(Runnable r) {
        mainHandler.post(r);
    }

    private long getFileStartMs(File file) {
        if (file == null) return 0L;
        String name = file.getName();
        String fileTimeStr = name.replaceAll("^.*_(\\d{17})\\.mp4$", "$1");
        return parseTimeString(fileTimeStr);
    }

    /**
     * 支持三种格式：
     * 1. 文件名格式 yyyyMMddHHmmssSSS
     * 2. ISO 8601 格式 yyyy-MM-dd'T'HH:mm:ss[.SSS]
     * 3. 毫秒时间戳
     */
    public static long parseTimeString(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return 0L;

        // 1. 尝试 yyyyMMddHHmmssSSS
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
            Date d = sdf.parse(timeStr);
            if (d != null) return d.getTime();
        } catch (ParseException ignored) {
        }

        // 2. 尝试 ISO 8601 格式
        try {
            SimpleDateFormat sdfIsoMs = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
            Date d = sdfIsoMs.parse(timeStr);
            if (d != null) return d.getTime();
        } catch (ParseException ignored) {
        }
        try {
            SimpleDateFormat sdfIso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date d = sdfIso.parse(timeStr);
            if (d != null) return d.getTime();
        } catch (ParseException ignored) {
        }

        // 3. 尝试毫秒时间戳
        try {
            return Long.parseLong(timeStr);
        } catch (NumberFormatException ignored) {
        }

        return 0L;
    }

    /**
     * 销毁资源
     */
    public void destroy() {
        cancel();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }
}
