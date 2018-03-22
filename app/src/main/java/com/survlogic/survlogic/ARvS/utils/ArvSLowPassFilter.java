package com.survlogic.survlogic.ARvS.utils;

/**
 * Created by chrisfillmore on 3/21/2018.
 */

public class ArvSLowPassFilter {
    /*
     * Time smoothing constant for low-pass filter 0 ≤ α ≤ 1 ; a smaller value
     * basically means more smoothing See:
     * http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
     */
    private static final float ALPHA = 0.2f;

    // Time constant in seconds
    static final float timeConstant = 0.297f;
    private float alpha = 0.15f;
    private float dt = 0;
    private float timestamp = System.nanoTime();
    private float timestampOld = System.nanoTime();
    private float output[] = new float[]{ 0, 0, 0 };
    private long count = 0;

    private ArvSLowPassFilter() {

    }

    /**
     * Filter the given input against the previous values and return a low-pass
     * filtered result.
     *
     * @param input
     *            float array to smooth.
     * @param output
     *            float array representing the previous values.
     * @return float array smoothed with a low-pass filter.
     */
    public static float[] filter(float[] input, float[] output) {
        if (input == null || output == null) throw new NullPointerException("input and prev float arrays must be non-NULL");
        if (input.length != output.length) throw new IllegalArgumentException("input and prev must be the same length");

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }


    public float[] filterByTime(float[] input)
    {
        timestamp = System.nanoTime();

        // Find the sample period (between updates).
        // Convert from nanoseconds to seconds
        dt = 1 / (count / ((timestamp - timestampOld) / 1000000000.0f));

        count++;

        // Calculate alpha
        alpha = timeConstant / (timeConstant + dt);

        output[0] = calculate(input[0], output[0]);
        output[1] = calculate(input[1], output[1]);
        output[2] = calculate(input[2], output[2]);

        return output;
    }


    float calculate(float input, float output){
        float out = alpha * output  + (1 - alpha) * input;
        return out;
    }





}
