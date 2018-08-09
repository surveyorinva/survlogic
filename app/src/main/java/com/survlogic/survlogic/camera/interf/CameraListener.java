package com.survlogic.survlogic.camera.interf;

import android.graphics.Bitmap;

import java.io.File;

public interface CameraListener {

    void returnPhotoCaptured(File file);


    File getFilePathFromActivity();

}
