package com.survlogic.survlogic.ARvS.interf;

import android.hardware.SensorEvent;

/**
 * Created by chrisfillmore on 3/16/2018.
 */

public interface ArvSOnSensorChangeListener {

    void onSensorChangedRaw(SensorEvent event);

    void onSensorChangedFiltered(float azimuthFrom, float azimuthTo, float deviceOrientation[]);

}
