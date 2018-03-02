package com.survlogic.survlogic.interf;

/**
 * Created by chrisfillmore on 8/19/2017.
 */

public interface PointGeodeticEntryListener {

    void onWorldReturnValues(double latOut, double longOut, double heightEllipsOut, double heightOrthoOut);


    void onGridReturnValues(double gridNorth, double gridEast);

}
