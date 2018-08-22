package com.survlogic.survlogic.PhotoEditor.interf;

import android.graphics.Bitmap;
import android.location.Location;

import java.io.File;

public interface DialogPointPhotoAddV2Listener {

    void requestPhotoRefresh();

    boolean isUsePhotoLocation();

    boolean isUsePhotoSensor();

    int getPointNumber();

    int getPointId();

    int getProjectId();

    int getJobId();

    Bitmap getPhoto();

    File getPhotoFile();

    Location getPhotoLocation();

    int getPhotoAzimuth();

}
