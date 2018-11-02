package net.wetfish.wetfish.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

import net.wetfish.wetfish.R;
import net.wetfish.wetfish.data.FileContract.FileColumns;
import net.wetfish.wetfish.data.FileContract.Files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ${Michael} on 11/4/2017.
 * <p>
 * Utility class for various methods relevant to handling files
 */

public class FileUtils {

    private static final double UNIT_CONVERSION = 1000;
    private static final double ROUNDING_NUMBER = 100.0;

    private static final double MINIMUM_BITMAP_RESOLUTION = 1;

    public static String getAbsolutePathFromUri(Context context, Uri contentUri) {
        String fileProviderString = "(/net.wetfish.wetfish/)";
        String capturedFileString = "(CAPTURED_FILE_)";
        String storageString = "(/storage/)";
        Pattern fileProviderPattern = Pattern.compile(capturedFileString);
        Pattern capturedFilePattern = Pattern.compile(fileProviderString);
        Pattern storagePattern = Pattern.compile(storageString);
        Matcher fileProviderMatcher = fileProviderPattern.matcher(contentUri.toString());
        Matcher capturedFileMatcher = capturedFilePattern.matcher(contentUri.toString());
        Matcher storageStringMatcher = storagePattern.matcher(contentUri.toString());

        if (fileProviderMatcher.find() || capturedFileMatcher.find() || storageStringMatcher.find()) {
            // provided uri is already the file path
            return contentUri.toString();
        } else {
            Cursor cursor = null;
            try {
                Log.d("FileUtils[gRPFU]: ", contentUri.toString());
                String[] proj = {MediaStore.Images.Media.DATA};
                cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();

                // Use cursor to store column_index string
                String columnIndex = cursor.getString(column_index);

                // Close Cursor
                cursor.close();

                // Return value
                return columnIndex;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public static String getFileExtensionFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;

        // RegEx Matcher to see where the file came from and determine the means to get the file extension
        Log.d("FileUtils[gFEFU]: ", "contentUri: " + contentUri.toString());
        String patternString = "(?:/storage/)";
        String patternStringTwo = "(content:)";
        Pattern pattern = Pattern.compile(patternString);
        Pattern patternTwo = Pattern.compile(patternStringTwo);
        Matcher storageMatcher = pattern.matcher(contentUri.toString());
        Matcher contentMatcher = patternTwo.matcher(contentUri.toString());
        if (storageMatcher.find()) {
            String[] tokens = contentUri.toString().split("\\.(?=[^\\.]+$)");
            return "." + tokens[tokens.length - 1];
        }

        if (contentMatcher.find()) {
            String[] tokens = contentUri.toString().split("\\.(?=[^\\.]+$)");
            return "." + tokens[tokens.length - 1];
        }

        // Try a RegEx matcher for files within MediaStore.Images.Media.Data
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            Log.d("FileUtils[gFEFU]: ", "Cursor try lewppp");

            String[] tokens = cursor.getString(column_index).split("\\.(?=[^\\.]+$)");
            cursor.close();
            return "." + tokens[1];
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Cursor getFilesData(Context context) {
        // Used within loader to obtain cursor data
        // Read the current preferences of the user to determine which cursor to provide back to the user
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        boolean sortByMostRecentSetting = sharedPref.getBoolean(context.getString(R.string.pref_sortByMostRecent_key),
                context.getResources().getBoolean(R.bool.pref_sortByMostRecent_default_value));

        // String for the sortOrder of the cursor
        String sortOrder = null;

        Log.d("Blorp", "Settings Stuff" + sortByMostRecentSetting);

        if (sortByMostRecentSetting) {
            // User selected for sorting of the newest first
            sortOrder = FileColumns.COLUMN_FILE_UPLOAD_TIME + " DESC";
        } else {
            // User selected for sorting of the oldest first
            sortOrder = FileColumns.COLUMN_FILE_UPLOAD_TIME + " ASC";
        }

        // Return the desired data set
        return context.getContentResolver().query(Files.CONTENT_URI,
                null,
                null,
                null,
                sortOrder);
    }

    public static Uri getFileUri(int id) {
        return Files.CONTENT_URI.buildUpon().appendPath(Integer.toString(id)).build();
    }

    /**
     * Database method that inserts the uploaded file's data into the database upon a successful upload
     *
     * @param context
     * @param fileTitle
     * @param fileTags
     * @param fileDescription
     * @param fileUploadDate
     * @param fileExtension
     * @param fileDeviceUri
     * @param fileWetfishLocationUrl
     * @param fileWetfishDeletionUrl
     * @param editedFileDeviceUri
     * @return
     */
    public static int insertFileData(Context context, String fileTitle, String fileTags,
                                     String fileDescription, long fileUploadDate, String fileExtension,
                                     String fileDeviceUri, String fileWetfishLocationUrl,
                                     String fileWetfishDeletionUrl, String editedFileDeviceUri) {

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
        cv.put(FileColumns.COLUMN_FILE_EDITED_DEVICE_STORAGE_LINK, editedFileDeviceUri);

        // Insert the content values into the database and get the position
        return Integer.valueOf(((context.getContentResolver().insert(Files.CONTENT_URI, cv)).getLastPathSegment()).toString());
    }

    /**
     * Method to determine what the mime type is for the provided file extension
     *
     * @param fileType provided file type
     * @param context  TODO: Likely remove soon
     * @return return the appropriate mime type
     */
    public static String getMimeType(String fileType, Context context) {
        Log.d("FileUtils[dMT]: ", "CHECK IT: " + fileType);
        if (fileType.matches("(?i).jpeg|.jpg|.jiff|.exif|.tiff|.gif|.bmp|.png|.webp|.bat|.bpg|.svg(?-i)")) {
            Log.d("Ehyo", "image/*");
            return context.getString(R.string.image_mime_type);
        } else if (fileType.matches("(?i).flv|.f4v|.f4p|.f4a|.f4b|.3gp|.3g2|.m4v|.svi|.mpg|.mpeg" +
                "|.m2v|.mpe|.mp2|.mpv|.amv|.asf|.mvb|.rm|.yuv|.wmv|.mov|.qt|.avi|.mng|.gifv|.ogg|.vob|.ogv" +
                "|.mkv|.webm|.mp3|.mp4(?-i)")) {
            Log.d("FileUtils[dMT]: ", "video/*");
            return context.getString(R.string.video_mime_type);
        } else {
            Log.d("FileUtils[dMT]", "Improper response");
            //TODO:  This shouldn't happen
            //            return context.getString(R.string.file_mime_type);
            return null;
        }
    }

    /**
     * Determines if the image type is representable by the Glide library
     *
     * @param fileType The file type being used in @{@link net.wetfish.wetfish.ui.viewpager.FileUploadFragment}
     * @return the mime type
     */
    public static boolean representableByGlide(String fileType) {
        Log.d("FileUtils[rBG]: ", "CHECK IT: " + fileType);
        if (fileType.matches("(?i).jpeg|.jpg|.jiff|.exif|.tiff|.gif|.bmp|.png|.webp|.bat|.bpg|.svg(?-i)")) {
            Log.d("FileUtils[rBG]: ", "image/*");
            return true;
        } else if (fileType.matches("(?i).flv|.f4v|.f4p|.f4a|.f4b|.3gp|.3g2|.m4v|.svi|.mpg|.mpeg" +
                "|.m2v|.mpe|.mp2|.mpv|.amv|.asf|.mvb|.rm|.yuv|.wmv|.mov|.qt|.avi|.mng|.gifv|.ogg|.vob|.ogv" +
                "|.mkv|.webm|.mp3|.mp4(?-i)")) {
            Log.d("FileUtils[rBG]", "video/*");
            return true;
        } else {
            Log.d("FileUtils[rBG]: ", "What");
            return false;
        }
    }

    /**
     * Create a scaled bitmap of the given image, reducing resolution by the scaleRatio while preserving
     * the image's native aspect ratio.
     *
     * @param bitmapToDownscale    Bitmap to downscale to populate downscaledBitmapFile
     * @param scaleRatio           Ratio as to which to downscale the bitmap provided
     * @param downscaledBitmapFile the downscaled bitmap image populating an image
     * @return downscaled image or regular image if downscaling has failed
     */
    public static boolean createDownscaledImageFile(Bitmap bitmapToDownscale, double scaleRatio, File downscaledBitmapFile) {
        // Height and width downscaled by the scaleRatio
        double destinationHeight = bitmapToDownscale.getHeight() * scaleRatio;
        double destinationWidth = bitmapToDownscale.getWidth() * scaleRatio;

        if (destinationWidth < MINIMUM_BITMAP_RESOLUTION) {
            destinationWidth = MINIMUM_BITMAP_RESOLUTION;
        }

        if (destinationHeight < MINIMUM_BITMAP_RESOLUTION) {
            destinationHeight = MINIMUM_BITMAP_RESOLUTION;
        }

        // Return a new bitmap that's a downscaled version of the provided bitmap
        Bitmap bitmap = Bitmap.createScaledBitmap(bitmapToDownscale, (int) destinationWidth, (int) destinationHeight, true);

        try {
            // Byte Array Output Stream
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // Compress to the desired format, JPEG, at full quality
            boolean successfulDownscale = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);

            if (successfulDownscale) {
                // File Output Stream for the downscaledBitmapFile
                FileOutputStream fileOutputStream = new FileOutputStream(downscaledBitmapFile);
                fileOutputStream.write(byteArrayOutputStream.toByteArray());

                // Close File Output Stream when finished
                fileOutputStream.close();

                // Creating file was successful
                return true;
            } else {
                //  Creating file failed
                return false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Creating file failed
        return false;
    }

    /**
     * Create a scaled bitmap of the given image, reducing resolution by the scaleRatio while preserving
     * the image's native aspect ratio.
     *
     * @param downscaledBitmapFile the downscaled bitmap image populating an image
     * @return downscaled image or regular image if downscaling has failed
     */
    public static boolean createOriginalScaledImageFile(Bitmap originalBitmap, File downscaledBitmapFile) {
        try {
            // Byte Array Output Stream
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // Compress to the desired format, JPEG, at full quality
            boolean successfulCopy = originalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);

            if (successfulCopy) {
                // File Output Stream for the downscaledBitmapFile
                FileOutputStream fileOutputStream = new FileOutputStream(downscaledBitmapFile);
                fileOutputStream.write(byteArrayOutputStream.toByteArray());

                // Close File Output Stream when finished
                fileOutputStream.close();

                // Creating file was successful
                return true;
            } else {
                //  Creating file failed
                return false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Creating file failed
        return false;
    }

    /**
     * Method created to determine that a bitmap has successfully been downscaled
     *
     * * @param originalBitmapUri
     * @param downscaledBitmapUri
     * @return
     */
    public static boolean checkSuccessfulBitmapDownscale(Uri originalBitmapUri, Uri downscaledBitmapUri) {
        Bitmap bitmapOriginal = BitmapFactory.decodeFile(originalBitmapUri.toString());
        Bitmap bitmapDownscaledOriginal = BitmapFactory.decodeFile(downscaledBitmapUri.toString());

        double originalWidth = bitmapOriginal.getWidth();
        double downscaledWidth = bitmapDownscaledOriginal.getWidth();

        return downscaledWidth < originalWidth;
    }

    /**
     * Method created to determine that a bitmap has successfully been upscaled
     *
     * * @param originalBitmapUri
     * @param downscaledBitmapUri
     * @return
     */
    public static boolean checkSuccessfulBitmapUpscale(Uri originalBitmapUri, Uri downscaledBitmapUri) {
        Bitmap bitmapOriginal = BitmapFactory.decodeFile(originalBitmapUri.toString());
        Bitmap bitmapDownscaledOriginal = BitmapFactory.decodeFile(downscaledBitmapUri.toString());

        double originalWidth = bitmapOriginal.getWidth();
        double upscaledWidth = bitmapDownscaledOriginal.getWidth();

        return upscaledWidth == originalWidth;
    }

    /**
     * Method created  to determine that a bitmap has successfully been duplicated
     *
     * @param originalBitmapUri
     * @param downscaledBitmapUri
     * @return
     */
    public static boolean checkSuccessfulBitmapDuplication(Uri originalBitmapUri, Uri downscaledBitmapUri) {
        Bitmap bitmapOriginal = BitmapFactory.decodeFile(originalBitmapUri.toString());
        Bitmap bitmapDownscaledOriginal = BitmapFactory.decodeFile(downscaledBitmapUri.toString());

        double originalWidth = bitmapOriginal.getWidth();
        double upscaledWidth = bitmapDownscaledOriginal.getWidth();

        return upscaledWidth == originalWidth;
    }

    /**
     * Acquires the current file's size and returns it as a string
     *
     * @param fileUri
     * @param context
     * @return
     */
    public static String getFileSize(Uri fileUri, Context context) {
        // Create an image to reference
        File file = new File(fileUri.toString());

        // float for image file length
        float fileSize = file.length();

        // Get file size in kilobytes
        fileSize = (float) (fileSize / UNIT_CONVERSION);

        // Return the gathered file size, rounded up to mb
        if (fileSize / UNIT_CONVERSION < 1) {
            // File Size is within the kilobyte range, return the appropriate string
            return context.getString(R.string.tv_file_sizes_kb, Math.round(fileSize));
        } else if (fileSize / UNIT_CONVERSION >= 1 && !(fileSize / UNIT_CONVERSION >= 1000)) {
            // File Size is within the megabyte range, convert to mb
            fileSize = (float) (fileSize / UNIT_CONVERSION);

            // Return the appropriate string
            return context.getString(R.string.tv_file_size_mb, Math.round(fileSize * ROUNDING_NUMBER) / ROUNDING_NUMBER);
        } else {
            // TODO: This shouldn't feasibly happen, but must be dealt with. This will be implemented when desired functionality is discussed for this edge case.
            // TODO: For now, just return the currently available file size
            return context.getString(R.string.tv_file_sizes_kb, Math.round(file.length()));
        }
    }

    /**
     * Acquires the current file's image resolution and returns it as a string
     *
     * @param fileUri
     * @param context
     * @return
     */
    public static String getImageResolution(Uri fileUri, Context context) {
        // Get the bitmap we want the resolution from
        Bitmap bitmap = BitmapFactory.decodeFile(fileUri.toString());

        // Grab the height and width and return it in the form a resolution if the image isn't null
        if (bitmap != null){
            return context.getString(R.string.tv_image_resolution, bitmap.getWidth(), bitmap.getHeight());
        } else {
            return context.getString(R.string.tv_image_resolution_not_found);
        }
    }

    /**
     * Acquires the current video's length and returns it as a string
     *
     * @param fileUri
     * @param context
     * @return
     */
    public static String getVideoLength(Uri fileUri, Context context) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        // file to be used
        retriever.setDataSource(context, fileUri);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        // Check to see if the time was obtained or not
        if (time != null) {
            long timeInMillisec = Long.parseLong(time);

            // Get the time in minutes, then get the time in seconds minus the total time in seconds converted to minutes to obtain seconds
            long minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillisec);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillisec) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillisec));


            if (seconds < 10) {
                return context.getString(R.string.tv_video_length_below_ten_seconds, minutes, seconds);
            } else {
                return context.getString(R.string.tv_video_length_above_ten_seconds, minutes, seconds);
            }
        } else {
            // Should the time be null reflect that in the return string
            return context.getString(R.string.tv_video_length_unobtained);
        }
    }

    /**
     *
     */
    public static boolean checkIfFileExists (String filePath) {
        // Create a file object and see if the given file exists
        File file = new File(filePath);
        if (file.exists()) {
            // If the file exists return true after closing the file
            return true;
        } else {
            // If the file doesn't exist return false after closing the file
            return false;
        }
    }
}
