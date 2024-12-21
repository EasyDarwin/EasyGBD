package org.easydarwin.encode;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.easygbs.Device;

import org.easydarwin.muxer.EasyMuxer;
import org.easydarwin.push.Pusher;
import org.easydarwin.util.SPUtil;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * AudioRecord音频采集、MediaCodec硬编码
 */
public class AudioStream {
    private static final String TAG = AudioStream.class.getSimpleName();

    private static AudioStream _this;

    private final Context context;
    private int aac;
    private boolean allowAudio = true;

    private EasyMuxer mEasyMuxer;

    private int samplingRate = 8000;
    private int bitRate = 16000;
    private int BUFFER_SIZE = 1920;
    private boolean enableAudio;

    private int mSamplingRateIndex = 0;

    private AudioRecord mAudioRecord;   // 底层的音频采集
    private MediaCodec mMediaCodec;     // 音频硬编码器

    private Thread mThread = null;

    protected MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    protected ByteBuffer[] outputBuffers = null; // 编码后的数据

    private Set<Pusher> sets = new HashSet<>();

    /**
     * There are 13 supported frequencies by ADTS.
     **/
    public static final int[] AUDIO_SAMPLING_RATES = {
            96000, // 0
            88200, // 1
            64000, // 2
            48000, // 3
            44100, // 4
            32000, // 5
            24000, // 6
            22050, // 7
            16000, // 8
            12000, // 9
            11025, // 10
            8000, // 11
            7350, // 12
            -1, // 13
            -1, // 14
            -1, // 15
    };

    private Thread mWriter;
    private MediaFormat newFormat;

    private int Audiochannel = AudioFormat.CHANNEL_IN_STEREO;

    public static synchronized AudioStream getInstance(Context context) {
        if (_this == null)
            _this = new AudioStream(context);

        return _this;
    }

    public AudioStream(Context context) {
        this.context = context;
    }

    /*
     * 添加推流器
     * */
    public void addPusher(Pusher pusher) {

        aac = SPUtil.getAACCodec(context);
        allowAudio = SPUtil.getIsenaudio(context) == 1;
        int samplingrate = SPUtil.getSamplingrate(context);
        samplingRate = samplingrate;

        int audiocoderate = SPUtil.getAudiocoderate(context);
        bitRate = audiocoderate * 1000;

        int count = 1;

        int audiochannel = SPUtil.getAudiochannel(context);
        if (audiochannel == 0) {
            count = 1;
            Audiochannel = AudioFormat.CHANNEL_IN_MONO;
        } else {
            count = 2;
            Audiochannel = AudioFormat.CHANNEL_IN_STEREO;
        }

        Log.i(TAG, "addPusher aac " + aac
                + "  samplingRate  " + samplingRate
                + "  audiocoderate  " + audiocoderate
                + "  audiochannel  " + audiochannel
                + " allowAudio " + allowAudio
                + " count " + count
        );
        boolean shouldStart = false;

        synchronized (this) {
            if (sets.isEmpty()) {
                shouldStart = true;
            }

            if (pusher != null) {
                if (aac == 0) {
                    if (Audiochannel == AudioFormat.CHANNEL_IN_MONO) {
                        pusher.setAFormat(Device.AUDIO_CODEC_G711A, samplingRate, count, audiocoderate);
                    } else if (Audiochannel == AudioFormat.CHANNEL_IN_STEREO) {
                        pusher.setAFormat(Device.AUDIO_CODEC_G711A, samplingRate, count, audiocoderate);
                    }
                } else if (aac == 1) {
                    if (Audiochannel == AudioFormat.CHANNEL_IN_MONO) {
                        pusher.setAFormat(Device.AUDIO_CODEC_G711U, samplingRate, count, audiocoderate);
                    } else if (Audiochannel == AudioFormat.CHANNEL_IN_STEREO) {
                        pusher.setAFormat(Device.AUDIO_CODEC_G711U, samplingRate, count, audiocoderate);
                    }
                } else if (aac == 2) {
                    if (Audiochannel == AudioFormat.CHANNEL_IN_MONO) {
                        pusher.setAFormat(Device.AUDIO_CODEC_AAC, samplingRate, count, audiocoderate);
                    } else if (Audiochannel == AudioFormat.CHANNEL_IN_STEREO) {
                        pusher.setAFormat(Device.AUDIO_CODEC_AAC, samplingRate, count, audiocoderate);
                    }
                }
            }

            sets.add(pusher);
        }

        if (shouldStart) {
            startRecord();
        }
    }

    public void upDateAllowAudio() {
        allowAudio = SPUtil.getIsenaudio(context) == 1;
    }

