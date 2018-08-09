package com.survlogic.survlogic.ARvS.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.ARvS.interf.GPSMeasureHelperListener;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundSurveyPointExistInDatabase;
import com.survlogic.survlogic.interf.DatabaseDoesPointExistFromAsyncListener;
import com.survlogic.survlogic.utils.KeyboardUtils;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.RumbleHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;

import java.text.DecimalFormat;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class GPSMeasureHelper implements DatabaseDoesPointExistFromAsyncListener {
    private static final String TAG = "GPSMeasureHelper";

    private Context mContext;
    private Activity mActivity;

    private PreferenceLoaderHelper preferenceLoaderHelper;
    private GPSMeasureHelperListener listener;
    //---------------------------------------------------------------------------------------------- GNSS Measure
    private RelativeLayout rlMeasure, rlMeasureFooter, rlMeasureMenuSetup, rlFilterOverlay, rlMeasureStatus;
    private View reveal_view;

    private mehdi.sakout.fancybuttons.FancyButton ibGpsMeasure_Measure, ibGpsMeasure_Setup_Close, ibGpsMeasure_Measure_Close;
    private FloatingActionButton fabMeasureHidden;

    private TextView tvMeasureCountdown, tvMeasurePointNo;
    private EditText etPointNumber, etGpsHeight;
    private Spinner spRate;

    private String mJob_DatabaseName;

    private CountDownTimer mCountDownTimer;
    private long mEpochCountSet, mTimeLeftInMillis;

    private boolean hasGPSMeasureInit = false;
    private boolean isGPSMeasureSetup = false;
    private boolean isGPSMeasureSetupMenuOpen = false;
    private boolean isGPSMeasuring = false;
    private boolean isTimerRunning = false;
    private boolean isPointNoSet = false;

    //----------------------------------------------------------------------------------------------Method Variables
    private int mValuePointNo;
    private double mValueHeight;


    public GPSMeasureHelper(Context mContext, String jobDatabaseName, GPSMeasureHelperListener listener) {
        this.mContext = mContext;
        this.mActivity = (Activity) mContext;

        this.mJob_DatabaseName = jobDatabaseName;
        this.listener = listener;

        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        initGPSMeasureWidgets();

    }


    //---------------------------------------------------------------------------------------------- Getters and Setters

    public void setViewState(boolean toShow){
        Log.d(TAG, "setViewState: Started:" + toShow);
        if(toShow){
            rlMeasure.setVisibility(View.VISIBLE);
        }else{
            rlMeasure.setVisibility(View.GONE);
        }
    }

    public boolean isGPSMeasureSetupMenuOpen() {
        return isGPSMeasureSetupMenuOpen;
    }

    public void requestSetupMenuClose(){
        closeMeasureSetupDialogBox();
    }


    public void setPointNumber(int pointNumber){
        Log.d(TAG, "setPointNumber: ");

        this.mValuePointNo = pointNumber;
        etPointNumber.setText(String.valueOf(mValuePointNo));

        checkPointNumberFromDatabase(mValuePointNo);


    }

    public int getPointNumber(){
        return mValuePointNo;
    }

    public double getInstrumentHeight(){
        Log.d(TAG, "getInstrumentHeight: Started");

        return mValueHeight;

    }

    //---------------------------------------------------------------------------------------------- Methods

    private void initGPSMeasureWidgets() {
        Log.d(TAG, "initGPSMeasureWidgets: Started");

        rlMeasure = mActivity.findViewById(R.id.rl_gnss_measure);
        rlFilterOverlay = mActivity.findViewById(R.id.background_filter);
        reveal_view = mActivity.findViewById(R.id.reveal);

        rlMeasureFooter = mActivity.findViewById(R.id.rl_floating_footer);
        rlMeasureMenuSetup = mActivity.findViewById(R.id.rl_setup);
        rlMeasureStatus = mActivity.findViewById(R.id.rl_measure_status);

        fabMeasureHidden = mActivity.findViewById(R.id.fab_measure_hidden);

        ibGpsMeasure_Measure = mActivity.findViewById(R.id.btn_measure);
        ibGpsMeasure_Setup_Close = mActivity.findViewById(R.id.btn_settings_save);

        ibGpsMeasure_Measure_Close = mActivity.findViewById(R.id.btn_measure_cancel);

        tvMeasureCountdown = mActivity.findViewById(R.id.measure_countdown);
        tvMeasurePointNo = mActivity.findViewById(R.id.value_measure_status_point_no);

        etPointNumber = mActivity.findViewById(R.id.pointNumber);
        etPointNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    String pointNo = null;
                    pointNo = etPointNumber.getText().toString();

                    if (!StringUtilityHelper.isStringNull(pointNo)) {
                        mValuePointNo = Integer.parseInt(pointNo);
                        checkPointNumberFromDatabase(mValuePointNo);

                    }

                } catch (NumberFormatException ex) {
                    showToast("Error.  Check Number Format", true);

                }
            }
        });

        etGpsHeight = mActivity.findViewById(R.id.gps_height);

        spRate = mActivity.findViewById(R.id.spinner_rate);
        setRateSpinner();

        setGPSMeasureOnClickListeners();
        hasGPSMeasureInit = true;

    }

    private void setRateSpinner() {
        Log.d(TAG, "setRateSpinner: Started");

        spRate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            // On selecting a spinner item
                String item = spRate.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }


    private void setGPSMeasureOnClickListeners(){
        ibGpsMeasure_Measure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isGPSMeasureSetup){
                    openMeasureSetupDialogBox();
                }else{
                    measureSpatialPosition();
                }

            }
        });

        ibGpsMeasure_Measure.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                RumbleHelper.init(mContext);
                RumbleHelper.once(50);

                openMeasureSetupDialogBox();

                return true;
            }
        });

        ibGpsMeasure_Setup_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isGPSMeasureSetupMenuOpen){
                    closeMeasureSetupDialogBox();
                }
            }
        });

        ibGpsMeasure_Measure_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelMeasureStatusView();
            }
        });

    }

    private void openMeasureSetupDialogBox(){
        Log.d(TAG, "openMeasureSetupDialogBox: Started");
        DecimalFormat df = StringUtilityHelper.createUSNonBiasDecimalFormatSelect(2);

        isGPSMeasureSetupMenuOpen = true;

        double height = preferenceLoaderHelper.getGpsSurveyHeight();
        etGpsHeight.setText(df.format(height));

        rlFilterOverlay.setVisibility(View.VISIBLE);
        listener.setViewsForModal(true);

        rlMeasureMenuSetup.setVisibility(View.VISIBLE);


        if(!isGPSMeasureSetup){
            isGPSMeasureSetup = true;
        }

    }

    private void closeMeasureSetupDialogBox(){
        Log.d(TAG, "closeMeasureSetupDialogBox: Started");

        if(isPointNoSet){

            KeyboardUtils.hideKeyboard(mActivity);

            listener.setViewsForModal(false);
            rlFilterOverlay.setVisibility(View.GONE);
            rlMeasureMenuSetup.setVisibility(View.GONE);

            mValuePointNo = Integer.valueOf(etPointNumber.getText().toString());

            preferenceLoaderHelper.setGpsSurveyHeight(Float.valueOf(etGpsHeight.getText().toString()),true);
            mValueHeight = Float.valueOf(etGpsHeight.getText().toString());

            isGPSMeasureSetupMenuOpen = false;
        }else{
            RumbleHelper.init(mContext);
            RumbleHelper.once(100);

            etPointNumber.setError(mContext.getResources().getString(R.string.cogo_point_no_not_entered));

        }

    }

    private void measureSpatialPosition(){
        Log.d(TAG, "measureSpatialPosition: Started");
        revealMeasureActions();


    }

    private void delayedStartMeasure() {
        isGPSMeasuring = true;
        listener.setViewsForModal(true);
        ibGpsMeasure_Measure.setEnabled(false);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tvMeasurePointNo.setText(String.valueOf(mValuePointNo));
                Log.d(TAG, "run: point No: " + mValuePointNo);

                rlMeasureStatus.setVisibility(View.VISIBLE);
                startTimer();
            }
        }, 300);
    }

    private void closeMeasureStatusView(){
        Log.d(TAG, "closeMeasureStatusView: Started");

        listener.setViewsForModal(false);
        ibGpsMeasure_Measure.setEnabled(true);

        listener.showResultsDialog(true);
        rlMeasureStatus.setVisibility(View.INVISIBLE);

        isGPSMeasuring = false;

    }

    private void cancelMeasureStatusView(){
        Log.d(TAG, "cancelMeasureStatusView: Started");

        pauseTimer();

        listener.setViewsForModal(false);
        ibGpsMeasure_Measure.setEnabled(true);

        listener.showResultsDialog(true);
        rlMeasureStatus.setVisibility(View.INVISIBLE);

        isGPSMeasuring = false;

    }

    //----------------------------------------------------------------------------------------------
    private void startTimer(){
        Log.d(TAG, "startTimer: Started");

        int epoch_pos = spRate.getSelectedItemPosition();
        String[] epoch_values = mActivity.getResources().getStringArray(R.array.pref_gps_frequency_values);
        mEpochCountSet = Integer.valueOf(epoch_values[epoch_pos])*1000 + 1000;

        listener.startStopMeasureData(true);

        mCountDownTimer = new CountDownTimer(mEpochCountSet, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();

                if(mTimeLeftInMillis==1000){
                    RumbleHelper.init(mContext);
                    RumbleHelper.once(100);
                }

            }

            @Override
            public void onFinish() {
                listener.startStopMeasureData(false);

                onFinishTimer();

                isTimerRunning = false;
            }
        }.start();

        isTimerRunning = true;

    }

    private void pauseTimer() {
        mCountDownTimer.cancel();
        isTimerRunning = false;

    }

    private void resetTimer() {
        mTimeLeftInMillis = mEpochCountSet;
        updateCountDownText();

    }

    private void updateCountDownText(){
        Log.d(TAG, "updateCountDownText: Started");

        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        if(seconds == 0){
            tvMeasureCountdown.setText(mActivity.getResources().getString(R.string.general_finished));
        }else{
            tvMeasureCountdown.setText(String.valueOf(seconds));
        }


    }

    private void onFinishTimer(){
        Log.d(TAG, "onFinishTimer: Started");

        closeMeasureStatusView();


    }


    //---------------------------------------------------------------------------------------------- Reveal Animation
    //----------------------------------------------------------------------------------------------//
    private void revealMeasureActions() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startRevealAnimation();

                delayedStartMeasure();
            }
        }, 500);
    }


    private void startRevealAnimation() {
        ibGpsMeasure_Measure.setElevation(0f);

        reveal_view.setVisibility(VISIBLE);

        int cx = reveal_view.getWidth();
        int cy = reveal_view.getHeight();


        int x = (int) (getFabWidth() / 2 + fabMeasureHidden.getX());
        int y = (int) (getFabWidth() / 2 + fabMeasureHidden.getY());

        float finalRadius = Math.max(cx, cy) * 1.2f;

        Animator reveal = ViewAnimationUtils
                .createCircularReveal(reveal_view, x, y, getFabWidth(), finalRadius);

        reveal.setDuration(400);
        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                reset(animation);

            }

            private void reset(Animator animation) {
                super.onAnimationEnd(animation);
                reveal_view.setVisibility(INVISIBLE);

            }
        });

        reveal.start();
    }


    private int getFabWidth() {
        return (int) mActivity.getResources().getDimension(R.dimen.fab_size);
    }

    //---------------------------------------------------------------------------------------------- Point Entry Check
    private void checkPointNumberFromDatabase(int pointNumber){
        Log.d(TAG, "checkPointNumber: Started...");
        BackgroundSurveyPointExistInDatabase dbChecker = new BackgroundSurveyPointExistInDatabase(
                mContext,
                mJob_DatabaseName,
                this,
                pointNumber);

        dbChecker.execute();

    }

    //---------------------------------------------------------------------------------------------- DatabaseDoesPointExistFromAsyncListener
    @Override
    public void doesPointExist(int pointNumber, boolean isPointFoundInDatabase) {
        if(isPointFoundInDatabase){
            //point exists in either the database, throw flag
            etPointNumber.setError(mContext.getResources().getString(R.string.cogo_point_no_exists));
            Log.d(TAG, "does point exist:btSaveObservation: Locked");
            ibGpsMeasure_Setup_Close.setClickable(false);
            isPointNoSet = false;

            if(!isGPSMeasureSetupMenuOpen){
                openMeasureSetupDialogBox();
            }

        }else{
            etPointNumber.setError(null);
            Log.d(TAG, "does point exist:btSaveObservation: UnLocked");
            ibGpsMeasure_Setup_Close.setClickable(true);
            isPointNoSet = true;
        }

    }

    //---------------------------------------------------------------------------------------------- Helpers
    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(mContext, data, Toast.LENGTH_LONG).show();

        }
    }

}
