package com.survlogic.survlogic.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

import com.survlogic.survlogic.R;

/**
 * Created by chrisfillmore on 7/30/2017.
 */

public class SettingsJobCurrentActivity extends AppCompatPreferenceActivity {

    private static final String TAG = "SettingsJobCurrentActiv";

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            Log.d(TAG, "onPreferenceChange: Started");
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);


            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        Log.d(TAG, "bindPreferenceSummaryToValue: Started");
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Started--------------------------->");
        setupActionBar();

        addPreferencesFromResource(R.xml.pref_job_current_general);

        bindPreferences();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        

    }

    //<------------------------------------------------------------------------------------------------>//

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void bindPreferences(){
        Log.d(TAG, "bindPreferences: Started");

        bindPreferenceSummaryToValue(findPreference("pref_key_current_job_name"));
        bindPreferenceSummaryToValue(findPreference("pref_key_current_job_units"));



    }

    //<------------------------------------------------------------------------------------------------>//

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
                //startActivity(new Intent(this, SettingsActivity.class));
                //OR USING finish();
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }


    private void setupActionBar() {
        Log.d(TAG, "setupActionBar: Starting");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }else{
            Log.d(TAG, "setupActionBar: Action Bar returned null");
        }
    }


}
