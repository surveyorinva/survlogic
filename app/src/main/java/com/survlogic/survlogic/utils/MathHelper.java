package com.survlogic.survlogic.utils;

import android.content.Context;

import java.util.List;

public class MathHelper {

    private Context mContext;

    public MathHelper(Context context) {
        mContext = context;
    }

    //Simple Mathmatics
    public float getMeanFloat(List<Float> points) {
        Float sum = 0.0F;
        if (!points.isEmpty()) {
            for (Float point : points) {
                sum += point;
            }
            return sum / points.size();
        }
        return sum;
    }

    public double getMeanDouble(List<Double> points) {
        Double sum = 0.0D;
        if (!points.isEmpty()) {
            for (Double point : points) {
                sum += point;
            }
            return sum / points.size();
        }
        return sum;
    }

    public float getVarianceFloat(List<Float> points) {
        float mean = getMeanFloat(points);
        float temp = 0;
        for (Float point : points) {
            temp += (point - mean) * (point - mean);

        }

        return temp / (points.size() - 1);

    }

    public double getVarianceDouble(List<Double> points) {
        double mean = getMeanDouble(points);
        double temp = 0;
        for (Double point : points) {
            temp += (point - mean) * (point - mean);

        }

        return temp / (points.size() - 1);

    }

    public float getStdDevFloat(List<Float> points) {
        return (float) Math.sqrt(getVarianceFloat(points));
    }

    public double getStdDevDouble(List<Double> points) {
        return (double) Math.sqrt(getVarianceDouble(points));
    }

}
