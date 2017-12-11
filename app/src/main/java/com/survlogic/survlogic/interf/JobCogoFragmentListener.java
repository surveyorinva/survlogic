package com.survlogic.survlogic.interf;

import com.survlogic.survlogic.model.JobInformation;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 10/10/2017.
 */

public interface JobCogoFragmentListener {

    ArrayList<PointSurvey> sendPointSurveyToFragment();

    JobInformation sendJobInformationToFragment();

    PointSurvey sendOccupyPointSurveyToFragment();

    PointSurvey sendBacksightPointSurveyToFragment();

    PointSurvey sendOccupyPointSurveyToFragment(PointSurvey pointSurvey);

    PointSurvey sendBacksightPointSurveyToFragment(PointSurvey pointSurvey);


    int sendOccupyPointNoToFragment();

    int sendBacksightPointNoToFragment();

    double sendOccupyHeightToFragment();

    double sendBacksightHeightToFragment();

    void setOccupyPointSurveyFromFragment(PointSurvey pointSurvey);

    void setBacksightPointSurveyFromFragment(PointSurvey pointSurvey);

    void setOccupyHeightFromFragment(double measureUp);

    void setBacksightHeightFromFragment(double measureUp);

    void sendSetupToMainActivity();

    void sendTraverseSetupToMainActivity(int occupyPointNo, int backsightPointNo, double occupyHI, double backsightHI);

    void invalidatePointSurveyList();

}
