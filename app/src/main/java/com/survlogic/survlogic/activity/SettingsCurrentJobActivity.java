package com.survlogic.survlogic.activity;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.survlogic.survlogic.R;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsCurrentJobActivity extends AppCompatPreferenceActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
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
        loadHeadersFromResource(R.xml.pref_headers_job_current, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || NotesPreferenceFragment.class.getName().equals(fragmentName)
                || DisplayPreferenceFragment.class.getName().equals(fragmentName)
                || RawDataPreferenceFragment.class.getName().equals(fragmentName)
                || OptionsPreferenceFragment.class.getName().equals(fragmentName);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_job_current_general);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_name)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_type)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_over_projection)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_over_zone)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_over_units)));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsCurrentJobActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotesPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_job_current_notes);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_attr_client)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_attr_mission)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_attr_weather_general)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_attr_weather_temp)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_attr_weather_pres)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_attr_staff_chief)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_attr_staff_iman)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_attr_staff_rman)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_attr_staff_other)));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsCurrentJobActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DisplayPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_job_current_display);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_system_distance_display)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_system_distance_precision_display)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_system_angle_display)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_format_coord_entry)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_format_angle_hz_display)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_format_angle_hz_obsrv_entry)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_format_angle_vz_obsrv_display)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_current_job_format_distance_hz_obsrv_display)));

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsCurrentJobActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class RawDataPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_job_current_raw_data);
            setHasOptionsMenu(true);

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsCurrentJobActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class OptionsPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_job_current_options);
            setHasOptionsMenu(true);

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsCurrentJobActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
