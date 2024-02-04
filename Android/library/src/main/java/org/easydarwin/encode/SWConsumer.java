package org.easydarwin.encode;

import android.content.Context;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;

import com.easygbs.Device;

import org.easydarwin.muxer.EasyMuxer;
import org.easydarwin.push.Pusher;
import org.easydarwin.sw.JNIUtil;
import org.easydarwin.sw.X264Encoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * X264Encoder视频软编码器
 * <p>
 * Created by apple on 2017/5/13.
 */
public class SWConsumer extends Thread implements VideoConsumer {
    private static final String TAG = "SWConsumer";

    private final Context context;

    private int mHeight;
    private int mWidth;

    private X264Encoder x264;

    private final Pusher mPusher;
    private volatile boolean mVideoStarted;
    private int bitrateKbps;

    final int millisPerFrame = 1000 / 30;
    long lastPush = 0;

    private ArrayBlockingQueue<TimedBuffer> yuvs = new ArrayBlockingQueue<TimedBuffer>(2);
    private ArrayBlockingQueue<byte[]> yuv_caches = new ArrayBlockingQueue<byte[]>(10);

    public SWConsumer(Context context, Pusher pusher, int bitrateKbps) {
        this.context = context;
        mPusher = pusher;
        this.bitrateKbps = bitrateKbps;
    }

    @Override
    public void run() {
        byte[] h264 = new byte[mWidth * mHeight * 3 / 2];
        byte[] keyFrm = new byte[1];
        int[] outLen = new int[1];

        do {
            try {
                int r;
                TimedBuffer tb = yuvs.take();
                byte[] data = tb.buffer;
                long begin = System.currentTimeMillis();
                boolean keyFrame = false;
                r = x264.encode(data, 0, h264, 0, outLen, keyFrm);

                if (r > 0) {
                    keyFrame = keyFrm[0] == 1;
                    Log.i(TAG, String.format("encode spend:%d ms. keyFrm:%d", System.currentTimeMillis() - begin, keyFrm[0]));
                }

                keyFrm[0] = 0;
                yuv_caches.offer(data);

                if (mPusher != null) {
                    mPusher.pushV(h264, outLen[0], keyFrame ? 1 : 0);
                    save(h264, outLen[0]);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (mVideoStarted);
    }

    @Override
    public void onVideoStart(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;

        if (mPusher != null) {
            mPusher.setVFormat(Device.VIDEO_CODEC_H264,
                    width,
                    height,
                    bitrateKbps);
        }

        x264 = new X264Encoder();
        int bitrate = 72 * 1000 + bitrateKbps;
        x264.create(width, height, 30, bitrate / 1000);
        mVideoStarted = true;
        start();
    }

    @Override
    public int onVideo(byte[] data, int format) {
        try {
            if (lastPush == 0) {
                lastPush = System.currentTimeMillis();
            }

            long time = System.currentTimeMillis() - lastPush;

            if (time >= 0) {
                time = millisPerFrame - time;

                if (time > 0)
                    Thread.sleep(time / 2);
            }

            byte[] buffer = yuv_caches.poll();

            if (buffer == null || buffer.length != data.length) {
                buffer = new byte[data.length];
            }

            JNIUtil.ConvertFromI420(data, buffer, mWidth, mHeight, 1);
            yuvs.offer(new TimedBuffer(buffer));

            if (time > 0)
                Thread.sleep(time / 2);

            lastPush = System.currentTimeMillis();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    @Override
    public void onVideoStop() {
        do {
            mVideoStarted = false;

            try {
                interrupt();// 中断线程
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (isAlive());

        if (x264 != null) {
            x264.close();
        }

        x264 = null;
    }

    @Override
    public void setMuxer(EasyMuxer muxer) {

    }

    class TimedBuffer {
        byte[] buffer;
        long time;

        public TimedBuffer(byte[] data) {
            buffer = data;
            time = System.currentTimeMillis();
        }
    }

    private void save(byte[] h264, int length) {
        String path = Environment.getExternalStorageDirectory() + "/EasyGBS";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        save2path(h264, 0, length, path + "/hw_264.h264", true);
    }

    private static void save2path(byte[] buffer, int offset, int length, String path, boolean append) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path, append);
            fos.write(buffer, offset, length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
