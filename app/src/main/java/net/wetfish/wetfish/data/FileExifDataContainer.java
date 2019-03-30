package net.wetfish.wetfish.data;

import java.util.ArrayList;

/**
 * Created by ${Michael} on 5/24/2018.
 */
public class FileExifDataContainer {

    private ArrayList<Object> mFileExifDataList;

    public FileExifDataContainer () {

    }

    public FileExifDataContainer(ArrayList<Object> fileExifDataList) {
        mFileExifDataList = fileExifDataList;
    }

    public ArrayList<Object> getFileExifDataList() {
        return mFileExifDataList;
    }
}