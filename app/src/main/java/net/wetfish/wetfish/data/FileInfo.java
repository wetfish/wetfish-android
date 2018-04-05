package net.wetfish.wetfish.data;

import android.database.Cursor;

import net.wetfish.wetfish.data.FileContract.FileColumns;

import org.parceler.Parcel;

/**
 * Created by ${Michael} on 12/24/2017.
 */
@Parcel
public class FileInfo {
    public String fileTitle;
    public String fileTags;
    public String fileDescription;
    public String fileUploadTime;
    public String fileExtensionType;
    public String fileDeviceStorageLink;
    public String fileWetfishStorageLink;
    public String fileWetfishDeletionLink;
    public String editedFileDeviceStorageLink;
    public boolean fileInfoInitialized = false;

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
        // Initialize file
        fileInfoInitialized = true;

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
        editedFileDeviceStorageLink = cursor.getString(cursor.getColumnIndex(FileColumns.COLUMN_FILE_WETFISH_EDITED_FILE_STORAGE_LINK));

        // Close cursor
        cursor.close();
    }

    /**
     * Constructor that will take the cursor and populate a
     *
     * @param cursor of files database to a single position
     */
    public FileInfo(Cursor cursor, int integer) {
        // Initialize file
        fileInfoInitialized = true;

        // Move cursor to the available row
        cursor.moveToPosition(integer);

        // Populate data fields
        fileTitle = cursor.getString(cursor.getColumnIndex(FileColumns.COLUMN_FILE_TITLE));
        fileTags = cursor.getString(cursor.getColumnIndex(FileColumns.COLUMN_FILE_TAGS));
        fileDescription = cursor.getString(cursor.getColumnIndex(FileColumns.COLUMN_FILE_DESCRIPTION));
        fileUploadTime = cursor.getString(cursor.getColumnIndex(FileColumns.COLUMN_FILE_UPLOAD_TIME));
        fileExtensionType = cursor.getString(cursor.getColumnIndex(FileColumns.COLUMN_FILE_TYPE_EXTENSION));
        fileDeviceStorageLink = cursor.getString(cursor.getColumnIndex(FileColumns.COLUMN_FILE_DEVICE_STORAGE_LINK));
        fileWetfishStorageLink = cursor.getString(cursor.getColumnIndex(FileColumns.COLUMN_FILE_WETFISH_STORAGE_LINK));
        fileWetfishDeletionLink = cursor.getString(cursor.getColumnIndex(FileColumns.COLUMN_FILE_WETFISH_DELETION_LINK));
        editedFileDeviceStorageLink = cursor.getString(cursor.getColumnIndex(FileColumns.COLUMN_FILE_WETFISH_EDITED_FILE_STORAGE_LINK));

        // Close cursor
        cursor.close();
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

    public String getEditedFileDeviceStorageLink() {
        return editedFileDeviceStorageLink;
    }

    public boolean getFileInfoInitialized() {
        return fileInfoInitialized;
    }


}
