package com.survlogic.survlogic.interf;

import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 10/10/2017.
 */

public interface JobPointsListener {

    void isMapSelectorActive(boolean isSelected);

    void isMapSelectorOpen(boolean isSelected);

    void getPointsGeodetic(ArrayList<PointGeodetic> lstPointGeodetics);

    void getPointsSurvey(ArrayList<PointSurvey> lstPointSurvey);
}
