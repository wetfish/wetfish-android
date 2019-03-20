package net.wetfish.wetfish.data;

import android.content.Context;

import net.wetfish.wetfish.R;

/**
 * Class that is created when EXIf data is found for either General or Sensitive EXIF tags
 *
 * Created by ${Michael} on 5/24/2018.
 */
public class FileExifDataHeader {

    private String mExifDataStringHeader;

    private boolean mIsExifDataSensitive;

    public FileExifDataHeader (Boolean isExifDataSensitive, Context context) {

        // Determine whether EXIF data is sensitive or not
        mIsExifDataSensitive = isExifDataSensitive;

        // Set the appropriate header title regarding the EXIF data type
        if (isExifDataSensitive) {
            mExifDataStringHeader = context.getString(R.string.exif_data_header_sensitive);
        } else {
            mExifDataStringHeader = context.getString(R.string.exif_data_header_general);
        }
    }

    public FileExifDataHeader (FileExifDataHeader fileExifDataHeader) {
        // Do nothing, this is merely a place holder
    }

    public String getExifDataStringHeader() {
        return mExifDataStringHeader;
    }

    public boolean getIsExifDataSensitive() {
        return mIsExifDataSensitive;
    }
}