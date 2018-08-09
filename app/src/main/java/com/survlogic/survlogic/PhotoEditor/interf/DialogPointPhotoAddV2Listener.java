package com.survlogic.survlogic.PhotoEditor.interf;

import android.graphics.Bitmap;
import android.location.Location;

public interface DialogPointPhotoAddV2Listener {

    void requestPhotoRefresh();

    boolean isUsePhotoLocation();

    boolean isUsePhotoSensor();

    int getPointNumber();

    Bitmap getPhoto();

    Location getPhotoLocation();

    int getPhotoAzimuth();

}
