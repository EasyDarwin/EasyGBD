package org.easydarwin.push;

import org.easydarwin.util.SIP;

/**
 * 推流器
 * <p>
 * Created by john on 2017/5/6.
 */
public interface Pusher {

    void stop();

    void initPush(SIP sip);

    void setVFormat(int codec, int width, int height, int frameRate);

    void setAFormat(int codec, int sampleRate, int channels, int bitPerSamples);

    void pushV(byte[] buffer, int length, int keyframe);

    void pushA(boolean isAac, byte[] buffer, int length, int nbSamples);
}
