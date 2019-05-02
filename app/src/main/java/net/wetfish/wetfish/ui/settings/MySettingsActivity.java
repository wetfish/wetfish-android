package net.wetfish.wetfish.ui.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import net.wetfish.wetfish.BuildConfig;
import net.wetfish.wetfish.R;

public final class MySettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            this.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new MySettingsActivity.SettingsFragment()).commit();
        } else {
            this.setTitle(savedInstanceState.getCharSequence("settingsActivityTitle"));
        }

        this.getSupportFragmentManager().addOnBackStackChangedListener((new FragmentManager.OnBackStackChangedListener() {
            public final void onBackStackChanged() {
                FragmentManager fragmentManager = MySettingsActivity.this.getSupportFragmentManager();
                if (fragmentManager.getBackStackEntryCount() == 0) {
                    setTitle(R.string.title_activity_settings);
                }

            }
        }));
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("settingsActivityTitle", this.getTitle());
    }

    public boolean onSupportNavigateUp() {
        return this.getSupportFragmentManager().popBackStackImmediate() ? true : super.onSupportNavigateUp();
    }

    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        Bundle args = pref.getExtras();
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment fragment = fragmentManager.getFragmentFactory()
                .instantiate(this.getClassLoader(), pref.getFragment(), args);
        fragment.setArguments(args);
        fragment.setTargetFragment((Fragment)caller, 0);
        this.getSupportFragmentManager().beginTransaction().replace(R.id.settings, fragment).addToBackStack((String)null).commit();
        this.setTitle(pref.getTitle());
        return true;
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static android.preference.Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new android.preference.Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(android.preference.Preference preference, Object value) {
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

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return super.getSharedPreferences(name, mode);
    }

//--

    public static final class SettingsFragment extends PreferenceFragmentCompat {
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            this.setPreferencesFromResource(R.xml.pref_root, rootKey);
        }
    }

    public static final class GeneralPreferenceFragment extends PreferenceFragmentCompat {
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            this.setPreferencesFromResource(R.xml.pref_general, rootKey);
        }
    }

//    public static final class NotificationPreferenceFragment extends PreferenceFragmentCompat {
//        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
//            this.setPreferencesFromResource(R.xml.pref_notification, rootKey);
//        }
//    }

    public static final class DataSyncPreferenceFragment extends PreferenceFragmentCompat {
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            this.setPreferencesFromResource((R.xml.pref_data_sync), rootKey);
        }
    }

    public static final class AboutPreferenceFragment extends PreferenceFragmentCompat {
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            this.setPreferencesFromResource(R.xml.pref_about, rootKey);

            // Bind the summaries to the information
            // Setup app version summary
            findPreference(getString(R.string.pref_appVersion_key)).setSummary(BuildConfig.VERSION_NAME);

            // Setup app release notes summary and button
            findPreference(getString(R.string.pref_appVersionSummary_key)).setSummary(getString(R.string.pref_appVersionSummary_prompt));
            findPreference(getString(R.string.pref_appVersionSummary_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                /**
                 * Called when a preference has been clicked.
                 *
                 * @param preference The preference that was clicked
                 * @return {@code true} if the click was handled
                 */
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Create dialog builder
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);


                    // Create layout inflater
                    LayoutInflater inflater =getActivity().getLayoutInflater();

                    // Inflate the dialog's custom layout
                    builder.setView(inflater.inflate(R.layout.dialog_custom_version_information, null))

//                            .setMessage(R.string.pref_appVersionSummary)
                            .setTitle(R.string.pref_appVersion_title);

                    builder.setPositiveButton(R.string.dialog_acknowledged, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Acknowledged
                        }
                    });

                    AlertDialog dialog = builder.create();

                    dialog.show();

                    return true;
                }
            });

            // Setup open source references settings summary and button
            findPreference(getString(R.string.pref_appOpenSourceLibraries_key)).setSummary(getString(R.string.pref_appOpenSourceLibraries_prompt));
            findPreference(getString(R.string.pref_appOpenSourceLibraries_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                /**
                 * Called when a preference has been clicked.
                 *
                 * @param preference The preference that was clicked
                 * @return {@code true} if the click was handled
                 */
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Open the OssLicenseMenuActivity
                    OssLicensesMenuActivity.setActivityTitle(getString(R.string.custom_license_title));
                    startActivity(new Intent(getActivity(), OssLicensesMenuActivity.class));

                    return true;
                }
            });
        }
    }
}