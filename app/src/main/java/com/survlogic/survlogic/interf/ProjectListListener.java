package com.survlogic.survlogic.interf;

import com.survlogic.survlogic.model.Project;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 2/6/2018.
 */

public interface ProjectListListener {

    void getProjectList(ArrayList<Project> lstProjects);

    void refreshProjectList();

}
