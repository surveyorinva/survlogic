package com.survlogic.survlogic.interf;

import com.survlogic.survlogic.model.PointMapCheck;

/**
 * Created by chrisfillmore on 12/21/2017.
 */

public interface MapcheckListener {

    void sendNewMapcheckToActivity(PointMapCheck pointMapCheck, int position);

}
