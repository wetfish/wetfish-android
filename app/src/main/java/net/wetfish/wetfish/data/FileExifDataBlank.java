package net.wetfish.wetfish.data;

/**
 * Class that is created when no EXIF data is found within General and/or Sensitive EXIF tags
 *
 * Created by ${Michael} on 5/28/2018.
 */
public class FileExifDataBlank {


    private String mNoExifDataGeneral = "No General Exif Data Found";
    private String mNoExifDataSensitive = "No Sensitive Exif Data Found";
    private String mNoExifDataFound;

    public FileExifDataBlank (boolean isExifDataSensitive) {
        // If the exif data is sensitive, provide the correct sensitive sentence version, otherwise, general
        if (isExifDataSensitive) {
            mNoExifDataFound = mNoExifDataSensitive;
        } else {
            mNoExifDataFound = mNoExifDataGeneral;
        }

    }

    public String getNoExifDataFoundString() {
        return mNoExifDataFound;
    }
}