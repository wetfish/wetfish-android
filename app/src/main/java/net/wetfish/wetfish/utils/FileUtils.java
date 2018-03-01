package net.wetfish.wetfish.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ${Michael} on 11/4/2017.
 * <p>
 * Utility class for various methods relevant to handling files
 */

public class FileUtils {
    // TODO: Might want to rename this
    public static String getRealPathFromUri(Context context, Uri contentUri) {
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
                return cursor.getString(column_index);
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


    /**
     * Method to determine what the mime type is for the provided file extension
     *
     * @param fileType provided file type
     * @param context TODO: Likely remove soon
     * @return return the appropriate mime type
     */
    public static String determineMimeType(Context context, String fileType) {
        Log.d("FileUtils[dMT]: ", "CHECK IT: " + fileType);
        if (fileType.matches(".jpeg|.jpg|.jiff|.exif|.tiff|.gif|.bmp|.png|.webp|.bat|.bpg|.svg")) {
            Log.d("Ehyo", "image/*");
            return context.getString(R.string.image_mime_type);
        } else if (fileType.matches(".flv|.f4v|.f4p|.f4a|.f4b|.3gp|.3g2|.m4v|.svi|.mpg|.mpeg" +
                "|.m2v|.mpe|.mp2|.mpv|.amv|.asf|.mvb|.rm|.yuv|.wmv|.mov|.qt|.avi|.mng|.gifv|.ogg|.vob|.ogv" +
                "|.mkv|.webm|.mp3|.mp4")) {
            Log.d("FileUtils[dMT]: ", "video/*");
            return context.getString(R.string.video_mime_type);
        } else {
            Log.d("FileUtils[dMT]", "image/*, video/*");
            return context.getString(R.string.file_mime_type);
        }
    }

    public static boolean representableByGlide(String fileType) {
        Log.d("FileUtils[rBG]: ", "CHECK IT: " + fileType);
        if (fileType.matches(".jpeg|.jpg|.jiff|.exif|.tiff|.gif|.bmp|.png|.webp|.bat|.bpg|.svg")) {
            Log.d("FileUtils[rBG]: ", "image/*");
            return true;
        } else if (fileType.matches(".flv|.f4v|.f4p|.f4a|.f4b|.3gp|.3g2|.m4v|.svi|.mpg|.mpeg" +
                "|.m2v|.mpe|.mp2|.mpv|.amv|.asf|.mvb|.rm|.yuv|.wmv|.mov|.qt|.avi|.mng|.gifv|.ogg|.vob|.ogv" +
                "|.mkv|.webm|.mp3|.mp4")) {
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
            boolean successfulDownscale = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

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

    public double checkFileSize(Uri fileUri, Context context) {
        File imageFile = new File(fileUri.toString());
        double imageFileSize = imageFile.length();

        Uri fileUri2 = FileProvider.getUriForFile(context,
                context.getString(R.string.file_provider_authority),
                new File(fileUri.toString()));
        File imageFile2 = new File(fileUri2.toString());
        double imageFileSize2 = imageFile2.length();

        Log.d("This is in FileUtils", "\nImageFileSize: " + imageFileSize + "\nImageFileSize2: " + imageFileSize2);

        return 0.0;
    }
}
