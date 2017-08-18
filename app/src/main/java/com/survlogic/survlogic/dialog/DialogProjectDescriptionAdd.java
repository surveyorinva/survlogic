package com.survlogic.survlogic.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundProjectUpdate;
import com.survlogic.survlogic.database.ProjectDatabaseHandler;
import com.survlogic.survlogic.model.Project;

/**
 * Created by chrisfillmore on 7/22/2017.
 */

public class DialogProjectDescriptionAdd extends DialogFragment {
    private static final String TAG = "DialogProjectDescr";
    private Context mContext;
    private AlertDialog alertDialog;

    private int project_id;

    private String mDescription;
    private Project project;

    private EditText etDescription;

    public static DialogProjectDescriptionAdd newInstance(Integer mProjectId, String description) {

        DialogProjectDescriptionAdd frag = new DialogProjectDescriptionAdd();
        Bundle args = new Bundle();

        args.putInt("project_id", mProjectId);
        args.putString("description", description);

        frag.setArguments(args);
        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        project_id = getArguments().getInt("project_id");
        mDescription = getArguments().getString("description");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_project_note,null);
        builder.setView(v);

        builder.setPositiveButton(R.string.general_post,null);

        builder.create();
        return builder.show();


    }

    @Override
    public void onResume() {
        super.onResume();

        mContext = getActivity();
        AlertDialog alertDialog = (AlertDialog) getDialog();

        etDescription = (EditText) getDialog().findViewById(R.id.description);

        initValuesFromObject();

        Button btnSave = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm(v);
            }
        });

    }

    private void submitForm(View v) {
        Log.d(TAG, "submitForm: Starting");
        String description = etDescription.getText().toString();
        if(!isStringNull(description)){
            project.setmProjectDescription(description);
            saveProjectInBackground();
            Log.d(TAG, "submitForm: Closing Dialog");
            dismiss();
        }else{
            Log.d(TAG, "submitForm: Closing Dialog");
            dismiss();
        }

    }


    private boolean initValuesFromObject(){
        Log.d(TAG, "initValuesFromObject: Started");
        boolean results = false;

        ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(mContext);
        SQLiteDatabase db = projectDb.getReadableDatabase();

        try {
            project = projectDb.getProjectById(db, project_id);

            etDescription.setText(project.getmProjectDescription());
            etDescription.setSelection(etDescription.getText().length());

        }catch (Exception e){
            e.printStackTrace();
        }

        return results;

    }

    private void saveProjectInBackground(){
        Log.d(TAG, "saveProjectInBackground: Starting");
        // Setup Background Task
        BackgroundProjectUpdate backgroundProjectUpdate = new BackgroundProjectUpdate(mContext);

        // Execute background task
        backgroundProjectUpdate.execute(project);

    }

    private boolean isStringNull(String string){
        Log.d(TAG, "isStringNull: checking string if null.");

        return (string==null || string.trim().equals(""));
    }


}
