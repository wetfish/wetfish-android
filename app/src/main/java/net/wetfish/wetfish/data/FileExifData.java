package net.wetfish.wetfish.data;

/**
 * Class that is created when EXIF data is found within General and/or Sensitive EXIF tags
 *
 * Created by ${Michael} on 4/29/2018.
 */
public class FileExifData {

    private String mExifDataKey;
    private String mExifDataValue;

    // Regular constructor
    public FileExifData (String exifDataTag, String exifDataValue) {
        mExifDataKey = exifDataTag;
        mExifDataValue = exifDataValue;
    }

    //  Cloning constructor
    public FileExifData (FileExifData  fileExifData) {
        mExifDataKey = fileExifData.getExifDataTag();
        mExifDataValue = fileExifData.getExifDataValue();
    }

    public String getExifDataTag() {
        return mExifDataKey;
    }

    public String getExifDataValue() {
        return mExifDataValue;
    }

    public void setExifDataValue(String mExifDataValue) {
        this.mExifDataValue = mExifDataValue;
    }
}
