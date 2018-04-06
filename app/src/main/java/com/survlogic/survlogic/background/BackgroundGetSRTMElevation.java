package com.survlogic.survlogic.background;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.survlogic.survlogic.ARvS.interf.GetNationalMapElevationListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class BackgroundGetSRTMElevation extends AsyncTask<String,String,String> {
    private static final String TAG = "BackgroundGetNationalMa";

    private Context mContext;
    private GetNationalMapElevationListener mListener;

    private Location targetLocation;

    private ProgressDialog pd;

    public BackgroundGetSRTMElevation(Context context, Location targetLocation, GetNationalMapElevationListener listener) {
        this.mContext = context;
        this.targetLocation = targetLocation;
        this.mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        pd = new ProgressDialog(mContext);
        pd.setMessage("Please wait");
        pd.setCancelable(false);
        pd.show();

    }


    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;


        http://api.geonames.org/srtm3JSON?lat=38.953610&lng=-76.833974&username=surveyorinva

        try {
            URL url = new URL("http://api.geonames.org/srtm3JSON?lat="
                    + targetLocation.getLatitude() + "&lng="
                    + targetLocation.getLongitude() + "&username=surveyorinva");

            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
                Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

            }

            return buffer.toString();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;


    }



    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);


    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (pd.isShowing()){
            pd.dismiss();
        }

        String elevationResults = Double.toString(parseResultsForElevation(result));
        mListener.getResultSRTM(elevationResults);

    }


    private Double parseResultsForElevation(String input){
        Log.d(TAG, "parseResultsForElevation: Started");
        double elevation = 0.00;
        try{
            JSONObject jsonResultsQuery = new JSONObject(input);
            elevation = jsonResultsQuery.getDouble("srtm3");

        }catch (Exception ex){
            Log.d(TAG, "parseResultsForElevation: Error");
        }

        return elevation;

    }

}

