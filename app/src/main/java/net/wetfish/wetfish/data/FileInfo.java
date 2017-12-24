package net.wetfish.wetfish.data;

import android.database.Cursor;

import net.wetfish.wetfish.data.FileContract.FileColumns;

import org.parceler.Parcel;

/**
 * Created by ${Michael} on 12/24/2017.
 */
@Parcel
public class FileInfo {
    private String fileTitle;
    private String fileTags;
    private String fileDescription;
    private String fileUploadTime;
    private String fileExtensionType;
    private String fileDeviceStorageLink;
    private String fileWetfishStorageLink;

    /**
     * Default Constructor
     */
    public FileInfo() {

    }



    /**
     * Constructor that will take the cursor and populate a
     *
     * @param cursor of files database to a single position
     */
    public FileInfo(Cursor cursor) {

        // Move cursor to the available row
        cursor.moveToFirst();

        // Populate data fields
        fileTitle = cursor.getString(cursor.getColumnIndex(FileColumns.COLUMN_FILE_TITLE));
        fileTags = cursor.getString(cursor.getColumnIndex(FileColumns.COLUMN_FILE_TAGS));
        fileDescription = cursor.getString(cursor.getColumnIndex(FileColumns.COLUMN_FILE_DESCRIPTION));
        fileUploadTime = cursor.getString(cursor.getColumnIndex(FileColumns.COLUMN_FILE_UPLOAD_TIME));
        fileExtensionType = cursor.getString(cursor.getColumnIndex(FileColumns.COLUMN_FILE_TYPE_EXTENSION));
        fileDeviceStorageLink = cursor.getString(cursor.getColumnIndex(FileColumns.COLUMN_FILE_DEVICE_STORAGE_LINK));
        fileWetfishStorageLink = cursor.getString(cursor.getColumnIndex(FileColumns.COLUMN_FILE_WETFISH_STORAGE_LINK));
        fileWetfishDeletionLink = cursor.getString(cursor.getColumnIndex(FileColumns.COLUMN_FILE_WETFISH_DELETION_LINK));
    }

    public String getFileTitle() {
        return fileTitle;
    }

    public String getFileTags() {
        return fileTags;
    }

    public String getFileDescription() {
        return fileDescription;
    }

    public String getFileUploadTime() {
        return fileUploadTime;
    }

    public String getFileExtensionType() {
        return fileExtensionType;
    }

    public String getFileDeviceStorageLink() {
        return fileDeviceStorageLink;
    }

    public String getFileWetfishStorageLink() {
        return fileWetfishStorageLink;
    }

    public String getFileWetfishDeletionLink() {
        return fileWetfishDeletionLink;
    }

    private String fileWetfishDeletionLink;

}
