package com.survlogic.survlogic.interf;

import android.widget.ImageView;

import com.survlogic.survlogic.model.Project;

/**
 * Created by chrisfillmore on 7/6/2017.
 */

public interface ProjectItemClickListener {

    void onProjectItemClick(int pos, Project project, ImageView shareImageView);


}