    /*
     * 删除推流器
     */
    public void removePusher(Pusher pusher) {
        boolean shouldStop = false;

        synchronized (this) {
            sets.remove(pusher);

            if (sets.isEmpty())
                shouldStop = true;
        }

        if (shouldStop) {
            stop();
        }
    }

    /*
     * 设置音频录像器
     */
    public synchronized void setMuxer(EasyMuxer mEasyMuxer) {
        if (mEasyMuxer != null) {
            if (newFormat != null) {
                mEasyMuxer.addTrack(newFormat, false);
            }
        }

        this.mEasyMuxer = mEasyMuxer;
    }

    /**
     * 编码
     */
    private void startRecord() {
        if (mThread != null)
            return;

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                int len, bufferIndex;

                try {
                    int count = 1;

                    int audiochannel = PreferenceManager.getDefaultSharedPreferences(context).getInt("audiochannel", 1);
                    if (audiochannel == 0) {
                        count = 1;
                        Audiochannel = AudioFormat.CHANNEL_IN_MONO;
                    } else {
                        count = 2;
                        Audiochannel = AudioFormat.CHANNEL_IN_STEREO;
                    }

                    int samplingrate = PreferenceManager.getDefaultSharedPreferences(context).getInt("samplingrate", 8000);
                    samplingRate = samplingrate;

                    for (int i = 0; i < AUDIO_SAMPLING_RATES.length; i++) {
                        if (AUDIO_SAMPLING_RATES[i] == samplingRate) {
                            mSamplingRateIndex = i;
                            break;
                        }
                    }

                    int bufferSize = 0;
                    try {
                        bufferSize = AudioRecord.getMinBufferSize(samplingRate, Audiochannel, AudioFormat.ENCODING_PCM_16BIT);
                    } catch (Exception e) {
                        Log.e(TAG, "startRecord  1 Exception  " + e.toString());
                        e.printStackTrace();
                    }
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mAudioRecord = new AudioRecord(
                            MediaRecorder.AudioSource.MIC,
                            samplingRate,
                            Audiochannel,
                            AudioFormat.ENCODING_PCM_16BIT,
                            bufferSize);

//                    Log.i(TAG, String.format("音频配置 samplingRate:%d ,Audiochannel:%d ,bufferSize:%d", samplingRate, Audiochannel, bufferSize));

                    String encodeType = "audio/mp4a-latm";

                    mMediaCodec = MediaCodec.createEncoderByType(encodeType);

                    MediaFormat format = new MediaFormat();
                    format.setString(MediaFormat.KEY_MIME, encodeType);

                    int audiocoderate = SPUtil.getAudiocoderate(context);
                    bitRate = audiocoderate * 1000;
                    format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);

                    format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, count);


