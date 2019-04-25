package net.wetfish.wetfish.data;


import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import net.wetfish.wetfish.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import androidx.appcompat.app.AlertDialog;

import static net.wetfish.wetfish.data.FileContract.FileColumns;
import static net.wetfish.wetfish.data.FileContract.Files;

/**
 * DB Helper for Wetfish DB
 * <p>
 * Created by ${Michael} on 12/9/2017.
 */

public class FileDbHelper extends SQLiteOpenHelper {

    //Database File Name
    public static final String DATABASE_NAME = "file.db";

    // Current version of the database schema
    public static final int DATABASE_VERSION = 2;

    // Database upgrade number
    public static final int DATABASE_VERSION_2 = 2;

    /**
     * Create a helper object to create, open, and/or manage a database.
     * This method always returns very quickly.  The database is not actually
     * created or opened until one of {@link #getWritableDatabase} or
     * {@link #getReadableDatabase} is called.
     *
     * @param context to use to open or create the database
     */
    public FileDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creating SQL string command to create an SQL Table
        final String SQL_CREATE_FILES_TABLE =
                "CREATE TABLE " + Files.TABLE_NAME + " (" +
                        Files._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        FileColumns.COLUMN_FILE_TITLE + " TEXT, " +
                        FileColumns.COLUMN_FILE_TAGS + " TEXT, " +
                        FileColumns.COLUMN_FILE_DESCRIPTION + " TEXT, " +
                        FileColumns.COLUMN_FILE_UPLOAD_TIME + " INTEGER NOT NULL, " +
                        FileColumns.COLUMN_FILE_TYPE_EXTENSION + " TEXT NOT NULL, " +
                        FileColumns.COLUMN_FILE_DEVICE_STORAGE_LINK + " TEXT NOT NULL, " +
                        FileColumns.COLUMN_FILE_WETFISH_STORAGE_LINK + " TEXT NOT NULL, " +
                        FileColumns.COLUMN_FILE_WETFISH_DELETION_LINK + " TEXT NOT NULL," +
                        FileColumns.COLUMN_FILE_EDITED_DEVICE_STORAGE_LINK + " TEXT);";

