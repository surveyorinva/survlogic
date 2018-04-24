package com.survlogic.survlogic.interf;

import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 5/17/2017.
 */

public interface GpsSurveyListener extends LocationListener {

    void gpsStart();

    void gpsStop();

    @Deprecated
    void onGpsStatusChanged(int event, GpsStatus status);

    void onGnssFirstFix(int ttffMillis);

    void onSatelliteStatusChanged(GnssStatus status);

    void onGnssStarted();

    void onGnssStopped();

    void onGnssMeasurementsReceived(GnssMeasurementsEvent event);

    void onOrientationChanged(double orientation, double tilt);

    void onNmeaMessage(String message, long timestamp);

    void onFilterLocationChange(Location filtered);



}
