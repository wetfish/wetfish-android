package net.wetfish.wetfish.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * File contract for Wetfish User DB
 *
 * Created by ${Michael} on 12/9/2017.
 */

public class FileContract {

    //Authority for our content provider
    public static final String AUTHORITY = "net.wetfish.wetfish";

    // Uri for the base content
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Paths for accessing data within file directory
    public static final String PATH_FILES = "files";

    public static final class Files implements BaseColumns {
        // Files content uri
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FILES).build();

        // Name for files table
        public static final String TABLE_NAME = "files";
    }

    public static final class FileColumns implements BaseColumns {
        // File name
        public static final String COLUMN_FILE_TITLE = "title";

        // File Tags
        public static final String COLUMN_FILE_TAGS = "tags";

        // File Description
        public static final String COLUMN_FILE_DESCRIPTION = "description";

        // File Upload Date
        public static final String COLUMN_FILE_UPLOAD_TIME = "uploadTime";

        // File Type
        public static final String COLUMN_FILE_TYPE_EXTENSION = "fileType";

        // File Internal Storage Link
        public static final String COLUMN_FILE_DEVICE_STORAGE_LINK = "deviceLocationLink";

        // File Wetfish Uploader Link
        public static final String COLUMN_FILE_WETFISH_STORAGE_LINK = "wetfishLocationLink";

        // File Wetfish Uploader Deletion Link
        public static final String COLUMN_FILE_WETFISH_DELETION_LINK = "wetfishDeletionLink";

        // File Wetfish Uploader Modified Storage Link
        public static final String COLUMN_FILE_WETFISH_EDITED_FILE_STORAGE_LINK =  "deviceLocationLinkEditedVersion";

    }
}
