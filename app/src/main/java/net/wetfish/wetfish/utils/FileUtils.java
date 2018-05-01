package net.wetfish.wetfish.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

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

    // TODO: Might want to rename this
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
                Log.d("FileUtils[gRPFU]: ", cursor.getString(column_index));

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
        cv.put(FileColumns.COLUMN_FILE_WETFISH_EDITED_FILE_STORAGE_LINK, editedFileDeviceUri);

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
     * @param view                 utilized to generate a snackbar for the appropriate layout
     * @return downscaled image or regular image if downscaling has failed
     */
    public static boolean createDownscaledImageFile(Bitmap bitmapToDownscale, double scaleRatio, File downscaledBitmapFile, View view) {
        // Height and width downscaled by the scaleRatio
        double destinationHeight = bitmapToDownscale.getHeight() * scaleRatio;
        double destinationWidth = bitmapToDownscale.getWidth() * scaleRatio;

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

                return true;
            } else {
                return false;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean checkSuccessfulBitmapDownscale(Uri originalBitmapUri, Uri downscaledBitmapUri) {
        Bitmap bitmapOriginal = BitmapFactory.decodeFile(originalBitmapUri.toString());
        Bitmap bitmapDownscaledOriginal = BitmapFactory.decodeFile(downscaledBitmapUri.toString());

        double originalWidth = bitmapOriginal.getWidth();
        double downscaledWidth = bitmapDownscaledOriginal.getWidth();

        return downscaledWidth < originalWidth;
    }

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
            return context.getString(R.string.tv_image_sizez_kb, Math.round(fileSize));
        } else if (fileSize / UNIT_CONVERSION >= 1 && !(fileSize / UNIT_CONVERSION >= 1000)) {
            // File Size is within the megabyte range, convert to mb
            fileSize = (float) (fileSize / UNIT_CONVERSION);

            // Return the appropriate string
            return context.getString(R.string.tv_file_size_mb, Math.round(fileSize * ROUNDING_NUMBER) / ROUNDING_NUMBER);
        } else {
            // TODO: This shouldn't feasibly happen, but must be dealt with. This will be implemented when desired functionality is discussed for this edge case.
        }
        return context.getString(R.string.tv_file_size_mb, Math.round(file.length()));

    }

    public static String getImageResolution(Uri fileUri, Context context) {
        // Get the bitmap we want the resolution from
        Bitmap bitmap = BitmapFactory.decodeFile(fileUri.toString());

        // Grab the height and width and return it in the form a resolution
        return context.getString(R.string.tv_image_resolution, bitmap.getWidth(), bitmap.getHeight());
    }

    public static String getVideoLength(Uri fileUri, Context context) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        // file to be used
        retriever.setDataSource(context, fileUri);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
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
    }

    public static void transferExifData(Uri originalFile, Uri newFile) {
        Log.d("LOG_TAG", "It ranAnnananananan~~~~~~~~~~~~~~~~~~~~");
        try {
            // Exif attributes for android version <=23
            String[] exifAttributesVersion23Below = new String[] {
                    ExifInterface.TAG_APERTURE,
                    ExifInterface.TAG_DATETIME,
                    ExifInterface.TAG_EXPOSURE_TIME,
                    ExifInterface.TAG_FLASH,
                    ExifInterface.TAG_FOCAL_LENGTH,
                    ExifInterface.TAG_GPS_ALTITUDE,
                    ExifInterface.TAG_GPS_ALTITUDE_REF,
                    ExifInterface.TAG_GPS_DATESTAMP,
                    ExifInterface.TAG_GPS_LATITUDE,
                    ExifInterface.TAG_GPS_LATITUDE_REF,
                    ExifInterface.TAG_GPS_LONGITUDE,
                    ExifInterface.TAG_GPS_LONGITUDE_REF,
                    ExifInterface.TAG_GPS_PROCESSING_METHOD,
                    ExifInterface.TAG_GPS_TIMESTAMP,
                    ExifInterface.TAG_IMAGE_LENGTH,
                    ExifInterface.TAG_IMAGE_WIDTH,
                    ExifInterface.TAG_ISO,
                    ExifInterface.TAG_MAKE,
                    ExifInterface.TAG_MODEL,
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
                    ExifInterface.TAG_SUBSEC_TIME_ORIG,
                    ExifInterface.TAG_WHITE_BALANCE,
                    String.valueOf(ExifInterface.WHITEBALANCE_AUTO),
                    String.valueOf(ExifInterface.WHITEBALANCE_MANUAL)
               };

            // Exif attributes for android version >=24
            String[] exifAttributesVersion24Above = new String[] {
                    ExifInterface.TAG_APERTURE_VALUE,
                    ExifInterface.TAG_ARTIST,
                    ExifInterface.TAG_BITS_PER_SAMPLE,
                    ExifInterface.TAG_BRIGHTNESS_VALUE,
                    ExifInterface.TAG_CFA_PATTERN,
                    ExifInterface.TAG_COLOR_SPACE,
                    ExifInterface.TAG_COMPONENTS_CONFIGURATION,
                    ExifInterface.TAG_COMPRESSED_BITS_PER_PIXEL,
                    ExifInterface.TAG_COMPRESSION,
                    ExifInterface.TAG_CONTRAST,
                    ExifInterface.TAG_COPYRIGHT,
                    ExifInterface.TAG_CUSTOM_RENDERED,
                    ExifInterface.TAG_DATETIME,
                    ExifInterface.TAG_DATETIME_DIGITIZED,
                    ExifInterface.TAG_DATETIME_ORIGINAL,
                    ExifInterface.TAG_DEFAULT_CROP_SIZE,
                    ExifInterface.TAG_DEVICE_SETTING_DESCRIPTION,
                    ExifInterface.TAG_DIGITAL_ZOOM_RATIO,
                    ExifInterface.TAG_DNG_VERSION,
                    ExifInterface.TAG_EXIF_VERSION,
                    ExifInterface.TAG_EXPOSURE_BIAS_VALUE,
                    ExifInterface.TAG_EXPOSURE_INDEX,
                    ExifInterface.TAG_EXPOSURE_MODE,
                    ExifInterface.TAG_EXPOSURE_PROGRAM,
                    ExifInterface.TAG_EXPOSURE_TIME,
                    ExifInterface.TAG_FILE_SOURCE,
                    ExifInterface.TAG_FLASH,
                    ExifInterface.TAG_FLASHPIX_VERSION,
                    ExifInterface.TAG_FLASH_ENERGY,
                    ExifInterface.TAG_FOCAL_LENGTH,
                    ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM,
                    ExifInterface.TAG_FOCAL_PLANE_RESOLUTION_UNIT,
                    ExifInterface.TAG_FOCAL_PLANE_X_RESOLUTION,
                    ExifInterface.TAG_FOCAL_PLANE_Y_RESOLUTION,
                    ExifInterface.TAG_GAIN_CONTROL,
                    ExifInterface.TAG_GPS_ALTITUDE,
                    ExifInterface.TAG_GPS_ALTITUDE_REF,
                    ExifInterface.TAG_GPS_AREA_INFORMATION,
                    ExifInterface.TAG_GPS_DATESTAMP,
                    ExifInterface.TAG_GPS_DEST_BEARING,
                    ExifInterface.TAG_GPS_DEST_BEARING_REF,
                    ExifInterface.TAG_GPS_DEST_DISTANCE,
                    ExifInterface.TAG_GPS_DEST_DISTANCE_REF,
                    ExifInterface.TAG_GPS_LATITUDE,
                    ExifInterface.TAG_GPS_LATITUDE_REF,
                    ExifInterface.TAG_GPS_LONGITUDE,
                    ExifInterface.TAG_GPS_LONGITUDE_REF,
                    ExifInterface.TAG_GPS_MAP_DATUM,
                    ExifInterface.TAG_GPS_MEASURE_MODE,
                    ExifInterface.TAG_GPS_PROCESSING_METHOD,
                    ExifInterface.TAG_GPS_SATELLITES,
                    ExifInterface.TAG_GPS_SPEED,
                    ExifInterface.TAG_GPS_SPEED_REF,
                    ExifInterface.TAG_GPS_STATUS,
                    ExifInterface.TAG_GPS_TIMESTAMP,
                    ExifInterface.TAG_GPS_TRACK,
                    ExifInterface.TAG_GPS_TRACK_REF,
                    ExifInterface.TAG_GPS_VERSION_ID,
                    ExifInterface.TAG_IMAGE_DESCRIPTION,
                    ExifInterface.TAG_IMAGE_LENGTH,
                    ExifInterface.TAG_IMAGE_UNIQUE_ID,
                    ExifInterface.TAG_IMAGE_WIDTH,
                    ExifInterface.TAG_INTEROPERABILITY_INDEX,
                    ExifInterface.TAG_ISO_SPEED_RATINGS,
                    ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT,
                    ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH,
                    ExifInterface.TAG_LIGHT_SOURCE,
                    ExifInterface.TAG_MAKE,
                    ExifInterface.TAG_MAKER_NOTE,
                    ExifInterface.TAG_MAX_APERTURE_VALUE,
                    ExifInterface.TAG_METERING_MODE,
                    ExifInterface.TAG_MODEL,
                    ExifInterface.TAG_NEW_SUBFILE_TYPE,
                    ExifInterface.TAG_OECF,
                    ExifInterface.TAG_ORF_ASPECT_FRAME,
                    ExifInterface.TAG_ORF_PREVIEW_IMAGE_LENGTH,
                    ExifInterface.TAG_ORF_PREVIEW_IMAGE_START,
                    ExifInterface.TAG_ORF_THUMBNAIL_IMAGE,
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.TAG_PHOTOMETRIC_INTERPRETATION,
                    ExifInterface.TAG_PIXEL_X_DIMENSION,
                    ExifInterface.TAG_PIXEL_Y_DIMENSION,
                    ExifInterface.TAG_PLANAR_CONFIGURATION,
                    ExifInterface.TAG_PRIMARY_CHROMATICITIES,
                    ExifInterface.TAG_REFERENCE_BLACK_WHITE,
                    ExifInterface.TAG_RELATED_SOUND_FILE,
                    ExifInterface.TAG_RESOLUTION_UNIT,
                    ExifInterface.TAG_ROWS_PER_STRIP,
                    ExifInterface.TAG_RW2_ISO,
                    ExifInterface.TAG_RW2_JPG_FROM_RAW,
                    ExifInterface.TAG_RW2_SENSOR_BOTTOM_BORDER,
                    ExifInterface.TAG_RW2_SENSOR_LEFT_BORDER,
                    ExifInterface.TAG_RW2_SENSOR_RIGHT_BORDER,
                    ExifInterface.TAG_RW2_SENSOR_TOP_BORDER,
                    ExifInterface.TAG_SAMPLES_PER_PIXEL,
                    ExifInterface.TAG_SATURATION,
                    ExifInterface.TAG_SCENE_CAPTURE_TYPE,
                    ExifInterface.TAG_SCENE_TYPE,
                    ExifInterface.TAG_SENSING_METHOD,
                    ExifInterface.TAG_SHARPNESS,
                    ExifInterface.TAG_SHUTTER_SPEED_VALUE,
                    ExifInterface.TAG_SOFTWARE,
                    ExifInterface.TAG_SPATIAL_FREQUENCY_RESPONSE,
                    ExifInterface.TAG_SPECTRAL_SENSITIVITY,
                    ExifInterface.TAG_STRIP_BYTE_COUNTS,
                    ExifInterface.TAG_STRIP_OFFSETS,
                    ExifInterface.TAG_SUBFILE_TYPE,
                    ExifInterface.TAG_SUBJECT_AREA,
                    ExifInterface.TAG_SUBJECT_DISTANCE,
                    ExifInterface.TAG_SUBJECT_DISTANCE_RANGE,
                    ExifInterface.TAG_SUBJECT_LOCATION,
                    ExifInterface.TAG_SUBSEC_TIME,
                    ExifInterface.TAG_SUBSEC_TIME_DIG,
                    ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
                    ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH,
                    ExifInterface.TAG_THUMBNAIL_IMAGE_WIDTH,
                    ExifInterface.TAG_TRANSFER_FUNCTION,
                    ExifInterface.TAG_USER_COMMENT,
                    ExifInterface.TAG_WHITE_BALANCE,
                    ExifInterface.TAG_WHITE_POINT,
                    ExifInterface.TAG_X_RESOLUTION,
                    ExifInterface.TAG_Y_CB_CR_COEFFICIENTS,
                    ExifInterface.TAG_Y_CB_CR_POSITIONING,
                    ExifInterface.TAG_Y_CB_CR_SUB_SAMPLING,
                    ExifInterface.TAG_Y_RESOLUTION,
                    String.valueOf(ExifInterface.WHITEBALANCE_AUTO),
                    String.valueOf(ExifInterface.WHITEBALANCE_MANUAL)
            };

            // Check what the version of the SDK is to utilize the appropriate available ExifInterface attributes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                // Create an ExifInterface object to interact with the original file's exif data.
                ExifInterface originalFileExif = new ExifInterface(originalFile.toString());

                // Create an ExifInterface object to transfer the original file's exif data.
                ExifInterface newFileExif = new ExifInterface(newFile.toString());

                for (int i = 0; i < exifAttributesVersion24Above.length; i++) {
                    String exifAttributeValue = originalFileExif.getAttribute(exifAttributesVersion24Above[i]);
                    if (exifAttributeValue != null) {
                        newFileExif.setAttribute(exifAttributesVersion24Above[i], exifAttributeValue);
                    }
                }
                newFileExif.saveAttributes();
            } else {
                // Create an ExifInterface object to interact with the original file's exif data.
                ExifInterface originalFileExif = new ExifInterface(originalFile.toString());

                // Create an ExifInterface object to transfer the original file's exif data.
                ExifInterface newFileExif = new ExifInterface(newFile.toString());

                for (int i = 0; i < exifAttributesVersion23Below.length; i++) {
                    String exifAttributeValue = originalFileExif.getAttribute(exifAttributesVersion23Below[i]);
                    if (exifAttributeValue != null) {
                        newFileExif.setAttribute(exifAttributesVersion23Below[i], exifAttributeValue);
                    }
                }
                newFileExif.saveAttributes();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeExif(Uri absolutePath) {
        try {
            ExifInterface exifInterface = new ExifInterface(absolutePath.toString());

            // Use exifInterface.setAttribute(ExifInterface.TAG_HERE,  String.valueOf(Whatever)

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
