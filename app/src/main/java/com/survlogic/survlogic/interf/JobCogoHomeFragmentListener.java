package com.survlogic.survlogic.interf;

import com.survlogic.survlogic.model.PointSurvey;

/**
 * Created by chrisfillmore on 11/9/2017.
 */

public interface JobCogoHomeFragmentListener {

    PointSurvey getOccupyPointSurvey();

    PointSurvey getBacksightPointSurvey();

    boolean isOccupyPointSurveySet();

    boolean isBacksightPointSurveySet();

    double getOccupyHeight();

    double getBacksightHeight();

}
