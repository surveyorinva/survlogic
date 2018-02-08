package com.survlogic.survlogic.interf;

import com.survlogic.survlogic.model.ProjectJobs;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 2/8/2018.
 */

public interface ProjectDetailsActivityListener {

    void refreshView();

    void refreshJobBoard(ArrayList<ProjectJobs> lstJobs);

    void refreshJobBoardWithNewJob();

}
