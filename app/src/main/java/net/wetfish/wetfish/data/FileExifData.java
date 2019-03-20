package net.wetfish.wetfish.data;

/**
 * Class that is created when EXIF data is found within General and/or Sensitive EXIF tags
 *
 * Created by ${Michael} on 4/29/2018.
 */
public class FileExifData {

    private String mExifDataTag;
    private String mExifDataValue;
    private String mExifDataTagLayout;

    // Regular constructor
    public FileExifData (String exifDataTagLayout, String exifDataTag,  String exifDataValue) {
        mExifDataTag = exifDataTag;
        mExifDataTagLayout = exifDataTagLayout;
        mExifDataValue = exifDataValue;
    }

    //  Cloning constructor
    public FileExifData (FileExifData  fileExifData) {
        mExifDataTag = fileExifData.getExifDataTag();
        mExifDataTagLayout = fileExifData.getExifDataTagLayout();
        mExifDataValue = fileExifData.getExifDataValue();
    }

    public String getExifDataTag() {
        return mExifDataTag;
    }

    public String getExifDataValue() {
        return mExifDataValue;
    }

    public String getExifDataTagLayout() { return mExifDataTagLayout; }

    public void setExifDataValue(String mExifDataValue) {
        this.mExifDataValue = mExifDataValue;
    }
}