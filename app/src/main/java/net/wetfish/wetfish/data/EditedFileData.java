package net.wetfish.wetfish.data;

import android.net.Uri;

/**
 * Class to transfer the uri of the given file and the type of file
 * <p>
 * Created by ${Michael} on 8/20/2018.
 */
public class EditedFileData {

    private Uri mEditedFileUri;
    private boolean mExifChanged = false;
    private double mRescaledImageQuality = 100;

    /**
     * Blank constructor
     */
    public EditedFileData() {
    }

    /**
     * Constructor
     *
     * @param editedFileUri
     * @param exifChanged
     * @param rescaledImageQuality
     */
    public EditedFileData(Uri editedFileUri, boolean exifChanged, int rescaledImageQuality) {
        mEditedFileUri = editedFileUri;
        mExifChanged = exifChanged;
        mRescaledImageQuality = rescaledImageQuality;
    }

    /* Getters */
    public Uri getEditedFileUri() {
        return mEditedFileUri;
    }
    public boolean getExifChanged() {
        return mExifChanged;
    }
    public double getRescaledImageQuality() {
        return mRescaledImageQuality;
    }

    /* Setters */
    public void setEditedFileUri(Uri mFileUri) {
        this.mEditedFileUri = mFileUri;
    }
    public void setExifChanged(boolean mExifChanged) {
        this.mExifChanged = mExifChanged;
    }
    public void setRescaledImageQuality(double mRescaledImageQuality) {
        this.mRescaledImageQuality = this.mRescaledImageQuality;
    }

}