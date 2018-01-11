package com.survlogic.survlogic.interf;

import com.survlogic.survlogic.model.PointMapCheck;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 12/21/2017.
 */

public interface MapcheckListener {

    void sendNewMapcheckToActivity(PointMapCheck pointMapCheck, int position);

    void deleteNewMapcheckUserCancel(int position);

    void editModeMapcheckCancel(PointMapCheck pointMapCheck, int position);

    void sendEditModeMapcheckToActivity(PointMapCheck pointMapCheck, int position);

    void hideKeyboard();

    ArrayList<PointMapCheck> getPointMapCheck();
}
