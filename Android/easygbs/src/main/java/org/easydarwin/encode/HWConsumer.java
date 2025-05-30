package org.easydarwin.encode;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.easygbs.BuildConfig;
import com.easygbs.Device;

import org.easydarwin.muxer.EasyMuxer;
import org.easydarwin.push.Pusher;
import org.easydarwin.sw.JNIUtil;
import org.easydarwin.util.SPUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar;

/**
 * MediaCodec视频硬编码器
 * <p>
 * Created by apple on 2017/5/13.
 */
public class HWConsumer extends Thread implements VideoConsumer {
    private static final String TAG = HWConsumer.class.getSimpleName();

    private final String mMime;
    public EasyMuxer mEasyMuxer;
    private final Context mContext;
    private final Pusher mPusher;
    private int mHeight;
    private int mWidth;

    private int bitrateKbps;
    public String mName;
    public int mColorFormat;

    private int channelid;
    private String path;
    private MediaCodec mMediaCodec;
    private ByteBuffer[] inputBuffers;
    private ByteBuffer[] outputBuffers;

    private volatile boolean mVideoStarted;
    private MediaFormat mMediaFormat;
    private byte[] yuv;

    private int millisPerFrame = 1000 / 25;
    private long lastPush = 0;

    public HWConsumer(Context context, String mime, Pusher pusher, int bitrateKbps, String mName, int mColorFormat, int channelid, String path) {
        mContext = context;
        mMime = mime;
        mPusher = pusher;
        this.bitrateKbps = bitrateKbps;
        this.mName = mName;
        this.mColorFormat = mColorFormat;
        this.channelid = channelid;
        this.path = path;
    }

    @Override
    public void onVideoStart(int width, int height) {
        mMediaFormat = null;

        this.mWidth = width;
        this.mHeight = height;

        if (mPusher != null) {
            mPusher.setVFormat((mMime == MediaFormat.MIMETYPE_VIDEO_AVC) ? Device.VIDEO_CODEC_H264 : Device.VIDEO_CODEC_H265, width, height, bitrateKbps);
        }

        startMediaCodec();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP + 1) {
            inputBuffers = outputBuffers = null;
        } else {
            // 获取编码器的输入缓存inputBuffers
            inputBuffers = mMediaCodec.getInputBuffers();
            // 获取编码器的输出缓存outputBuffers
            outputBuffers = mMediaCodec.getOutputBuffers();
        }

        start();

