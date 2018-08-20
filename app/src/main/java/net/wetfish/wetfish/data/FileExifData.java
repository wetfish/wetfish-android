package net.wetfish.wetfish.data;

/**
 * Class that is created when EXIF data is found within General and/or Sensitive EXIF tags
 *
 * Created by ${Michael} on 4/29/2018.
 */
public class FileExifData {

    private String mExifDataTag;
    private String mExifDataValue;

    public FileExifData (String exifDataTag, String exifDataValue) {

        mExifDataTag = exifDataTag;
        mExifDataValue = exifDataValue;
    }

    public String getExifDataTag() {
        return mExifDataTag;
    }

    public String getExifDataValue() {
        return mExifDataValue;
    }
}
