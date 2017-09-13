package com.survlogic.survlogic.background;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.database.JobDatabaseHandler;
import com.survlogic.survlogic.database.ProjectDatabaseHandler;
import com.survlogic.survlogic.model.JobSketch;
import com.survlogic.survlogic.model.ProjectImages;

/**
 * Created by chrisfillmore on 6/29/2017.
 */

public class BackgroundProjectSketchNew extends AsyncTask <JobSketch,JobSketch,String> {

    private static final String TAG = "BackgroundProjectSketch";
    private ProgressDialog dialog;

    private Context context;
    private Activity activity;
    private String jobDbName;
    long project_sketch_id = 0;

    public BackgroundProjectSketchNew(Context context, String jobDbName) {
        this.context = context;
        this.dialog = new ProgressDialog(context);
        this.jobDbName = jobDbName;

        activity = (Activity) context;
    }

    @Override
    protected String doInBackground(JobSketch... params) {

        JobDatabaseHandler jobDb = new JobDatabaseHandler(context, jobDbName);
        SQLiteDatabase db = jobDb.getWritableDatabase();

        JobSketch jobSketch = params[0];

        try{
            project_sketch_id = jobDb.addSketchToDB(db,jobSketch);

        }catch (Exception e){
            e.printStackTrace();
        }

        if (project_sketch_id>0){
            return "One Row Inserted";
        }else{
            return "Error Inserting Row";
        }

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage("Saving Job Sketch");
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
            showToast(context.getString(R.string.sketch_new_validation_save_success),true);

        }else if (result.equals("Error Inserting Row")){
            showToast(context.getString(R.string.sketch_new_validation_save_failed),true);
        }

    }

    @Override
    protected void onProgressUpdate(JobSketch... values) {
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
