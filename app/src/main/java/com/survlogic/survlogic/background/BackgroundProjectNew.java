package com.survlogic.survlogic.background;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.database.ProjectDatabaseHandler;
import com.survlogic.survlogic.model.Project;

/**
 * Created by chrisfillmore on 6/29/2017.
 */

public class BackgroundProjectNew extends AsyncTask <Project,Project,String> {

    private String TAG = getClass().getSimpleName();
    private ProgressDialog dialog;

    private Context context;
    private Activity activity;

    long project_id = 0;

    public BackgroundProjectNew(Context context) {
        this.context = context;
        this.dialog = new ProgressDialog(context);

        activity = (Activity) context;
    }

    @Override
    protected String doInBackground(Project... params) {

        ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(context);
        SQLiteDatabase db = projectDb.getWritableDatabase();

        Project project = params[0];

        try{
            project_id = projectDb.addProjectToDB(db,project);

        }catch (Exception e){
            e.printStackTrace();
        }

        if (project_id>0){
            return "One Row Inserted";
        }else{
            return "Error Inserting Row";
        }

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage("Saving Project");
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.show();

    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        if (result.equals("One Row Inserted")){
            showToast(context.getString(R.string.project_new_validation_project_save_success),true);

        }else if (result.equals("Error Inserting Row")){
            showToast(context.getString(R.string.project_new_validation_project_save_failed),true);
        }

    }

    @Override
    protected void onProgressUpdate(Project... values) {
        super.onProgressUpdate(values);
    }

    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(context, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(context, data, Toast.LENGTH_LONG).show();

        }

    }

}
