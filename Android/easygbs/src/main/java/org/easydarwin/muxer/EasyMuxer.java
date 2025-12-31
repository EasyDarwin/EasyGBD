package org.easydarwin.muxer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.util.Log;

import org.easydarwin.common.EasyGBDConstant;
import org.easydarwin.util.RecordStatusListener;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EasyMuxer {
    private static final String TAG = EasyMuxer.class.getSimpleName();

    public SimpleDateFormat ymdhmSimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.CHINA);

    private int channelId = 0;
    private String recordPath;
    private String mFilePath;

    private MediaMuxer mMediaMuxer;
    private final long durationMillis;

    private int mVideoTrackIndex = -1;
    private int mAudioTrackIndex = -1;
    private long mBeginMillis;

    private MediaFormat mVideoFormat;
    private MediaFormat mAudioFormat;

    public RecordStatusListener mNotifyStartStopRecordListener;

    public EasyMuxer(int channelId, String recordPath, long durationMillis, RecordStatusListener mNotifyStartStopRecordListener) {
        this.channelId = channelId;
        this.recordPath = recordPath;
        this.durationMillis = durationMillis;
        this.mNotifyStartStopRecordListener = mNotifyStartStopRecordListener;

        try {

            mFilePath = new File(recordPath, "CH" + (channelId + 1) + "_" + ymdhmSimpleDateFormat.format(new Date())).toString();

            mMediaMuxer = new MediaMuxer(mFilePath + ".mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (Exception e) {
            Log.e(TAG, "EasyMuxer  Exception  " + e.toString());
        } finally {
            Log.i(TAG, "EasyMuxer  finally");

            mNotifyStartStopRecordListener.msg(EasyGBDConstant.STARTRECORD);
        }
    }

    public synchronized void addTrack(MediaFormat format, boolean isVideo) {
        if (mAudioTrackIndex != -1 && mVideoTrackIndex != -1) throw new RuntimeException("already add all tracks");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            int track = mMediaMuxer.addTrack(format);

            Log.i(TAG, String.format("addTrack %s   %d", isVideo ? "video" : "audio", track));

            Log.i(TAG, "addTrack  isVideo  " + isVideo + "   mAudioTrackIndex  " + mAudioTrackIndex + "   mVideoTrackIndex   " + mVideoTrackIndex);
            if (isVideo) {
                mVideoFormat = format;
                mVideoTrackIndex = track;
                if (mAudioTrackIndex != -1) {
                    Log.i(TAG, "addTrack  1  both audio and video added,and muxer is started");

                    mMediaMuxer.start();
                    mBeginMillis = System.currentTimeMillis();
                }
            } else {
                mAudioFormat = format;
                mAudioTrackIndex = track;
                if (mVideoTrackIndex != -1) {
                    Log.i(TAG, "addTrack  2  both audio and video added,and muxer is started");
                    mMediaMuxer.start();
                    mBeginMillis = System.currentTimeMillis();
                }
            }
        }
    }

    public synchronized void pumpStream(ByteBuffer outputBuffer, MediaCodec.BufferInfo mBufferInfo, boolean isVideo) {
        if (mAudioTrackIndex == -1 || mVideoTrackIndex == -1) {
            Log.i(TAG, String.format("pumpStream %s  but muxer is not start", isVideo ? "video" : "audio"));
            return;
        }

        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            //The codec config data was pulled out and fed to the muxer when we got the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
        } else if (mBufferInfo.size != 0) {
            if (isVideo && mVideoTrackIndex == -1) {
                throw new RuntimeException("pumpStream  muxer hasn't started");
            }

            //adjust the ByteBuffer values to match BufferInfo (not needed?)
            outputBuffer.position(mBufferInfo.offset);
            outputBuffer.limit(mBufferInfo.offset + mBufferInfo.size);

            mMediaMuxer.writeSampleData(isVideo ? mVideoTrackIndex : mAudioTrackIndex, outputBuffer, mBufferInfo);
        }

        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            Log.i(TAG, "pumpStream  BUFFER_FLAG_END_OF_STREAM received");
        }

        if (System.currentTimeMillis() - mBeginMillis >= durationMillis) {
            mMediaMuxer.stop();
            mMediaMuxer.release();
            mMediaMuxer = null;
            mVideoTrackIndex = mAudioTrackIndex = -1;

            try {
                mFilePath = new File(recordPath, "CH" + (channelId + 1) + "_" + ymdhmSimpleDateFormat.format(new Date())).toString();
                mMediaMuxer = new MediaMuxer(mFilePath + ".mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

                addTrack(mVideoFormat, true);
                addTrack(mAudioFormat, false);
            } catch (Exception e) {
                Log.i(TAG, "pumpStream  Exception  " + e.toString());
            }
        }
    }

    public synchronized void release() {
        if (mMediaMuxer != null) {
            if (mAudioTrackIndex != -1 && mVideoTrackIndex != -1) {
                Log.i(TAG, String.format("release  muxer is started, now it will be stoped"));

                try {
                    mMediaMuxer.stop();
                    mMediaMuxer.release();
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                }

                if (System.currentTimeMillis() - mBeginMillis <= 1500) {
                    File mFile = new File(mFilePath + ".mp4");

                    boolean deleteRe = mFile.delete();
                    Log.i(TAG, "录制的上一个MP4音视频文件的时长不大于1500毫秒时，则删除该MP4音视频文件  release  deleteRe  " + deleteRe + "    mFile.getAbsolutePath()  " + mFile.getAbsolutePath());
                }

                mAudioTrackIndex = mVideoTrackIndex = -1;

                mNotifyStartStopRecordListener.msg(EasyGBDConstant.STOPRECORD);
            }
        }
    }

    public RecordStatusListener getmNotifyStartStopRecordListener() {
        return mNotifyStartStopRecordListener;
    }

    public void setmNotifyStartStopRecordListener(RecordStatusListener mNotifyStartStopRecordListener) {
        this.mNotifyStartStopRecordListener = mNotifyStartStopRecordListener;
    }
}
