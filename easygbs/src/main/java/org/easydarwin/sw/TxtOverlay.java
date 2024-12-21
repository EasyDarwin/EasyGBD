package org.easydarwin.sw;
import android.content.Context;
import android.text.TextUtils;
import java.io.File;

/**
 * Created by John on 2017/2/23.
 */
public class TxtOverlay {

    static {
        System.loadLibrary("TxtOverlay");
    }

    private final Context context;

    private long ctx;

    public TxtOverlay(Context context) {
        this.context = context;
    }

    public void init(int width, int height, String fonts) {
        if (TextUtils.isEmpty(fonts)) {
            throw new IllegalArgumentException("the font file must be valid!");
        }

        if (!new File(fonts).exists()) {
            throw new IllegalArgumentException("the font file must be exists!");
        }

        ctx = txtOverlayInit(width, height, fonts);
    }

    public void overlay(byte[] data, String txt) {
        if (ctx == 0)
            return;

        txtOverlay(ctx, data, txt);
    }

    public void release() {
        if (ctx == 0)
            return;

        txtOverlayRelease(ctx);
        ctx = 0;
    }

    private static native long txtOverlayInit(int width, int height, String fonts);

    private static native void txtOverlay(long ctx, byte[] data, String txt);

    private static native void txtOverlayRelease(long ctx);
}
