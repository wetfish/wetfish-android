package net.wetfish.wetfish.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static net.wetfish.wetfish.data.FileContract.FileColumns;
import static net.wetfish.wetfish.data.FileContract.Files;

/**
 * Created by ${Michael} on 12/9/2017.
 */

public class FileDbHelper extends SQLiteOpenHelper{

    //Database File Name
    public static final String DATABASE_NAME = "file.db";

    // Current version of the database schema
    public static final int DATABASE_VERSION = 1;

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
                        FileColumns.COLUMN_FILE_WETFISH_DELETION_LINK + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_FILES_TABLE);
    }

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
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     *
     * If you add new columns you can use ALTER TABLE to insert them into a live table.
     * If you rename or remove columns you can use ALTER TABLE to rename the old table,
     * then create the new table and then populate the new table with the contents of the old table.
     *
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     *
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Files.TABLE_NAME);
        onCreate(db);
    }
}
