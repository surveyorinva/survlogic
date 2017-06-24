package com.survlogic.survlogic.activity;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import java.util.List;

/**
 * Created by chrisfillmore on 6/10/2017.
 */

public class SettingsGpsSurveyActivity extends AppCompatPreferenceActivity {
        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
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

        /**
         * Helper method to determine if the device has an extra-large screen. For
         * example, 10" tablets are extra-large.
         */
        private static boolean isXLargeTablet(Context context) {
            return (context.getResources().getConfiguration().screenLayout
                    & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
        }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.
         *
         * @see #sBindPreferenceSummaryToValueListener
         */
        private static void bindPreferenceSummaryToValue(Preference preference) {
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }

    boolean mAttachedFragment;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            mAttachedFragment = false;
            super.onCreate(savedInstanceState);

        }

        /**
         * Set up the {@link android.app.ActionBar}, if the API is available.
         */
        private void setupActionBar() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                // Show the Up button in the action bar.
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean onIsMultiPane() {
            return isXLargeTablet(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void onBuildHeaders(List<Header> target) {
            loadHeadersFromResource(R.xml.pref_headers_gps_survey, target);
        }


    @Override
    public void onAttachFragment(Fragment fragment) {
        mAttachedFragment = true;
        super.onAttachFragment(fragment);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //if we didn't attach a fragment, go ahead and apply the layout
        if (!mAttachedFragment) {
            setContentView(R.layout.activity_gps_survey_settings);
            setSupportActionBar((Toolbar)findViewById(R.id.toolbar_in_app_bar_layout_settings));

            getSupportActionBar().setTitle("Settings");

        }
    }


    /**
         * This method stops fragment injection in malicious applications.
         * Make sure to deny any unknown fragments here.
         */
        protected boolean isValidFragment(String fragmentName) {
            return PreferenceFragment.class.getName().equals(fragmentName)
                    || com.survlogic.survlogic.activity.SettingsGpsSurveyActivity.GeneralPreferenceFragment.class.getName().equals(fragmentName)
                    || com.survlogic.survlogic.activity.SettingsGpsSurveyActivity.SurveyPreferenceFragment.class.getName().equals(fragmentName);


        }



        /**
         * This fragment shows general preferences only. It is used when the
         * activity is showing a two-pane settings UI.
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public static class GeneralPreferenceFragment extends PreferenceFragment {
            @Override
            public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                addPreferencesFromResource(R.xml.pref_gps_survey_general);
                setHasOptionsMenu(true);

            }

            @Override
            public boolean onOptionsItemSelected(MenuItem item) {
                int id = item.getItemId();
                if (id == android.R.id.home) {
                    startActivity(new Intent(getActivity(), com.survlogic.survlogic.activity.SettingsGpsSurveyActivity.class));
                    return true;
                }
                return super.onOptionsItemSelected(item);
            }
        }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SurveyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_gps_survey_survey);
            setHasOptionsMenu(true);

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), com.survlogic.survlogic.activity.SettingsGpsSurveyActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

}
