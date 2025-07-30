package org.easydarwin.push;
import org.easydarwin.util.SIP;

public interface Pusher {
    void stop();
    void initPush(SIP sip);
    void setVFormat(int codec, int width, int height, int frameRate);
    void setAFormat(int codec, int sampleRate, int channels, int bitPerSamples);
    void pushV(int channel, byte[] buffer, int length, int keyframe);
    void pushA(int channel, boolean isAac, byte[] buffer, int length, int nbSamples);
    void setLoLa(int channel, double longitude, double latitude);
    boolean getPushed();
}