                    format.setInteger(MediaFormat.KEY_SAMPLE_RATE, samplingRate);
                    format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
                    format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, BUFFER_SIZE);

                    mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

                    mMediaCodec.start();

                    mWriter = new WriterThread();
                    mWriter.start();

                    // 2、开始采集
                    mAudioRecord.startRecording();

                    //获取编码器的输入缓存inputBuffers
                    final ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();

                    long presentationTimeUs = 0;

                    while (mThread != null) {
                        bufferIndex = mMediaCodec.dequeueInputBuffer(1000);

                        if (bufferIndex >= 0) {
                            inputBuffers[bufferIndex].clear();

                            len = mAudioRecord.read(inputBuffers[bufferIndex], bufferSize);

                            if (aac != 2) {
                                Collection<Pusher> p;
                                synchronized (AudioStream.this) {
                                    p = sets;
                                }

                                Iterator<Pusher> it = p.iterator();
                                while (it.hasNext()) {
                                    Pusher ps = it.next();

                                    byte[] bytes = new byte[bufferSize];

                                    inputBuffers[bufferIndex].clear();

                                    inputBuffers[bufferIndex].get(bytes);

                                    if (allowAudio) {
                                        ps.pushA(0, false, bytes, bytes.length, (int) presentationTimeUs);
//                                        saveToFile(bytes, "test.pcm");
                                    }
                                }
                            }

                            long timeUs = System.nanoTime() / 1000;

                            presentationTimeUs = timeUs;

                            if (len == AudioRecord.ERROR_INVALID_OPERATION || len == AudioRecord.ERROR_BAD_VALUE) {
                                mMediaCodec.queueInputBuffer(bufferIndex, 0, 0, presentationTimeUs, 0);
                            } else {
                                mMediaCodec.queueInputBuffer(bufferIndex, 0, len, presentationTimeUs, 0);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "startRecord   Exception  " + e.toString());
                    e.printStackTrace();
                } finally {
                    Log.i(TAG, "startRecord  finally");
                    Thread t = mWriter;
                    mWriter = null;

                    while (t != null && t.isAlive()) {
                        try {
                            t.interrupt();
                            t.join();
                        } catch (InterruptedException e) {
                            Log.e(TAG, "startRecord   1  Exception  " + e.toString());
                            e.printStackTrace();

                        }
                    }

                    //4、停止采集，释放资源。
                    if (mAudioRecord != null) {
                        mAudioRecord.stop();
                        mAudioRecord.release();
                        mAudioRecord = null;
                    }

                    //停止编码
                    if (mMediaCodec != null) {
                        mMediaCodec.stop();
                        mMediaCodec.release();
                        mMediaCodec = null;
                    }
                }
            }
        }, "AACRecoder");

        if (enableAudio) {
            mThread.start();
        }
    }

    /**
     * 不断的从输出缓存中取出编码后的数据，然后push出去
     */
    private class WriterThread extends Thread {
        public WriterThread() {
            super("WriteAudio");
        }

        @Override
        public void run() {
            try {
                int index;

                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                    outputBuffers = mMediaCodec.getOutputBuffers();
                }

                ByteBuffer mByteBufferAAC = ByteBuffer.allocate(10240);

                do {
                    index = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 10000);

                    if (index >= 0) {
                        if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                            continue;
                        }

                        mByteBufferAAC.clear();

                        ByteBuffer outputBuffer;

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            outputBuffer = mMediaCodec.getOutputBuffer(index);
                        } else {
                            outputBuffer = outputBuffers[index];
                        }

                        if (mEasyMuxer != null) {
                            mEasyMuxer.pumpStream(outputBuffer, mBufferInfo, false);
                        }

                        outputBuffer.get(mByteBufferAAC.array(), 7, mBufferInfo.size);

                        outputBuffer.clear();

                        mByteBufferAAC.position(7 + mBufferInfo.size);
                        addADTStoPacket(mByteBufferAAC.array(), mBufferInfo.size + 7);

                        mByteBufferAAC.flip();

                        if (aac == 2) {
                            Collection<Pusher> p;
                            synchronized (AudioStream.this) {
                                p = sets;
                            }

                            int size = p.size();

                            Iterator<Pusher> it = p.iterator();
                            while (it.hasNext()) {
                                Pusher ps = it.next();

                                byte[] ByteBufferToarray = mByteBufferAAC.array();

                                if (allowAudio) {
                                    ps.pushA(0, true, ByteBufferToarray, mBufferInfo.size + 7, (int) (mBufferInfo.presentationTimeUs / 1000));
                                }
                            }
                        }

                        mMediaCodec.releaseOutputBuffer(index, false);
                    } else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        outputBuffers = mMediaCodec.getOutputBuffers();
                    } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        synchronized (AudioStream.this) {
                            newFormat = mMediaCodec.getOutputFormat();

                            if (mEasyMuxer != null)
                                mEasyMuxer.addTrack(newFormat, false);
                        }
                    } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {

                    } else {
                        Log.e(TAG, "WriterThread  Message   index" + index);
                    }
                } while (mWriter != null);

                Log.i(TAG, "WriterThread finish");
            } catch (Exception e) {
                Log.e(TAG, "WriterThread  Exception  " + e.toString());
            }
        }
    }

    /**
     * 添加ADTS头
     *
     * @param packet
     * @param packetLen
     */
    private void addADTStoPacket(byte[] packet, int packetLen) {
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF1;
        packet[2] = (byte) (((2 - 1) << 6) + (mSamplingRateIndex << 2) + (1 >> 2));
        packet[3] = (byte) (((1 & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    private void stop() {
        try {
            Thread t = mThread;
            mThread = null;

            if (t != null) {
                t.interrupt();
                t.join();
            }
        } catch (InterruptedException e) {
            e.fillInStackTrace();
        }
    }

    public void setEnableAudio(boolean enableAudio) {
        this.enableAudio = enableAudio;
    }

    private void saveToFile(byte[] buffer, String fileName) {
        try {
            File file = new File(context.getExternalFilesDir(null), fileName);

            if (!file.exists()) {
                try {
                    boolean created = file.createNewFile();
                    if (!created) {
                        Log.e("FileOutput", "Failed to create file.");
                        return;
                    }
                } catch (IOException e) {
                    Log.e("FileOutput", "Error creating file: " + e.getMessage());
                    return;
                }
            }
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(buffer, 0, buffer.length);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
