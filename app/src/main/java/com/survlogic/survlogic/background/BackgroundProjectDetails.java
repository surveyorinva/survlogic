package com.survlogic.survlogic.background;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.database.ProjectDatabaseHandler;
import com.survlogic.survlogic.model.Project;

/**
 * Created by chrisfillmore on 6/29/2017.
 */

public class BackgroundProjectDetails extends AsyncTask <Integer,Project,String> {

    private String TAG = getClass().getSimpleName();
    private ProgressDialog dialog;

    private Context context;
    private Activity activity;

    private TextView tvProjectName;
    private ImageView ivProjectImage;

    public BackgroundProjectDetails(Context context) {
        this.context = context;
        this.dialog = new ProgressDialog(context);

        activity = (Activity) context;
    }

    @Override
    protected String doInBackground(Integer... params) {

        ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(context);
        SQLiteDatabase db = projectDb.getReadableDatabase();

        int project_id = params[0];

        try{
            Project project = projectDb.getProjectById(db,project_id);
            publishProgress(project);

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
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.show();

        initView();


    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        if (result.equals("One Row Inserted")){


        }else if (result.equals("Error Inserting Row")){

        }

    }

    @Override
    protected void onProgressUpdate(Project... values) {
        super.onProgressUpdate(values);

        Project project = values[0];

        tvProjectName.setText(project.getmProjectName());
        ivProjectImage.setImageBitmap(convertToBitmap(project.getmImage()));

    }

    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(context, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(context, data, Toast.LENGTH_LONG).show();

        }

    }


    private void initView(){
        tvProjectName = (TextView) activity.findViewById(R.id.project_name_in_card_project_detail);
        ivProjectImage = (ImageView) activity.findViewById(R.id.header_image_in_activity_project_details);

    }

    private Bitmap convertToBitmap(byte[] b){
        return BitmapFactory.decodeByteArray(b, 0, b.length);

    }

}
