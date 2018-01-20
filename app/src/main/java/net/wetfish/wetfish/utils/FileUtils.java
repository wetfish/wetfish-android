package net.wetfish.wetfish.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import net.wetfish.wetfish.data.FileContract.FileColumns;
import net.wetfish.wetfish.data.FileContract.Files;

/**
 * Created by ${Michael} on 11/4/2017.
 * <p>
 * Utility class for various methods relevant to handling files
 */

public class FileUtils {
    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //TODO:
    public static String getFileExtensionFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            String[] tokens = cursor.getString(column_index).split("\\.(?=[^\\.]+$)");
            return "." + tokens[1];
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Cursor getFilesData(Context context) {
        // Used within loader to obtain cursor data
        return context.getContentResolver().query(Files.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    public static Uri getFileData(Context context, int id) {
        return Files.CONTENT_URI.buildUpon().appendPath(Integer.toString(id)).build();
    }

    public static int insertFileData(Context context, String fileTitle, String fileTags,
                                      String fileDescription, long fileUploadDate, String fileExtension,
                                      String fileDeviceUri, String fileWetfishLocationUrl,
                                      String fileWetfishDeletionUrl) {

        //TODO: Potential for adding error checking
//        String selection = FileColumns.COLUMN_FILE_DEVICE_STORAGE_LINK + "=?" +
//                " and " + FileColumns.COLUMN_FILE_WETFISH_STORAGE_LINK + "=?";
//        String[] selectionArgs = new String[]{fileDeviceUri, fileWetfishLocationUrl};

//        Cursor filesDatabase = context.getContentResolver().query(FileContract.Files.CONTENT_URI,
//                null,
//                selection,
//                selectionArgs,
//                null);

        // Setup the content values
        ContentValues cv = new ContentValues();
        cv.put(FileColumns.COLUMN_FILE_TITLE, fileTitle);
        cv.put(FileColumns.COLUMN_FILE_TAGS, fileTags);
        cv.put(FileColumns.COLUMN_FILE_DESCRIPTION, fileDescription);
        cv.put(FileColumns.COLUMN_FILE_UPLOAD_TIME, fileUploadDate);
        cv.put(FileColumns.COLUMN_FILE_TYPE_EXTENSION, fileExtension);
        cv.put(FileColumns.COLUMN_FILE_DEVICE_STORAGE_LINK, fileDeviceUri);
        cv.put(FileColumns.COLUMN_FILE_WETFISH_STORAGE_LINK, fileWetfishLocationUrl);
        cv.put(FileColumns.COLUMN_FILE_WETFISH_DELETION_LINK, fileWetfishDeletionUrl);

        // Insert the content values into the database and get the position
        return Integer.valueOf(((context.getContentResolver().insert(Files.CONTENT_URI, cv)).getLastPathSegment()).toString());
    }
}
