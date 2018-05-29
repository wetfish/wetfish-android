package net.wetfish.wetfish.utils;

import android.content.Context;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;

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

    // Exif attributes for android version <=23
    private static final String[] EXIF_ATTRIBUTES_VERSION_23_BELOW_COMPLETE = new String[]{
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
            ExifInterface.TAG_WHITE_BALANCE,
            String.valueOf(ExifInterface.WHITEBALANCE_AUTO),
            String.valueOf(ExifInterface.WHITEBALANCE_MANUAL)
    };

    private static final String[] EXIF_ATTRIBUTES_VERSION_23_BELOW_SENSITIVE = new String[]{
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_GPS_ALTITUDE,
            ExifInterface.TAG_GPS_ALTITUDE_REF,
            ExifInterface.TAG_GPS_DATESTAMP,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_PROCESSING_METHOD,
            ExifInterface.TAG_GPS_TIMESTAMP,
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
    };

    private static final String[] EXIF_ATTRIBUTES_VERSION_23_BELOW_GENERAL = new String[]{
            ExifInterface.TAG_APERTURE,
            ExifInterface.TAG_EXPOSURE_TIME,
            ExifInterface.TAG_FLASH,
            ExifInterface.TAG_FOCAL_LENGTH,
            ExifInterface.TAG_IMAGE_LENGTH,
            ExifInterface.TAG_IMAGE_WIDTH,
            ExifInterface.TAG_ISO,
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.TAG_WHITE_BALANCE,
            String.valueOf(ExifInterface.WHITEBALANCE_AUTO),
            String.valueOf(ExifInterface.WHITEBALANCE_MANUAL)
    };

    // Exif attributes for android version >=24
    private static final String[] EXIF_ATTRIBUTES_VERSION_24_ABOVE_COMPLETE = new String[]{
            ExifInterface.TAG_APERTURE,
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
            ExifInterface.TAG_F_NUMBER,
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
            ExifInterface.TAG_ISO,
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

    // Exif attributes for android version >=24
    private static final String[] EXIF_ATTRIBUTES_VERSION_24_ABOVE_SENSITIVE = new String[]{
            ExifInterface.TAG_ARTIST,
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_DATETIME_DIGITIZED,
            ExifInterface.TAG_DATETIME_ORIGINAL,
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
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MAKER_NOTE,
            ExifInterface.TAG_MODEL,
    };

    // Exif attributes for android version >=24
    private static final String[] EXIF_ATTRIBUTES_VERSION_24_ABOVE_GENERAL = new String[]{
            ExifInterface.TAG_APERTURE,
            ExifInterface.TAG_APERTURE_VALUE,
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
            ExifInterface.TAG_F_NUMBER,
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
            ExifInterface.TAG_IMAGE_LENGTH,
            ExifInterface.TAG_IMAGE_UNIQUE_ID,
            ExifInterface.TAG_IMAGE_WIDTH,
            ExifInterface.TAG_INTEROPERABILITY_INDEX,
            ExifInterface.TAG_ISO,
            ExifInterface.TAG_ISO_SPEED_RATINGS,
            ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT,
            ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH,
            ExifInterface.TAG_LIGHT_SOURCE,
            ExifInterface.TAG_MAX_APERTURE_VALUE,
            ExifInterface.TAG_METERING_MODE,
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


    public static void transferExifData(Uri originalFile, Uri newFile) {
        try {
            // Check what the version of the SDK is to utilize the appropriate available ExifInterface attributes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                // Create an ExifInterface object to interact with the original file's exif data.
                ExifInterface originalFileExif = new ExifInterface(originalFile.toString());

                // Create an ExifInterface object to transfer the original file's exif data.
                ExifInterface newFileExif = new ExifInterface(newFile.toString());

                for (int i = 0; i < EXIF_ATTRIBUTES_VERSION_24_ABOVE_COMPLETE.length; i++) {
                    String exifAttributeValue = originalFileExif.getAttribute(EXIF_ATTRIBUTES_VERSION_24_ABOVE_COMPLETE[i]);
                    if (exifAttributeValue != null) {
                        newFileExif.setAttribute(EXIF_ATTRIBUTES_VERSION_24_ABOVE_COMPLETE[i], exifAttributeValue);
                    }
                }
                newFileExif.saveAttributes();
            } else {
                // Create an ExifInterface object to interact with the original file's exif data.
                ExifInterface originalFileExif = new ExifInterface(originalFile.toString());

                // Create an ExifInterface object to transfer the original file's exif data.
                ExifInterface newFileExif = new ExifInterface(newFile.toString());

                for (int i = 0; i < EXIF_ATTRIBUTES_VERSION_23_BELOW_COMPLETE.length; i++) {
                    String exifAttributeValue = originalFileExif.getAttribute(EXIF_ATTRIBUTES_VERSION_23_BELOW_COMPLETE[i]);
                    if (exifAttributeValue != null) {
                        newFileExif.setAttribute(EXIF_ATTRIBUTES_VERSION_23_BELOW_COMPLETE[i], exifAttributeValue);
                    }
                }
                newFileExif.saveAttributes();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to gather Exif data to populate the ExifDataAdapter
     *
     * @param originalFile
     * @param context
     * @return
     */
    public static ArrayList<Object> gatherExifData(Uri originalFile, Context context) {
        // Logging Tag
        final String LOG_TAG = ExifUtils.class.getSimpleName();

        // Array List of EXIF data objects
        ArrayList<Object> exifDataArrayList = new ArrayList<>();

        // boolean value to track if a header has been created
        boolean exifHeaderCreated = false;

        // Tracker to see how many tags were stored
        int exifValueTagPairsStored = 0;


        try {
            // Check what the version of the SDK is to utilize the appropriate available ExifInterface attributes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                // Create an ExifInterface object to interact with the original file's exif data.
                ExifInterface originalFileExif = new ExifInterface(originalFile.toString());

                // Gather necessary references to Exif Tag names
                String[] exifTagsV24AndAboveSensitive = context.getResources().getStringArray(R.array.exif_attributes_v24_and_above_sensitive_array);
                String[] exifTagsV24AndAboveGeneral = context.getResources().getStringArray(R.array.exif_attributes_v24_and_above_general_array);

                // Boolean to check if a header is needed and created;

                // Run through sensitive tags and store them if they exist
                for (int i = 0; i < EXIF_ATTRIBUTES_VERSION_24_ABOVE_SENSITIVE.length; i++) {

                    // Gather the attribute value at location i and store it if it is present
                    String exifAttributeValue = originalFileExif.getAttribute(EXIF_ATTRIBUTES_VERSION_24_ABOVE_SENSITIVE[i]);
                    if (exifAttributeValue != null) {

                        // Check to see if a sensitive header has been added
                        if (!(exifHeaderCreated)) {
                            exifDataArrayList.add(new FileExifDataHeader(true, context));
                            exifHeaderCreated = true;
                        }

                        // Add Exif data and increment
                        exifDataArrayList.add(new FileExifData(exifTagsV24AndAboveSensitive[i], exifAttributeValue));
                        exifValueTagPairsStored++;
                    }
                }

                // If no exif tags were stored, represent this
                if (exifValueTagPairsStored == 0) {
                    exifDataArrayList.add(new FileExifDataHeader(true, context));
                    exifDataArrayList.add(new FileExifDataBlank(true));
                }

                // Reset the header and value tag pairs added for general security level exif tags
                exifHeaderCreated = false;
                exifValueTagPairsStored = 0;

                // Run through general tags and store them
                for (int i = 0; i < EXIF_ATTRIBUTES_VERSION_24_ABOVE_GENERAL.length; i++) {

                    // Gather the attribute value at location i and store it if it is present
                    String exifAttributeValue = originalFileExif.getAttribute(EXIF_ATTRIBUTES_VERSION_24_ABOVE_GENERAL[i]);
                    if (exifAttributeValue != null) {

                        // Check to see if a general header has been added
                        if (!(exifHeaderCreated)) {
                            exifDataArrayList.add(new FileExifDataHeader(false, context));
                            exifHeaderCreated = true;
                        }

                        // Add Exif data and increment
                        exifDataArrayList.add(new FileExifData(exifTagsV24AndAboveGeneral[i], exifAttributeValue));
                        exifValueTagPairsStored++;
                    }
                }

                // If no exif tags were stored, represent this
                if (exifValueTagPairsStored == 0) {
                    exifDataArrayList.add(new FileExifDataHeader(false, context));
                    exifDataArrayList.add(new FileExifDataBlank(false));
                }

                return exifDataArrayList;

            } else {
                // Create an ExifInterface object to interact with the original file's exif data.
                ExifInterface originalFileExif = new ExifInterface(originalFile.toString());

                // Gather necessary references to Exif Tag names
                String[] exifTagsV23AndBelowSensitive = context.getResources().getStringArray(R.array.exif_attributes_v23_and_below_sensitive_array);
                String[] exifTagsV23AndBelowGeneral = context.getResources().getStringArray(R.array.exif_attributes_v23_and_below_general_array);

                // Run through sensitive tags and store them
                for (int i = 0; i < EXIF_ATTRIBUTES_VERSION_23_BELOW_SENSITIVE.length; i++) {

                    // Gather the attribute value at location i and store it if it is present
                    String exifAttributeValue = originalFileExif.getAttribute(EXIF_ATTRIBUTES_VERSION_23_BELOW_SENSITIVE[i]);
                    if (exifAttributeValue != null) {

                        // Check to see if a sensitive header has been added
                        if (!(exifHeaderCreated)) {
                            exifDataArrayList.add(new FileExifDataHeader(true, context));
                            exifHeaderCreated = true;
                        }

                        // Add Exif data and increment
                        exifDataArrayList.add(new FileExifData(exifTagsV23AndBelowSensitive[i], exifAttributeValue));
                        exifValueTagPairsStored++;
                    }
                }

                // If no exif tags were stored, represent this
                if (exifValueTagPairsStored == 0) {
                    exifDataArrayList.add(new FileExifDataHeader(true, context));
                    exifDataArrayList.add(new FileExifDataBlank(true));
                }

                // Reset the header and value tag pairs added for general security level exif tags
                exifHeaderCreated = false;
                exifValueTagPairsStored = 0;

                // Run through general tags and store them
                for (int i = 0; i < EXIF_ATTRIBUTES_VERSION_23_BELOW_GENERAL.length; i++) {

                    // Gather the attribute value at location i and store it if it is present
                    String exifAttributeValue = originalFileExif.getAttribute(EXIF_ATTRIBUTES_VERSION_23_BELOW_GENERAL[i]);
                    if (exifAttributeValue != null) {


                        // Check to see if a general header has been added
                        if (!(exifHeaderCreated)) {
                            exifDataArrayList.add(new FileExifDataHeader(false, context));
                            exifHeaderCreated = true;
                        }

                        // Add Exif data and increment
                        exifDataArrayList.add(new FileExifData(exifTagsV23AndBelowGeneral[i], exifAttributeValue));
                        exifValueTagPairsStored++;
                    }
                }

                // If no exif tags were stored, represent this
                if (exifValueTagPairsStored == 0) {
                    exifDataArrayList.add(new FileExifDataHeader(false, context));
                    exifDataArrayList.add(new FileExifDataBlank(false));
                }

                return exifDataArrayList;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    //TODO: Addition for next update
//    private ArrayList<Object> transferEditedExifData(ArrayList<Object> editedExifDataList, Uri newFile) {
//        ArrayList<Object> exifDataArrayList = new ArrayList<>();
//
//        try {
//            // Check what the version of the SDK is to utilize the appropriate available ExifInterface attributes
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//
//                // Create an ExifInterface object to interact with the original file's exif data.
//                ExifInterface originalFileExif = new ExifInterface(originalFile.toString());
//
//
//                // Run through sensitive tags and store them
//                for (int i = 0; i < EXIF_ATTRIBUTES_VERSION_24_ABOVE_SENSITIVE.length; i++) {
//                    String exifAttributeValue = originalFileExif.getAttribute(EXIF_ATTRIBUTES_VERSION_24_ABOVE_SENSITIVE[i]);
//                    if (exifAttributeValue != null) {
//                        newFileExif.setAttribute(EXIF_ATTRIBUTES_VERSION_24_ABOVE[i], exifAttributeValue);
//                    }
//                }
//
//                // Run through general tags and store them
//                newFileExif.saveAttributes();
//            } else {
//                // Create an ExifInterface object to interact with the original file's exif data.
//                ExifInterface originalFileExif = new ExifInterface(originalFile.toString());
//
//                // Create an ExifInterface object to transfer the original file's exif data.
//                ExifInterface newFileExif = new ExifInterface(newFile.toString());
//
//                for (int i = 0; i < EXIF_ATTRIBUTES_VERSION_23_BELOW.length; i++) {
//                    String exifAttributeValue = originalFileExif.getAttribute(EXIF_ATTRIBUTES_VERSION_23_BELOW[i]);
//                    if (exifAttributeValue != null) {
//                        newFileExif.setAttribute(EXIF_ATTRIBUTES_VERSION_23_BELOW[i], exifAttributeValue);
//                    }
//                }
//                newFileExif.saveAttributes();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
