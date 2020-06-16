package com.moringa.barcode_reader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private SurfaceView mSurfaceView ;
    private BarcodeDetector mBarcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_IMAGE_CAPTURE = 201;
    private ToneGenerator mTone;
    private TextView mCodeText;
    private String CodeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTone = new ToneGenerator(AudioManager.STREAM_MUSIC,100);
        mSurfaceView = findViewById(R.id.surface_view);
        mCodeText = findViewById(R.id.barcode_text);
        initiateDetectors();

    }
    private void initiateDetectors(){
        mBarcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build();

        cameraSource = new CameraSource.Builder(this,mBarcodeDetector).setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true)
                .build();

        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try{
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                        cameraSource.start(mSurfaceView.getHolder());

                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},REQUEST_IMAGE_CAPTURE);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

                cameraSource.stop();

            }
        });

        mBarcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> BarCodes = detections.getDetectedItems();
                if(BarCodes.size() != 0){

                    mCodeText.post(new Runnable() {
                        @Override
                        public void run() {
                            if ( BarCodes.valueAt(0).email != null){
                                mCodeText.removeCallbacks(null);
                                CodeData = BarCodes.valueAt(0).email.address;

                                mCodeText.setText(CodeData);

                                mTone.startTone(ToneGenerator.TONE_CDMA_PIP,150);
                            } else {
                                CodeData = BarCodes.valueAt(0).displayValue;

                                mCodeText.setText(CodeData);

                                mTone.startTone(ToneGenerator.TONE_CDMA_PIP,150);
                            }
                        }
                    });
                }
            }
        });
    }
}