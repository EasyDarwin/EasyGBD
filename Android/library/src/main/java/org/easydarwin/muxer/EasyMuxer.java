package org.easydarwin.muxer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.util.Log;

import org.easydarwin.bus.StartRecord;
import org.easydarwin.bus.StopRecord;
import org.easydarwin.util.BUSUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 使用 MediaMuxer 合成音视频
 * <p>
 * Created by John on 2017/1/10.
 */
public class EasyMuxer {

    private static final String TAG = EasyMuxer.class.getSimpleName();

    private final String mFilePath;

    private MediaMuxer mMuxer;
    private final long durationMillis;

    private int index = 0;
    private int mVideoTrackIndex = -1;
    private int mAudioTrackIndex = -1;
    private long mBeginMillis;

    private MediaFormat mVideoFormat;
    private MediaFormat mAudioFormat;

    public EasyMuxer(String path, long durationMillis) {
        mFilePath = path;
        this.durationMillis = durationMillis;

        Object mux = null;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                // 初始化MediaMuxer
                mux = new MediaMuxer(path + "-" + index++ + ".mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mMuxer = (MediaMuxer) mux;

            // 通知UI 开始录像
            BUSUtil.BUS.post(new StartRecord());
        }
    }

    public synchronized void addTrack(MediaFormat format, boolean isVideo) {
        // now that we have the Magic Goodies, start the muxer
        if (mAudioTrackIndex != -1 && mVideoTrackIndex != -1)
            throw new RuntimeException("already add all tracks");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // 添加音频/视频的通道，此操作必须在mMuxer start方法之前调用:
            int track = mMuxer.addTrack(format);

            Log.i(TAG, String.format("addTrack %s result %d", isVideo ? "video" : "audio", track));

            if (isVideo) {
                mVideoFormat = format;
                mVideoTrackIndex = track;
                if (mAudioTrackIndex != -1) {
                    Log.i(TAG, "both audio and video added,and muxer is started");

                    mMuxer.start();
                    mBeginMillis = System.currentTimeMillis();
                }
            } else {
                mAudioFormat = format;
                mAudioTrackIndex = track;
                if (mVideoTrackIndex != -1) {
                    mMuxer.start();
                    mBeginMillis = System.currentTimeMillis();
                }
            }
        }
    }

    public synchronized void pumpStream(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo, boolean isVideo) {
        if (mAudioTrackIndex == -1 || mVideoTrackIndex == -1) {
            Log.i(TAG, String.format("pumpStream [%s] but muxer is not start.ignore..", isVideo ? "video" : "audio"));
            return;
        }

        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            // The codec config data was pulled out and fed to the muxer when we got
            // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
        } else if (bufferInfo.size != 0) {
            if (isVideo && mVideoTrackIndex == -1) {
                throw new RuntimeException("muxer hasn't started");
            }

            // adjust the ByteBuffer values to match BufferInfo (not needed?)
            outputBuffer.position(bufferInfo.offset);
            outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                /*
                 * 将H.264和AAC数据分别同时写入到MP4文件
                 *   BufferInfo对象的值一定要设置正确：
                 *       info.size 必须填入数据的大小
                 *       info.flags 需要给出是否为同步帧/关键帧
                 *       info.presentationTimeUs 必须给出正确的时间戳，注意单位是 us
                 * */
                mMuxer.writeSampleData(isVideo ? mVideoTrackIndex : mAudioTrackIndex, outputBuffer, bufferInfo);
            }

            Log.d(TAG, String.format("sent %s [" + bufferInfo.size + "] with timestamp:[%d] to muxer", isVideo ? "video" : "audio", bufferInfo.presentationTimeUs / 1000));
        }

        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            Log.i(TAG, "BUFFER_FLAG_END_OF_STREAM received");
        }

        if (System.currentTimeMillis() - mBeginMillis >= durationMillis) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Log.i(TAG, String.format("record file reach expiration.create new file:" + index));

                // 结束
                mMuxer.stop();
                mMuxer.release();
                mMuxer = null;
                mVideoTrackIndex = mAudioTrackIndex = -1;

                try {
                    mMuxer = new MediaMuxer(mFilePath + "-" + ++index + ".mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    addTrack(mVideoFormat, true);
                    addTrack(mAudioFormat, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void release() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (mMuxer != null) {
                if (mAudioTrackIndex != -1 && mVideoTrackIndex != -1) {
                    Log.i(TAG, String.format("muxer is started. now it will be stoped."));

                    try {
                        mMuxer.stop();
                        mMuxer.release();
                    } catch (IllegalStateException ex) {
                        ex.printStackTrace();
                    }

                    if (System.currentTimeMillis() - mBeginMillis <= 1500) {
                        new File(mFilePath + "-" + index + ".mp4").delete();
                    }

                    mAudioTrackIndex = mVideoTrackIndex = -1;
                    BUSUtil.BUS.post(new StopRecord());
                }
            }
        }
    }
}
