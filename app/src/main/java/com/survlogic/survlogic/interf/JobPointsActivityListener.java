package com.survlogic.survlogic.interf;

/**
 * Created by chrisfillmore on 11/23/2017.
 */

public interface JobPointsActivityListener {

    void refreshPointArrays();

    void requestPointSurveyArray();

    void requestPointGeodeticArray();

    void callPointViewDialogBox(long point_id, int pointNo);

}
