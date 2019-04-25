package net.wetfish.wetfish.ui.settings;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.snackbar.Snackbar;

import net.wetfish.wetfish.BuildConfig;
import net.wetfish.wetfish.R;
import net.wetfish.wetfish.data.FileDbHelper;

public final class MySettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
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
        fragment.setTargetFragment((Fragment) caller, 0);
        this.getSupportFragmentManager().beginTransaction().replace(R.id.settings, fragment).addToBackStack((String) null).commit();
        this.setTitle(pref.getTitle());
        return true;
    }

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

        // Permission String array for @requestStoragePermission
        private static final int REQUEST_STORAGE = 0;
        private static final String[] PERMISSIONS_STORAGE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            this.setPreferencesFromResource((R.xml.pref_data_sync), rootKey);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
//            bindPreferenceSummaryToValue(findPreference("sync_frequency"));

            // Setup export database button
            findPreference(getString(R.string.pref_appExportDatabase_key)).setSummary(getString(R.string.pref_appExportDatabase_prompt));
            findPreference(getString(R.string.pref_appExportDatabase_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Ask for storage permission if @ or above Android version 23
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED
                                || ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            // Permissions have not been granted, inform the user and ask again
                            requestStoragePermission();
                        } else {
                            // Storage permissions granted, export the current Wetfish database
                            String onExportDBResult = FileDbHelper.onExportDB(getActivity(), getActivity().findViewById(android.R.id.content));

                            if (onExportDBResult != null) {
                                Snackbar.make(getActivity().findViewById(android.R.id.content), onExportDBResult, Snackbar.LENGTH_LONG).show();
                            } else {
                                //Do nothing, the snackbar will be created asynchronously within the AlertDialog
                            }
                        }
                    } else {
                        // Export the current Wetfish database, storage permissions are allowed
                        String onExportDBResult = FileDbHelper.onExportDB(getActivity(), getActivity().findViewById(android.R.id.content));

                        if (onExportDBResult != null) {
                            Snackbar.make(getActivity().findViewById(android.R.id.content), onExportDBResult, Snackbar.LENGTH_LONG).show();
                        } else {
                            //Do nothing, the snackbar will be created asynchronously within the AlertDialog
                        }
                    }
                    return true;
                }
            });

            // Setup import database button
            findPreference(getString(R.string.pref_appImportDatabase_key)).setSummary(getString(R.string.pref_appImportDatabase_prompt));
            findPreference(getString(R.string.pref_appImportDatabase_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Ask for storage permission if @ or above Android version 23
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED
                                || ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            // Permissions have not been granted, inform the user and ask again
                            requestStoragePermission();
                        } else {
                            // Storage permissions granted, import the available Wetfish database
                            String onImportDBResult = FileDbHelper.onImportDB(getActivity(), getActivity().findViewById(android.R.id.content));

                            if (onImportDBResult != null) {
                                Snackbar.make(getActivity().findViewById(android.R.id.content), onImportDBResult, Snackbar.LENGTH_LONG).show();
                            } else {
                                //Do nothing, the snackbar will be created asynchronously within the AlertDialog
                            }
                        }
                    } else {
                        // Import the available Wetfish database, storage permissions are allowed
                        String onImportDBResult = FileDbHelper.onImportDB(getActivity(), getActivity().findViewById(android.R.id.content));

                        if (onImportDBResult != null) {
                            Snackbar.make(getActivity().findViewById(android.R.id.content), onImportDBResult, Snackbar.LENGTH_LONG).show();
                        } else {
                            //Do nothing, the snackbar will be created asynchronously within the AlertDialog
                        }

                    }

                    return true;
                }
            });
        }

        // Method to request storage permissions
        @RequiresApi(api = Build.VERSION_CODES.M)
        private void requestStoragePermission() {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // If the user has previously denied granting the permission, offer the rationale
                Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.sb_permission_storage_rationale_export_import,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.sb_ok, new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public void onClick(View view) {
                                requestPermissions(PERMISSIONS_STORAGE, REQUEST_STORAGE);
                            }
                        }).show();
            } else {
                // No explanation needed, request permission
                {
                    requestPermissions(PERMISSIONS_STORAGE, REQUEST_STORAGE);
                }
            }
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
                    LayoutInflater inflater = getActivity().getLayoutInflater();

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