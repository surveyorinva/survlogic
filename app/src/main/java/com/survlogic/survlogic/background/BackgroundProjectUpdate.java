package com.survlogic.survlogic.background;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.database.ProjectDatabaseHandler;
import com.survlogic.survlogic.model.Project;

/**
 * Created by chrisfillmore on 6/29/2017.
 */

public class BackgroundProjectUpdate extends AsyncTask <Project,Project,String> {

    private String TAG = getClass().getSimpleName();
    private ProgressDialog dialog;

    private Context context;
    private Activity activity;

    long project_id = 0;

    public BackgroundProjectUpdate(Context context) {
        Log.d(TAG, "BackgroundProjectUpdate: Starting");
        this.context = context;
        this.dialog = new ProgressDialog(context);

        activity = (Activity) context;
    }

    @Override
    protected String doInBackground(Project... params) {
        Log.d(TAG, "doInBackground: Opening db File");
        ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(context);
        SQLiteDatabase db = projectDb.getWritableDatabase();

        int row = 0;
        Project project = params[0];

        try{
            row = projectDb.updateProject(db,project);

        }catch (Exception e){
            e.printStackTrace();
        }

        Log.d(TAG, "doInBackground: Closing connection to db");
        db.close();

        if (row>0){
            return "One Row Inserted";
        }else{
            return "Error Inserting Row";
        }

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage("Updated Project");
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
