package com.survlogic.survlogic.model_util;

import com.survlogic.survlogic.model.PointGeodetic;

import java.util.Comparator;

/**
 * Created by chrisfillmore on 8/10/2017.
 */

public class PointGeodeticComparators {

    public PointGeodeticComparators() {

    }

    public static Comparator<PointGeodetic> getPointNoComparator(){
        return new PointGeodeticPointNoComparator();

    }

    public static Comparator<PointGeodetic> getLatitudeComparator(){
        return new PointGeodeticLatitudeComparator();

    }

    public static Comparator<PointGeodetic> getLongtitudeComparator(){
        return new PointGeodeticLongitudeComparator();

    }

    public static Comparator<PointGeodetic> getElevationComparator(){
        return new PointGeodeticElevationComparator();

    }

    public static Comparator<PointGeodetic> getDescriptionComparator(){
        return new PointGeodeticDescriptionComparator();

    }


    private static class PointGeodeticPointNoComparator implements Comparator<PointGeodetic>{

        @Override
        public int compare(PointGeodetic pointGeodetic1, PointGeodetic pointGeodetic2) {
            if (pointGeodetic1.getPoint_no() < pointGeodetic2.getPoint_no()) return -1;
            if (pointGeodetic1.getPoint_no() < pointGeodetic2.getPoint_no()) return 1;
            return 0;

        }
    }

    private static class PointGeodeticLatitudeComparator implements Comparator<PointGeodetic>{

        @Override
        public int compare(PointGeodetic pointGeodetic1, PointGeodetic pointGeodetic2) {
            if (pointGeodetic1.getNorthing() < pointGeodetic2.getNorthing()) return -1;
            if (pointGeodetic1.getNorthing() < pointGeodetic2.getNorthing()) return 1;
            return 0;

        }
    }

    private static class PointGeodeticLongitudeComparator implements Comparator<PointGeodetic>{

        @Override
        public int compare(PointGeodetic pointGeodetic1, PointGeodetic pointGeodetic2) {
            if (pointGeodetic1.getEasting() < pointGeodetic2.getEasting()) return -1;
            if (pointGeodetic1.getEasting() < pointGeodetic2.getEasting()) return 1;
            return 0;

        }
    }

    private static class PointGeodeticElevationComparator implements Comparator<PointGeodetic>{

        @Override
        public int compare(PointGeodetic pointGeodetic1, PointGeodetic pointGeodetic2) {
            if (pointGeodetic1.getElevation() < pointGeodetic2.getElevation()) return -1;
            if (pointGeodetic1.getElevation() < pointGeodetic2.getElevation()) return 1;
            return 0;

        }
    }

    private static class PointGeodeticDescriptionComparator implements Comparator<PointGeodetic>{

        @Override
        public int compare(PointGeodetic pointGeodetic1, PointGeodetic pointGeodetic2) {
            return pointGeodetic1.getDescription().compareTo(pointGeodetic2.getDescription());

        }
    }

}