        mVideoStarted = true;
    }

    @Override
    public int onVideo(byte[] i420, int format) {
        if (!mVideoStarted) {
            return 0;
        }

        if (yuv == null || yuv.length != i420.length)
            yuv = new byte[i420.length];

        try {
            byte[] data = yuv;

            if (lastPush == 0) {
                lastPush = System.currentTimeMillis();
            }

            long time = System.currentTimeMillis() - lastPush;

            if (time >= 0) {
                time = millisPerFrame - time;
                if (time > 0)
                    Thread.sleep(time / 2);
            }

            //从input缓冲区队列申请empty buffer
            int bufferIndex = mMediaCodec.dequeueInputBuffer(0);

            if (bufferIndex >= 0) {
                if (mColorFormat == COLOR_FormatYUV420SemiPlanar) {
                    JNIUtil.ConvertFromI420(i420, data, mWidth, mHeight, 3);
                } else if (mColorFormat == COLOR_TI_FormatYUV420PackedSemiPlanar) {
                    JNIUtil.ConvertFromI420(i420, data, mWidth, mHeight, 3);
                } else if (mColorFormat == COLOR_FormatYUV420Planar) {
                    JNIUtil.ConvertFromI420(i420, data, mWidth, mHeight, 0);
                } else {
                    JNIUtil.ConvertFromI420(i420, data, mWidth, mHeight, 0);
                }

                ByteBuffer buffer;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    buffer = mMediaCodec.getInputBuffer(bufferIndex);
                } else {
                    buffer = inputBuffers[bufferIndex];
                }

                buffer.clear();     //清除原来的内容以接收新的内容
                buffer.put(data);   //添加采集的原始视频数据
                buffer.clear();

                //将要编解码的数据拷贝到empty buffer，然后放入input缓冲区队列
                mMediaCodec.queueInputBuffer(bufferIndex, 0, data.length, System.nanoTime() / 1000, MediaCodec.BUFFER_FLAG_KEY_FRAME);
            }

            if (time > 0) {
                Thread.sleep(time / 2);
            }

            lastPush = System.currentTimeMillis();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    @Override
    public void run() {
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex;
        byte[] mPpsSps = new byte[0];
        byte[] h264 = new byte[mWidth * mHeight];

        do {
            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10000);

            if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                outputBuffers = mMediaCodec.getOutputBuffers();
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                synchronized (HWConsumer.this) {
                    mMediaFormat = mMediaCodec.getOutputFormat();
                    if (mEasyMuxer != null) {
                        //should happen before receiving buffers, and should only happen once
                        mEasyMuxer.addTrack(mMediaFormat, true);
                    }
                }
            } else if (outputBufferIndex < 0) {

            } else {
                ByteBuffer outputBuffer;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                } else {
                    outputBuffer = outputBuffers[outputBufferIndex];
                }

                outputBuffer.position(bufferInfo.offset);
                outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                if (mEasyMuxer != null) {
                    mEasyMuxer.pumpStream(outputBuffer, bufferInfo, true);
                }

                try {
                    if (mPusher != null) {
                        boolean sync = false;

                        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {// sps
                            sync = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0;

                            if (!sync) {
                                byte[] temp = new byte[bufferInfo.size];
                                outputBuffer.get(temp);
                                mPpsSps = temp;
                                continue;
                            } else {
                                mPpsSps = new byte[0];
                            }
                        }

                        sync |= (bufferInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0;
                        int len = mPpsSps.length + bufferInfo.size;

                        if (len > h264.length) {
                            h264 = new byte[len];
                        }

                        if (sync) {
                            System.arraycopy(mPpsSps, 0, h264, 0, mPpsSps.length);
                            outputBuffer.get(h264, mPpsSps.length, bufferInfo.size);
                            mPusher.pushV(channelid, h264, mPpsSps.length + bufferInfo.size, 1);

                            //save(h264,mPpsSps.length + bufferInfo.size);
                        } else {
                            outputBuffer.get(h264, 0, bufferInfo.size);
                            mPusher.pushV(channelid, h264, bufferInfo.size, 0);

                            //save(h264,bufferInfo.size);
                        }

                        if (BuildConfig.DEBUG) {
                            //Log.i(TAG,String.format("push video stamp:%d", bufferInfo.presentationTimeUs / 1000));
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "run  Exception  " + e.toString());
                } finally {
                    mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                }
            }
        } while (mVideoStarted);
    }

    @Override
    public void onVideoStop() {
        do {
            mMediaFormat = null;
            mVideoStarted = false;

            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (isAlive());

        if (mMediaCodec != null) {
            stopMediaCodec();
            mMediaCodec = null;
        }
    }

    @Override
    public synchronized void setMuxer(EasyMuxer mEasyMuxer) {
        if (mEasyMuxer != null) {
            if (mMediaFormat != null)
                mEasyMuxer.addTrack(mMediaFormat, true);
        }

        this.mEasyMuxer = mEasyMuxer;
    }

    /**
     * 初始化编码器
     */
    private void startMediaCodec() {

//        int bitrate = 10 * 1000 + bitrateKbps;

        try {
            // 1、选择出一个 MediaCodecInfo，初始化MediaCodec
            mMediaCodec = MediaCodec.createByCodecName(mName);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }

        int frameRate = SPUtil.getFramerate(mContext);

        millisPerFrame = 1000 / frameRate;

        //配置编码器
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(mMime, mWidth, mHeight);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrateKbps * 1000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mColorFormat);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);//关键帧间隔时间 单位s

        Log.i(TAG, "编码参数:" + mediaFormat.toString());

        //2、配置，进入Configured状态
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        //3、start()进入到执行状态，编解码器立即处于Flushed子状态，它拥有所有的缓冲区。
        mMediaCodec.start();

        Bundle params = new Bundle();
        params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mMediaCodec.setParameters(params);
        }
    }

    /**
     * 停止编码并释放编码资源占用
     */
    private void stopMediaCodec() {
        mMediaCodec.stop();
        mMediaCodec.release();
    }

    private void save(byte[] h264byteArr, int length) {
        File file = new File(path);
        boolean existsRe = file.exists();
        if (!existsRe) {
            boolean mkdirsRe = file.mkdirs();
        }

        save2path(h264byteArr, 0, length, path + "/hw_264.h264", true);
    }

    private static void save2path(byte[] buffer, int offset, int length, String path, boolean append) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path, append);
            fos.write(buffer, offset, length);
        } catch (Exception e) {
            Log.e(TAG, "save2path  Exception  " + e.toString());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    Log.e(TAG, "save2path  finally  Exception  " + e.toString());
                }
            }
        }
    }
}
