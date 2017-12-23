package net.wetfish.wetfish.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import net.wetfish.wetfish.data.FileContract.Files;

/**
 * Created by ${Michael} on 12/9/2017.
 */

public class FileContentProvider extends ContentProvider {

    // Directory of files
    public static final int FILES_DIRECTORY_CODE = 0;

    // Items in directory of files
    public static final int FILES_WITH_ID_DIRECTORY_CODE = 1;

    // URI Matcher
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // Database Helper Memeber Variable
    private FileDbHelper mFileDbHelper;

    // Associates URI's with their int match!
    private static UriMatcher buildUriMatcher() {
        // Initializing the UriMatcher
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Directory Uri Matcher
        uriMatcher.addURI(FileContract.AUTHORITY, FileContract.PATH_FILES, FILES_DIRECTORY_CODE);

        // Single Item Uri Matcher
        uriMatcher.addURI(FileContract.AUTHORITY, FileContract.PATH_FILES + "/#", FILES_WITH_ID_DIRECTORY_CODE);

        return uriMatcher;
    }


    @Override
    public boolean onCreate() {
        Context context = getContext();
        mFileDbHelper = new FileDbHelper(context);
        return true;
    }

    /**
     * Create a basic query
     *
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Acquiring the database to enact queries
        SQLiteDatabase db = mFileDbHelper.getReadableDatabase();

        // Return the produced cursor
        Cursor returnCursor;

        // Acquire the Uri Matcher's match whether we are getting a certain item or a directory
        switch (sUriMatcher.match(uri)) {
            // Entire files database
            case FILES_DIRECTORY_CODE:
                Log.d("Qlorp", "Shlorp drop Query Total");
                returnCursor = db.query(Files.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            // Specific files database row
            case FILES_WITH_ID_DIRECTORY_CODE:
                Log.d("Slorp", "Shlorp drop Query JUAN");

                // Get id of provided uri
                String filesID = uri.getLastPathSegment();

                // Selection argument (id)
                String[] filesSelectionArguments = new String[]{filesID};

                // Query the database for the appropriate row with (id)
                returnCursor = db.query(Files.TABLE_NAME,
                        projection,
                        Files._ID+ "=?",
                        filesSelectionArguments,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                // No matches to URI
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        // Notify cursor changes
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the resulting cursor
        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    /**
     * Insert a row into the files database
     *
     * @param uri
     * @param contentValues
     * @return
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        // Acquire database to enact insertions
        SQLiteDatabase db = mFileDbHelper.getWritableDatabase();

        // Returns the successful Uri
        Uri returnUri;

        // Acquire the Uri Matcher's match
        switch (sUriMatcher.match(uri)) {
            // Entire files database
            case FILES_DIRECTORY_CODE:
                // Insert new values into the database
                long filesID = db.insert(Files.TABLE_NAME, null, contentValues);
                if (filesID > 0) {
                    returnUri = ContentUris.withAppendedId(Files.CONTENT_URI, filesID);
                    Log.d("Yo!", "It worked");
                } else {
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        // Notify the resolver of any potential uri changes
        getContext().getContentResolver().notifyChange(uri, null);

        // Return constructed uri that points of inserted row of data
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Acquire the database to enact deletions upon
        SQLiteDatabase db = mFileDbHelper.getWritableDatabase();

        // Return int
        int rowsDeleted;

        // Acquire the Uri Matcher's match and whether we are deleting a atabase or a row
        switch(sUriMatcher.match(uri)) {
            // Delete files directory
            case FILES_DIRECTORY_CODE:
                rowsDeleted = db.delete(Files.TABLE_NAME, selection, selectionArgs);
                break;
            case FILES_WITH_ID_DIRECTORY_CODE:
                // last path segment
                int lastPathSegment = 1;
                // Acquire file ID
                String filesID = uri.getPathSegments().get(lastPathSegment);
                // Delete the row(s) at selectionArgs
                rowsDeleted = db.delete(Files.TABLE_NAME, "_id=?", new String[]{filesID});
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        // Notify the resolver of any potential uri changes
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the amount of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
