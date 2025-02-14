package com.journeyapps.barcodescanner;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import com.google.zxing.client.android.R;
import com.journeyapps.util.StatusBarUtils;

public class CaptureActivity extends Activity {
    private String TAG=CaptureActivity.class.getSimpleName();
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private boolean isOpen = false;
    private ViewfinderView viewFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        StatusBarUtils.setStatusBarTransparent(CaptureActivity.this);
        barcodeScannerView = initializeContent();
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
        viewFinder = barcodeScannerView.getViewFinder();

        View llba = findViewById(R.id.llba);
        llba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFinder.cameraPreview.getCameraInstance().setFlashLight(false);
                finish();
            }
        });

        View ivba = findViewById(R.id.ivba);
        ivba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFinder.cameraPreview.getCameraInstance().setFlashLight(false);
                finish();
            }
        });

        View lllg = findViewById(R.id.lllg);
        lllg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOpen = !isOpen;
                viewFinder.cameraPreview.getCameraInstance().setFlashLight(isOpen);
            }
        });
    }

    protected DecoratedBarcodeView initializeContent() {
        setContentView(R.layout.zxing_capture);
        return (DecoratedBarcodeView)findViewById(R.id.zxing_barcode_scanner);
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}