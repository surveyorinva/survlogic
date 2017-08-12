package com.survlogic.survlogic.model_util;

import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;

import java.util.Comparator;

/**
 * Created by chrisfillmore on 8/10/2017.
 */

public class PointSurveyComparators {

    public PointSurveyComparators() {

    }

    public static Comparator<PointSurvey> getPointNoComparator(){
        return new PointSurveyPointNoComparator();

    }

    public static Comparator<PointSurvey> getNorthingComparator(){
        return new PointSurveyNorthingComparator();

    }

    public static Comparator<PointSurvey> getEastingComparator(){
        return new PointSurveyEastingComparator();

    }

    public static Comparator<PointSurvey> getElevationComparator(){
        return new PointSurveyElevationComparator();

    }

    public static Comparator<PointSurvey> getDescriptionComparator(){
        return new PointSurveyDescriptionComparator();

    }


    private static class PointSurveyPointNoComparator implements Comparator<PointSurvey>{

        @Override
        public int compare(PointSurvey pointSurvey1, PointSurvey pointSurvey2) {
            if (pointSurvey1.getPoint_no() < pointSurvey2.getPoint_no()) return -1;
            if (pointSurvey1.getPoint_no() < pointSurvey2.getPoint_no()) return 1;
            return 0;

        }
    }

    private static class PointSurveyNorthingComparator implements Comparator<PointSurvey>{

        @Override
        public int compare(PointSurvey pointSurvey1, PointSurvey pointSurvey2) {
            if (pointSurvey1.getNorthing() < pointSurvey2.getNorthing()) return -1;
            if (pointSurvey1.getNorthing() < pointSurvey2.getNorthing()) return 1;
            return 0;

        }
    }

    private static class PointSurveyEastingComparator implements Comparator<PointSurvey>{

        @Override
        public int compare(PointSurvey pointSurvey1, PointSurvey pointSurvey2) {
            if (pointSurvey1.getEasting() < pointSurvey2.getEasting()) return -1;
            if (pointSurvey1.getEasting() < pointSurvey2.getEasting()) return 1;
            return 0;

        }
    }

    private static class PointSurveyElevationComparator implements Comparator<PointSurvey>{

        @Override
        public int compare(PointSurvey pointSurvey1, PointSurvey pointSurvey2) {
            if (pointSurvey1.getElevation() < pointSurvey2.getElevation()) return -1;
            if (pointSurvey1.getElevation() < pointSurvey2.getElevation()) return 1;
            return 0;

        }
    }

    private static class PointSurveyDescriptionComparator implements Comparator<PointSurvey>{

        @Override
        public int compare(PointSurvey pointSurvey1, PointSurvey pointSurvey2) {
            return pointSurvey1.getDescription().compareTo(pointSurvey2.getDescription());

        }
    }

}
