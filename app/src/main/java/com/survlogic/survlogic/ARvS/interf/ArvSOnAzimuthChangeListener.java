package com.survlogic.survlogic.ARvS.interf;

import android.hardware.SensorEvent;

/**
 * Created by chrisfillmore on 3/16/2018.
 */

public interface ArvSOnAzimuthChangeListener {

    void onDeviceSensorChange(SensorEvent event);

    void onAzimuthChanged(float azimuthFrom, float azimuthTo, float cameraOrientation[]);

}