        db.execSQL(SQL_CREATE_FILES_TABLE);
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p>
     * If you add new columns you can use ALTER TABLE to insert them into a live table.
     * If you rename or remove columns you can use ALTER TABLE to rename the old table,
     * then create the new table and then populate the new table with the contents of the old table.
     * <p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < DATABASE_VERSION_2) {
            String upgradeQuery = "ALTER TABLE " + Files.TABLE_NAME + " ADD COLUMN " +
                    FileColumns.COLUMN_FILE_EDITED_DEVICE_STORAGE_LINK + " TEXT";
            db.execSQL(upgradeQuery);
        }
    }
  
    /**
     * Method to export the Wetfish DB to fileBackup.db
     *
     * @param context Calling activity's context
     * @param rootView View reference of the calling activity to create Snackbars asynchronously with Alert Dialog
     * @return
     */
    public static String onExportDB(final Context context, final View rootView) {

        // Acquire the file system paths
        File internalStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File dataDirectory = Environment.getDataDirectory();

        // Create the paths regarding the various file locations
        String currentDBPath = "//data//" + context.getApplicationInfo().packageName + "//databases//file.db";
        String backupDBPath = "//fileBackup.db";

        // Setup the files pointing to the proper locations
        final File currentDB = new File(dataDirectory, currentDBPath);
        final File backupDB = new File(internalStorageDirectory, backupDBPath);

        // Check to see if a database exists
        if (currentDB.exists()) {
            Log.d("onExportDB 1st If", "This DB Exists" + "\n" + currentDBPath);

            // Check to see if a backup DB exists, if a backup DB exists ask the user if  they want to overwrite it or not
            if (backupDB.exists()) {
                // Arrays to reference a variable in a final context and still edit the value
                final String[] returnString = new String[1];

                // Mark the string as null so we can avoid making a snackbar
                returnString[0] = null;

                // Setup the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogTheme);
                builder.setMessage(R.string.ad_message_overwrite_last_export_warning)
                        .setTitle(R.string.ad_title_overwrite_last_export_title)
                        .setPositiveButton(R.string.ad_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User decided to create a new database export and overwrite the old one
                                returnString[0] = exportDB(backupDB, currentDB, context);
                                Snackbar.make(rootView, returnString[0], Snackbar.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton(R.string.ad_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User decided to not create a new database.
                                returnString[0] = context.getString(R.string.sb_db_export_stopped);
                                Snackbar.make(rootView, returnString[0], Snackbar.LENGTH_LONG).show();

                            }
                        });

                // Create and show the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();

                //Return null here because the snackbar will be created inside the AlertDialog because of its asynchronous execution.
                return null;
            } else {
                return exportDB(backupDB, currentDB, context);
            }
        } else {
            // Inform the user no database exists to export
            return context.getString(R.string.sb_db_export_nothing_to_export);
        }
    }

    /**
     * Method to import the fileBackup.db into Wetfish.
     *
     * @param context Calling activity's context
     * @param rootView View reference of the calling activity to create Snackbars asynchronously with Alert Dialog
     * @return
     */
    public static String onImportDB(final Context context, final View rootView) {
        // Acquire the file system paths
        File internalStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File dataDirectory = Environment.getDataDirectory();

        // Create the paths regarding the various file locations
        String currentDBPath = "//data//" + context.getApplicationInfo().packageName + "//databases//file.db";
        String currentDBBackupPath = "//currentFileBackupUniqueWetfishDatabase.db";
        String backupDBPath = "//fileBackup.db";

        // Setup the files pointing to the proper locations
        final File currentDB = new File(dataDirectory, currentDBPath);
        final File currentDBBackup = new File(internalStorageDirectory, currentDBBackupPath);
        final File backupDB = new File(internalStorageDirectory, backupDBPath);

        // Arrays to reference a variable in a final context and still edit the value
        final String[] returnString = new String[1];

        // Mark the string as null so we can avoid making a snackbar
        returnString[0] = null;

        // Setup the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogTheme);
        builder.setMessage(R.string.ad_message_import_last_export_warning)
                .setTitle(R.string.ad_title_import_title)
                .setPositiveButton(R.string.ad_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User decided to create a new database export and overwrite the old one

                        // Check to see if an exported database exists
                        if (backupDB.exists()) {
                            // Import the database
                            returnString[0] = importDB(backupDB, currentDB, currentDBBackup, context);
                        } else {
                            // Database wasn't found to import
                            returnString[0] = context.getString(R.string.sb_db_import_not_found);
                        }

                        // Snackbar result
                        if (returnString[0] != null) {
                            Snackbar.make(rootView, returnString[0], Snackbar.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(R.string.ad_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User decided to not create a new database.
                        returnString[0] = context.getString(R.string.sb_db_import_stopped);

                        // Snackbar result
                        Snackbar.make(rootView, returnString[0], Snackbar.LENGTH_LONG).show();

                    }
                });

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

        //Return null here because the snackbar will be created inside the AlertDialog because of its asynchronous execution.
        return null;
    }

    /**
     * This method helps simplify onExportDB by moving the file exporting logic.
     *
     * @param backupDB  The file reference of the location of where to create or overwrite the fileBackup.db
     * @param currentDB The file reference of the location of where the Wetfish app database resides
     * @param context   The calling activity's context
     * @return
     */
    private static String exportDB(File backupDB, File currentDB, Context context) {
        try {
            backupDB.createNewFile();

            if (backupDB.exists()) {
                // Create the File I/O streams necessary to transfer the data
                FileChannel source = new FileInputStream(currentDB).getChannel();
                FileChannel destination = new FileOutputStream(backupDB).getChannel();
                destination.transferFrom(source, 0, source.size());

                // Verify the data was properly transferred
                if (source.size() == destination.size()) {
                    source.close();
                    return context.getString(R.string.sb_db_export_verification_success);
                } else {
                    source.close();
                    return context.getString(R.string.sb_db_export_verification_failure);
                }
            } else {
//            Log.d("onExportDB 2nd If", "This DB Doesn't Exist" + "\n" + backupDBPath + "\n" + internalStorageDirectory + backupDBPath);
                return context.getString(R.string.sb_db_export_creation_failure);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            // In case of failure delete the DB
            if (backupDB.exists() && backupDB != null) {
                backupDB.delete();
            }

            // exporting failed
            return context.getString(R.string.sb_db_file_not_found_exception);
        } catch (IOException e) {
            e.printStackTrace();

            // In case of failure delete the DB
            if (backupDB.exists() && backupDB != null) {
                backupDB.delete();
            }

            // exporting failed
            return context.getString(R.string.sb_db_file_io_exception);
        }
    }

    /**
     * This method helps simplify onImportDB by moving the file importing logic.
     *
     * @param backupDB  The file reference of the location of where to find fileBackup.db
     * @param currentDB The file reference of the location of where the Wetfish app database resides
     * @param currentDBBackup The file reference of the location of where the Wetfish app database is temporarily stored in case of errors
     * @param context   The calling activity's context
     * @return
     */
    private static String importDB(File backupDB, File currentDB, File currentDBBackup, Context context) {
        try {
            if (!currentDB.exists()) {
                // If a database doesn't currently exist create a new file for destination to populate
                currentDB.createNewFile();
                currentDBBackup = null;
            } else {
                // If a database does currently exist create a temporary file for it to restore if importing goes astray.
                FileChannel currentDBSource = new FileInputStream(currentDB).getChannel();
                FileChannel destination = new FileOutputStream(currentDBBackup).getChannel();
                destination.transferFrom(currentDBSource, 0,  currentDBSource.size());

                if (currentDBSource.size() == destination.size()) {
                    // Close source
                    currentDBSource.close();
                } else {
                    // Close source and delete the backup, backup method failed.
                    currentDBSource.close();
                    currentDBBackup.delete();
                }
            }

            // Create the File I/O streams necessary to transfer the data
            FileChannel source = new FileInputStream(backupDB).getChannel();
            FileChannel destination = new FileOutputStream(currentDB).getChannel();
            destination.transferFrom(source, 0, source.size());

            // Verify the data was properly transferred
            if (source.size() == destination.size()) {
                // Close source and then delete the backup.
                source.close();
                currentDBBackup.delete();

                // Inform the user of the import success
                return context.getString(R.string.sb_db_import_verification_success);
            } else {
                // Close source
                source.close();

                // Try and revert from the backup copy if it exists. If all else fails, let the user know.
                if (currentDBBackup != null) {
                    // Transfer the data back from the temporary backup
                    FileChannel preImportBackupSource = new FileInputStream(currentDBBackup).getChannel();
                    destination.transferFrom(preImportBackupSource, 0,  preImportBackupSource.size());

                    if (preImportBackupSource.size() == destination.size()) {
                        // Close preImportBackupSource and delete the backup, backup method succeeded.
                        preImportBackupSource.close();
                        currentDBBackup.delete();

                        // Inform the user of the import failure and successful revert
                        return context.getString(R.string.sb_db_import_verification_failure);
                    } else {
                        // Close preImportBackupSource and delete the backup, backup method failed.
                        preImportBackupSource.close();
                        currentDBBackup.delete();

                        // Inform the user of the import failure and unsuccessful revert.
                        return context.getString(R.string.sb_db_import_verification_failure_plus);
                    }
                } else {
                    // Since no database existed before delete the failed import and close source
                    source.close();
                    currentDB.delete();

                    // Inform the user of the import failure.
                     return context.getString(R.string.sb_db_import_verification_failure);
                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();

            // In case of failure delete the DB backup
            if (currentDBBackup.exists() && currentDBBackup != null) {
                currentDBBackup.delete();
            }

            // Inform the user of the import failure.
            return context.getString(R.string.sb_db_file_not_found_exception);
        } catch (IOException e) {
            e.printStackTrace();

            // In case of failure delete the DB backup
            if (currentDBBackup.exists() && currentDBBackup != null) {
                currentDBBackup.delete();
            }

            // Inform the user of the import failure.
            return context.getString(R.string.sb_db_file_io_exception);
        }
    }
}
