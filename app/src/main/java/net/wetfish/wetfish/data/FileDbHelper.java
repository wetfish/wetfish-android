package net.wetfish.wetfish.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

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

    public static boolean onExportDB(Context context) {
        File backupDB = null;

        try {
            // Acquire the file system paths
            File internalStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File dataDirectory = Environment.getDataDirectory();

            // Create the paths regarding the various file locations
            String currentDBPath = "//data//" + context.getApplicationInfo().packageName + "//databases//file.db";
            String backupDBPath = "//fileBackup.db";

            File currentDB = new File(dataDirectory, currentDBPath);
            backupDB = new File(internalStorageDirectory, backupDBPath);

//            File currentDB = new File(currentDBPath);
//            backupDB = new File(backupDBPath);


            Log.d("onExportDB", dataDirectory.toString() + currentDBPath  +
                    "\n" + internalStorageDirectory.toString() + backupDBPath);

            Log.d("Dammit", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + "\n" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    + "\n" + Environment.getExternalStorageDirectory()
                    + "\n" + Environment.getDownloadCacheDirectory()
                    + "\n" + Environment.getDataDirectory());

            // Check to see if a database exists
            if (currentDB.exists()) {
                Log.d("onExportDB 1st If", "This DB Exists" + "\n" + currentDBPath);
                backupDB.createNewFile();
                if (backupDB.exists()) {
                    Log.d("onExportDB 2nd If", "This DB Exists" + "\n" + backupDBPath);
                    FileChannel source = new FileInputStream(currentDB).getChannel();
                    FileChannel destination = new FileOutputStream(backupDB).getChannel();
                    destination.transferFrom(source, 0, source.size());

                    source.close();

//                destination.close();

                    // TODO: Add a function to check if the databases are exact copies.
                    return true;
                } else {
                    Log.d("onExportDB 2nd If", "This DB Doesn't Exist" + "\n" + backupDBPath + "\n" +
                            internalStorageDirectory + backupDBPath);
                    return false;
                }
            } else {
                // Return false, probably return a better variable, number perhaps
                // TODO: Refine Error Return
                return false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            // In case of failure delete the DB
            if (backupDB.exists() && backupDB != null) {
                backupDB.delete();
                Log.d("Shoot  Willis", "Boom Boom");
            }

            // exporting failed
            return false;
        } catch (IOException e) {
            e.printStackTrace();

            // In case of failure delete the DB
            if (backupDB.exists() && backupDB != null) {
                backupDB.delete();
                Log.d("Shoot  Willis", "Boom Boom");
            }

            // exporting failed
            return false;
        }
    }


    public static boolean onImportDB(Context context) {

        try {
            // Acquire the file system paths
            File internalStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File dataDirectory = Environment.getDataDirectory();

            // Create the paths regarding the various file locations
            String currentDBPath = "//data//" + context.getApplicationInfo().packageName + "//databases//file.db";
            String backupDBPath = "//fileBackup.db";

            File currentDB = new File(dataDirectory, currentDBPath);
            File backupDB = new File(internalStorageDirectory, backupDBPath);

//            File currentDB = new File(currentDBPath);
//            backupDB = new File(backupDBPath);


            Log.d("onExportDB", dataDirectory.toString() + currentDBPath  +
                    "\n" + internalStorageDirectory.toString() + backupDBPath);

            Log.d("Dammit", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + "\n" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    + "\n" + Environment.getExternalStorageDirectory()
                    + "\n" + Environment.getDownloadCacheDirectory()
                    + "\n" + Environment.getDataDirectory());

            // Check to see if a database exists
            if (backupDB.exists()) {
                Log.d("onExportDB 2nd If", "This DB Exists" + "\n" + backupDBPath);

                if (currentDB.exists()) {
                    Log.d("onExportDB 1st If", "This DB Exists" + "\n" + currentDBPath);

                    FileChannel source = new FileInputStream(backupDB).getChannel();
                    FileChannel destination = new FileOutputStream(currentDB).getChannel();
                    destination.transferFrom(source, 0, source.size());

                    source.close();

                    // TODO: Add a function to check if the databases are exact copies.
                    return true;
                } else {
                    Log.d("onExportDB 2nd If", "This DB Doesn't Exist" + "\n" + backupDBPath + "\n" +
                            internalStorageDirectory + backupDBPath);
                    return false;
                }
            } else {
                // Return false, probably return a better variable, number perhaps
                // TODO: Refine Error Return
                return false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // exporting failed
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            // exporting failed
            return false;
        }
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
}
