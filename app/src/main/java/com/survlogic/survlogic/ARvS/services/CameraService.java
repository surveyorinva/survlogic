package com.survlogic.survlogic.ARvS.services;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.util.SizeF;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.util.Collections;

public class CameraService {
    private static final String TAG = "CameraService";

    private Context mContext;
    private Activity mActivity;

    private TextureView txvCameraView;

    private CameraManager cameraManager;
    private String mCameraId;
    private CameraDevice mCameraDevice;

    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest mPreviewRequest;

    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private CameraDevice.StateCallback mStateCallback;

    private TextureView.SurfaceTextureListener surfaceTextureListener;

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private int cameraFacing;
    private Size mPreviewSize;

    private float mCameraVerticalFOV, mCameraHorizontalFOV;

    public CameraService(Context context, TextureView txvCameraView) {
        this.mContext = context;
        this.txvCameraView = txvCameraView;

    }

    public boolean init(){
        Log.d(TAG, "init: Started");
        mActivity = (Activity) mContext;

        cameraManager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        cameraFacing = CameraCharacteristics.LENS_FACING_BACK;

        surfaceTextureListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                setUpCamera();
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        };

        initStateCallback();

        return true;

    }

    public boolean onStart(){
        Log.d(TAG, "onStart");
        openBackgroundThread();
        if (txvCameraView.isAvailable()) {
            setUpCamera();
            openCamera();
        } else {
            txvCameraView.setSurfaceTextureListener(surfaceTextureListener);
        }

        return  true;

    }

    public void onResume(){
        openBackgroundThread();
        if (txvCameraView.isAvailable()) {
            setUpCamera();
            openCamera();
        } else {
            txvCameraView.setSurfaceTextureListener(surfaceTextureListener);
        }

    }

    public boolean onStop(){
        closeCamera();
        closeBackgroundThread();

        return false;
    }
    //----------------------------------------------------------------------------------------------


    public float getCameraVerticalFOV() {
        return mCameraVerticalFOV;
    }

    public float getCameraHorizontalFOV() {
        return mCameraHorizontalFOV;
    }

    //----------------------------------------------------------------------------------------------
    private void setUpCamera() {
        Log.d(TAG, "setUpCamera: Started");

        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == cameraFacing) {
                    StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    mPreviewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
                    this.mCameraId = cameraId;
                    calculateFOV(cameraCharacteristics);
                }

            }
        } catch (CameraAccessException e) {
            showToast("Error",true);
            e.printStackTrace();
        }

    }

    private void openCamera() {
        Log.d(TAG, "openCamera: Started");

        try {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            cameraManager.openCamera(mCameraId, mStateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    private void closeCamera() {
        Log.d(TAG, "closeCamera: Started");

        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

    }


    //----------------------------------------------------------------------------------------------
    private void openBackgroundThread() {
        Log.d(TAG, "openBackgroundThread: Started");

        backgroundThread = new HandlerThread("camera_background_thread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void closeBackgroundThread() {
        if (backgroundHandler != null) {
            backgroundThread.quitSafely();
            backgroundThread = null;
            backgroundHandler = null;
        }
    }

    //----------------------------------------------------------------------------------------------
    private void initStateCallback() {
        Log.d(TAG, "initStateCallback: Started");

        mStateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                mCameraDevice = camera;
                createCameraPreviewSession();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                mCameraDevice.close();
                mCameraDevice = null;
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        };


    }

    private void createCameraPreviewSession() {
        Log.d(TAG, "createCameraPreviewSession: Started");

        try {
            SurfaceTexture surfaceTexture = txvCameraView.getSurfaceTexture();

            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            Surface previewSurface = new Surface(surfaceTexture);
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            if (mCameraDevice == null) {
                                return;
                            }

                            try {
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession = cameraCaptureSession;
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        null, backgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                        }
                    }, backgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void calculateFOV(CameraCharacteristics characteristics) {
        Log.d(TAG, "calculateFOV: Started");

        try {
            float[] maxFocus = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
            SizeF size = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
            float w = size.getWidth();
            float h = size.getHeight();
            mCameraHorizontalFOV = (float) (2 * Math.atan(w / (maxFocus[0] * 2)));
            mCameraVerticalFOV = (float) (2 * Math.atan(h / (maxFocus[0] * 2)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------------------------------------------------------------

    private void showToast(String data, boolean isShortTime) {

        if (isShortTime) {
            Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(mContext, data, Toast.LENGTH_LONG).show();

        }
    }

}
