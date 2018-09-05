package net.wetfish.wetfish.utils;

import android.content.Context;
import android.net.Uri;
import android.support.media.ExifInterface;
import android.util.Log;

import net.wetfish.wetfish.R;
import net.wetfish.wetfish.data.FileExifData;
import net.wetfish.wetfish.data.FileExifDataBlank;
import net.wetfish.wetfish.data.FileExifDataHeader;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ${Michael} on 5/24/2018.
 */
public class ExifUtils {

    public static boolean transferExifData(Uri originalFile, Uri newFile, Context context) {
        try {
            // Get the complete list of EXIF tags available to ExifInterface
            String[] exifTagsCompleteInterface = context.getResources().getStringArray(R.array.exif_tags_complete_array_interface);

            // Create an ExifInterface object to interact with the original file's exif data.
            ExifInterface originalFileExif = new ExifInterface(originalFile.toString());

            // Create an ExifInterface object to transfer the original file's exif data.
            ExifInterface newFileExif = new ExifInterface(newFile.toString());

            for (int i = 0; i < exifTagsCompleteInterface.length; i++) {
                String exifAttributeValue = originalFileExif.getAttribute(exifTagsCompleteInterface[i]);
                if (exifAttributeValue != null) {
                    newFileExif.setAttribute(exifTagsCompleteInterface[i], exifAttributeValue);
                }
            }
            newFileExif.saveAttributes();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method to gather Exif data to populate the ExifDataAdapter
     *
     * @param baseFile
     * @param context
     * @return
     */
    public static ArrayList<Object> gatherExifData(Uri baseFile, Context context) {
        // Logging Tag
        final String LOG_TAG = ExifUtils.class.getSimpleName();

        // Array List of EXIF data objects
        ArrayList<Object> exifDataArrayList = new ArrayList<>();

        // boolean value to track if a header has been created
        boolean exifHeaderCreated = false;

        // Tracker to see how many tags were stored
        int exifValueTagPairsStored = 0;

        try {
            // Create an ExifInterface object to interact with the original file's exif data.
            ExifInterface originalFileExif = new ExifInterface(baseFile.toString());

            // Gather necessary references to Exif Tag names
            String[] exifTagsSensitive = context.getResources().getStringArray(R.array.exif_tags_sensitive_array);
            String[] exifTagsGeneral = context.getResources().getStringArray(R.array.exif_tags_general_array);
            String[] exifTagsSensitiveInterface = context.getResources().getStringArray(R.array.exif_tags_sensitive_array_interface);
            String[] exifTagsGeneralInterface = context.getResources().getStringArray(R.array.exif_tags_general_array_interface);

            // Boolean to check if a header is needed and created;

            // Run through sensitive tags and store them if they exist
            for (int i = 0; i < exifTagsSensitiveInterface.length; i++) {

                // Gather the attribute value at location i and store it if it is present
//                    Log.d(LOG_TAG, "What is happening: " + originalFileExif.getAttribute(EXIF_ATTRIBUTES_VERSION_24_ABOVE_SENSITIVE[i]));
                String exifAttributeValue = originalFileExif.getAttribute(exifTagsSensitiveInterface[i]);
                if (exifAttributeValue != null) {

                    // Check to see if a sensitive header has been added
                    if (!(exifHeaderCreated)) {
                        exifDataArrayList.add(new FileExifDataHeader(true, context));
                        exifHeaderCreated = true;
                    }

                    // Add Exif data and increment
                    exifDataArrayList.add(new FileExifData(exifTagsSensitive[i], exifTagsSensitiveInterface[i], exifAttributeValue));
                    Log.d(LOG_TAG, "Value != Null (Secure): " + exifTagsSensitive[i] + " " + exifAttributeValue + " " + i);
                    exifValueTagPairsStored++;
                }
            }

            // If no exif tags were stored, represent this
            if (exifValueTagPairsStored == 0) {
                exifDataArrayList.add(new FileExifDataHeader(true, context));
                exifDataArrayList.add(new FileExifDataBlank(true));
            }

            Log.d(LOG_TAG, "EXIF VALUE TAGS STORED (Sensitive) " + exifValueTagPairsStored);

            // Reset the header and value tag pairs added for general security level exif tags
            exifHeaderCreated = false;
            exifValueTagPairsStored = 0;

            // Run through general tags and store them
            for (int i = 0; i < exifTagsGeneralInterface.length; i++) {

                // Gather the attribute value at location i and store it if it is present
//                    Log.d(LOG_TAG, "What is happening: " + originalFileExif.getAttribute(EXIF_ATTRIBUTES_VERSION_24_ABOVE_SENSITIVE[i]));
                String exifAttributeValue = originalFileExif.getAttribute(exifTagsGeneralInterface[i]);
                if (exifAttributeValue != null) {

                    // Check to see if a general header has been added
                    if (!(exifHeaderCreated)) {
                        exifDataArrayList.add(new FileExifDataHeader(false, context));
                        exifHeaderCreated = true;
                    }

                    // Add Exif data and increment
                    exifDataArrayList.add(new FileExifData(exifTagsGeneral[i], exifTagsGeneralInterface[i], exifAttributeValue));
                    Log.d(LOG_TAG, "Value != Null (General): " + exifTagsGeneral[i] + " " + exifAttributeValue);
                    exifValueTagPairsStored++;
                }
            }

            // If no exif tags were stored, represent this
            if (exifValueTagPairsStored == 0) {
                exifDataArrayList.add(new FileExifDataHeader(false, context));
                exifDataArrayList.add(new FileExifDataBlank(false));
            }

            Log.d(LOG_TAG, "EXIF VALUE TAGS STORED (Sensitive) " + exifValueTagPairsStored);

            return exifDataArrayList;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean transferEditedExifData(ArrayList<Object> editedFileExifDataList, Uri baseFile, Uri newFile) {
        // Logging Tag
        final String LOG_TAG = ExifUtils.class.getSimpleName();

        FileExifData fileExifData;

        boolean successfulExifEdit;

        try {
            // Create an ExifInterface object to interact with the original file's exif data.
            ExifInterface originalFileExif = new ExifInterface(baseFile.toString());

            // Create an ExifInterface object to transfer the original file's exif data.
            ExifInterface newFileExif = new ExifInterface(newFile.toString());

            for (int i = 0; i < editedFileExifDataList.size(); i++) {
                if (editedFileExifDataList.get(i) instanceof FileExifData) {
                    // Gather the @FileExifData object so we can add the value to the new file's EXIF
                    fileExifData = (FileExifData) editedFileExifDataList.get(i);
                    newFileExif.setAttribute(fileExifData.getExifDataTag(), originalFileExif.getAttribute(fileExifData.getExifDataTag()));

                    String exifAttributeValue = originalFileExif.getAttribute(fileExifData.getExifDataTag());
                    Log.d(LOG_TAG, "Transfer Edited Exif Data: " + fileExifData.getExifDataTag() + " " + exifAttributeValue + " :" + i);
                }
            }
            // Save the attributes to the new file
            newFileExif.saveAttributes();

            successfulExifEdit = true;
        } catch (IOException e) {
            e.printStackTrace();

            successfulExifEdit = false;
        }

        return successfulExifEdit;
    }
}
