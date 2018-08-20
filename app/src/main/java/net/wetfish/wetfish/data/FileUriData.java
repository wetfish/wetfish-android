package net.wetfish.wetfish.data;

/**
 * Class to transfer the uri of the given file and the type of file
 *
 * Created by ${Michael} on 8/20/2018.
 */
public class FileUriData {


    private String mFileTypeTag;
    private String mFileUri;
    private boolean mExifChanged;

    public FileUriData (String fileTypeTag, String fileUri, boolean exifChanged) {

        mFileTypeTag = fileTypeTag;
        mFileUri = fileUri;
        mExifChanged = exifChanged;
    }

    public String getFileTypeTag() {
        return mFileTypeTag;
    }

    public String getFileUri() {
        return mFileUri;
    }

    public boolean getExifChanged() {
        return mExifChanged;
    }
}
