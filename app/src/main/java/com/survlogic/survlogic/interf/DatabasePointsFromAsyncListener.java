package com.survlogic.survlogic.interf;

import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 12/14/2017.
 */

public interface DatabasePointsFromAsyncListener {

    void getPointsGeodetic(ArrayList<PointGeodetic> lstPointGeodetics);

    void getPointsSurvey(ArrayList<PointSurvey> lstPointSurvey);


}
