package com.survlogic.survlogic.ARvS.interf;

import com.survlogic.survlogic.model.ProjectJobs;

public interface PopupDialogBoxListener {

    void onSave();

    void onCancel();

    void onDismiss();

    ProjectJobs getProjectJob();

}
