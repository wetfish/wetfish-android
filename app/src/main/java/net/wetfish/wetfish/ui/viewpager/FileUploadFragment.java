package net.wetfish.wetfish.ui.viewpager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.github.clans.fab.FloatingActionButton;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;

import net.wetfish.wetfish.R;
import net.wetfish.wetfish.retrofit.RESTInterface;
import net.wetfish.wetfish.retrofit.RetrofitClient;
import net.wetfish.wetfish.ui.GalleryActivity;
import net.wetfish.wetfish.ui.GalleryCollectionActivity;
import net.wetfish.wetfish.utils.FileUtils;
import net.wetfish.wetfish.utils.UIUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FileUploadFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FileUploadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileUploadFragment extends Fragment implements FABProgressListener,
        AdapterView.OnItemSelectedListener {

    /* Fragment initialization parameter keys */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_FILE_URI = "file_uri";

    /* Constants */
    private static final String LOG_TAG = FileUploadFragment.class.getSimpleName();
    private static final int REQUEST_STORAGE = 0;
    private static final String[] PERMISSIONS_STORAGE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int ORIGINAL_SIZE_SELECTION = 0;
    private static final int LARGE_SIZE_SELECTION = 1;
    private static final int MEDIUM_SIZE_SELECTION = 2;
    private static final int SMALL_SIZE_SELECTION = 3;
    private static final double[] SELECTIONRATIO = {1, .66, .44, .22};
    private int START_AT_MOST_RECENT_FIRST_INTEGER = 0;
    private static final String IMAGE_FILE = "image/*";
    private static final String VIDEO_FILE = "video/*";

    /* Views */
    private ImageView mFileView;
    private TextView mFileLength;
    private TextView mFileViewSize;
    private TextView mFileNotFoundView;
    private TextView mFileViewResolution;
    private EditText mFileEditTagsView;
    private EditText mFileEditTitleView;
    private EditText mFileEditDescriptionView;
    private FloatingActionButton fabUploadFile;
    private FABProgressCircle mFabProgressCircle;
    private View mRootLayout;
    private View fileUploadContent;
    private Spinner mSpinner;

    /* Data */
    private Uri mFileUriAbsolutePath;
    private Uri mDownscaledImageAbsolutePath;
    private String mFileType;
    private String mMimeType;
    private String responseViewURL;
    private String responseDeleteURL;
    private boolean responseURLAcquired;
    private boolean mDownscaledImageCreated = false;
    private boolean mDatabaseAdditionSuccessful = false;
    private boolean fileFound;
    private int uploadID;
    private int mCurrentSpinnerSelection = 0;
    private double mImageFileSize = 0;
    private int sectionNumber;


    //TODO: Potentially remove.
    private OnFragmentInteractionListener mListener;

    public FileUploadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param position The position of the fragment
     * @param fileUri  The Uri of the data passed to the fragment
     * @return A new instance of fragment FileUploadFragment.
     */
    public static FileUploadFragment newInstance(Integer position, Uri fileUri) {
        FileUploadFragment fragment = new FileUploadFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, position);
        args.putString(ARG_FILE_URI, fileUri.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            mFileUriAbsolutePath = Uri.parse(getArguments().getString(ARG_FILE_URI));
        }
    }

    //TODO: Later on when Video Playback is possible with exoplayer the focus feature will only be for images
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Determine the mime type
        mFileType = FileUtils.getFileExtensionFromUri(getContext(), mFileUriAbsolutePath);
        mMimeType = FileUtils.getMimeType(mFileType, getContext());

        // Inflate the proper layout depending on the mime type
        switch (mMimeType) {
            case IMAGE_FILE: // This layout is for image files
                // Inflate the layout for this fragment
                mRootLayout = inflater.inflate(R.layout.fragment_file_upload_image_view_pager, container, false);

                // Reference to file upload layout content
                fileUploadContent = mRootLayout.findViewById(R.id.file_upload_content_container);

                // File Views
                mFileView = mRootLayout.findViewById(R.id.iv_fragment_file_upload);
                mFileViewSize = mRootLayout.findViewById(R.id.tv_image_size);
                mFileViewResolution = mRootLayout.findViewById(R.id.tv_image_resolution);

                // Setup Spinner
                mSpinner = mRootLayout.findViewById(R.id.spinner_fragment_file_upload);

                // Array Adapter for Spinner
                @SuppressLint("ResourceType") ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                        R.array.upload_fragment_spinner_array, R.xml.custom_spinner_item);

                // Specific array adapter layout
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // Apply the adapter to the mSpinner
                mSpinner.setAdapter(spinnerAdapter);

                // Setup onItemSelectedListener
                mSpinner.setOnItemSelectedListener(this);


                break;
            case VIDEO_FILE: // This layout is for video files
                //TODO: This will support a separate root layout for videos specifically
                mRootLayout = inflater.inflate(R.layout.fragment_file_upload_video_view_pager, container, false);

                // Reference to file upload layout content
                fileUploadContent = mRootLayout.findViewById(R.id.file_upload_content_container);

                // File views
                mFileView = mRootLayout.findViewById(R.id.iv_fragment_file_upload);
                mFileLength = mRootLayout.findViewById(R.id.tv_video_length);
                mFileViewSize = mRootLayout.findViewById(R.id.tv_video_size);

                // Setup view data
                mFileViewSize.setText(FileUtils.getFileSize(mFileUriAbsolutePath, getContext()));
                mFileLength.setText(FileUtils.getVideoLength(mFileUriAbsolutePath, getContext()));

                break;
            default:
                //TODO: Potentially make an error page
                break;
        }

        // Views
        // TODO: Support Video Views soon. (Glide/VideoView/Exoplayer)
        mFileEditTitleView = mRootLayout.findViewById(R.id.et_title);
        mFileEditTagsView = mRootLayout.findViewById(R.id.et_tags);
        mFileEditDescriptionView = mRootLayout.findViewById(R.id.et_description);
        mFabProgressCircle = mRootLayout.findViewById(R.id.fab_progress_circle);
        mFileNotFoundView = mRootLayout.findViewById(R.id.tv_file_not_found);

        // Setup mFileView's image and onClickListener with the correct file Uri
        if (mDownscaledImageCreated) {
            determineFileViewContent(mDownscaledImageAbsolutePath);
        } else {
            determineFileViewContent(mFileUriAbsolutePath);
        }
        // Set a focus change listener to allow for focus to dictate the appearance of the keyboard
        View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
            /**
             * Called when the focus state of a view has changed.
             *
             * @param v        The view whose state has changed.
             * @param hasFocus The new focus state of v.
             */
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mFileView.setClickable(false);
                } else if (!hasFocus) {
                    mFileView.setClickable(true);
                    UIUtils.hideKeyboard(v, getContext());
                }
            }
        };

        mFileEditTitleView.setOnFocusChangeListener(focusChangeListener);
        mFileEditTagsView.setOnFocusChangeListener(focusChangeListener);
        mFileEditDescriptionView.setOnFocusChangeListener(focusChangeListener);

        // Setup listener for progress bar
        mFabProgressCircle.attachListener(this);

        // Fab to upload file to Wetfish server
        fabUploadFile = mRootLayout.findViewById(R.id.fab_upload_file);
        fabUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Verify that the permissions necessary to complete this action have been granted
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permissions have not been granted, inform the user and ask again
                    requestStoragePermission();

                } else {

                    // Storage permissions granted!
                    mFabProgressCircle.show();
                    fabUploadFile.setClickable(false);
                    if (mSpinner != null) {
                        mSpinner.setEnabled(false);
                    }
                    uploadFile(mFileUriAbsolutePath);
                }

            }
        });

        return mRootLayout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to {@link net.wetfish.wetfish.ui.GalleryUploadActivity#onStart() Activity.onStart} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStart() {
        super.onStart();
        responseURLAcquired = false;
    }

    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to {@link Fragment#onPause() Activity.onPause} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onPause() {
        super.onPause();
        //TODO: Potentially delete and recreate image on onPause()?
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!mDatabaseAdditionSuccessful && mDownscaledImageCreated) {
            Log.d(LOG_TAG, "Database addition wasn't successful, delete file");
            deleteDownscaledFile();
        } else {
            // Do Nothing
            Log.d(LOG_TAG, "Database addition was successful or file wasn't created");
        }
    }

    /**
     * Animation to depict the uploading process
     */
    @Override
    public void onFABProgressAnimationEnd() {
        Snackbar.make(mFabProgressCircle, getContext().getString(R.string.tv_cloud_upload_complete), Snackbar.LENGTH_SHORT)
                .show();
        // Create file detail activity intent
        Intent fileDetails = new Intent(getContext(), GalleryCollectionActivity.class);

        // Create artificial backstack to populate the intent
        Intent backStackIntent = new Intent(getContext(), GalleryActivity.class);
        Intent[] intents = {backStackIntent, fileDetails};

        // Clear the backstack to prevent erroneous behaviour
        fileDetails.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        backStackIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Pass the Uri to the corresponding gallery item
        fileDetails.putExtra(getString(R.string.file_details_key),
                FileUtils.getFileUri(uploadID));

        // Read the current preferences of the user to determine which integer to pass
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        boolean sortByMostRecentSetting = sharedPref.getBoolean(getContext().getString(R.string.pref_sortByMostRecent_key),
                getContext().getResources().getBoolean(R.bool.pref_sortByMostRecent_default_value));
        if (sortByMostRecentSetting) {
            // Sorting method is most recent so it will always be the first.
            fileDetails.putExtra(getString(R.string.file_position_key), START_AT_MOST_RECENT_FIRST_INTEGER);
        } else {
            // The file will be the last file since the sorting method is newest last
            fileDetails.putExtra(getString(R.string.file_position_key), uploadID);
        }

        // Start GalleryCollectionActivity with an artificial back stack
        getContext().startActivities(intents);
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE: {
                if (requestCode == REQUEST_STORAGE) {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // Storage permissions were granted. Upload file.
                        uploadFile(mFileUriAbsolutePath);
                    } else {
                        // Storage Permissions were not granted
                        Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content),
                                R.string.permission_not_granted_storage,
                                Snackbar.LENGTH_LONG).show();
                    }
                }
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
            }
        }
    }

    /**
     * <p>Callback method to be invoked when an item in this view has been
     * selected. This callback is invoked only when the newly selected
     * position is different from the previously selected position or if
     * there was no selected item.</p>
     * <p>
     * Impelmenters can call getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param parent   The AdapterView where the selection happened
     * @param view     The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id       The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        mCurrentSpinnerSelection = position;

        switch (position) {
            case ORIGINAL_SIZE_SELECTION:
                // Check to see if a file has been generated before this
                if (mDownscaledImageCreated) {
                    // Delete Previous File
                    deleteDownscaledFile();
                }

                // Setup the file stats
                setupFileStats();

                break;
            case LARGE_SIZE_SELECTION:
                // Check to see if a file has been generated before this
                if (mDownscaledImageCreated) {
                    // Delete Previous File
                    deleteDownscaledFile();
                }

                // Generate medium sized image (75%)) and setup mFileView accordingly
                createDownscaledFile(position);

                // Setup the file stats
                setupFileStats();

                break;
            case MEDIUM_SIZE_SELECTION:
                // Check to see if a file has been generated before this
                if (mDownscaledImageCreated) {
                    // Delete Previous File
                    deleteDownscaledFile();
                }

                // Generate medium sized image (50%)) and setup mFileView accordingly
                createDownscaledFile(position);

                // Setup the file stats
                setupFileStats();

                break;
            case SMALL_SIZE_SELECTION:
                // Check to see if a file has been generated before this
                if (mDownscaledImageCreated) {
                    deleteDownscaledFile();

                }

                // Generate small sized image (25%) and setup mFileView accordingly
                createDownscaledFile(position);

                // Setup the file stats
                setupFileStats();

                break;
            default:
                Log.d(LOG_TAG, "Y'never know!");
        }
    }

    /**
     * Sets up the given file's stats
     *
     */
    private void setupFileStats() {
        if (mDownscaledImageCreated) {
            mFileViewSize.setText(FileUtils.getFileSize(mDownscaledImageAbsolutePath, getContext()));
            mFileViewResolution.setText(FileUtils.getImageResolution(mDownscaledImageAbsolutePath, getContext()));
        } else {
            mFileViewSize.setText(FileUtils.getFileSize(mFileUriAbsolutePath, getContext()));
            mFileViewResolution.setText(FileUtils.getImageResolution(mFileUriAbsolutePath, getContext()));
        }
    }

    private void deleteDownscaledFile() {
        // Delete Previous File
        File file = new File(mDownscaledImageAbsolutePath.toString());

        String canonicalPath;

        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            canonicalPath = file.getAbsolutePath();
        }
        final Uri uri = MediaStore.Files.getContentUri("external");
        final int result = getContext().getContentResolver().delete(uri,
                MediaStore.Files.FileColumns.DATA + "=?", new String[]{canonicalPath});
        if (result == 0) {
            final String absolutePath = file.getAbsolutePath();
            if (!absolutePath.equals(canonicalPath)) {
                getContext().getContentResolver().delete(uri,
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{mDownscaledImageAbsolutePath.toString()});
            }
        }

        mDownscaledImageCreated = false;
    }

    /**
     * Callback method to be invoked when the selection disappears from this
     * view. The selection can disappear for instance when touch is activated
     * or when the adapter becomes empty.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothin'
    }

    /**
     * This method will determine what Uri to use in displaying mFileView's background and onClickListener
     *
     * @param desiredAbsoluteFilePath passed Uri for the desired file path to be used for mFileView
     */
    private void determineFileViewContent(final Uri desiredAbsoluteFilePath) {

        // Setup file interaction
        mFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to find proper app to open file
                Intent selectViewingApp = new Intent();
                selectViewingApp.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                selectViewingApp.setAction(Intent.ACTION_VIEW);

                // Use FileProvider to get an appropriate URI compatible with version Nougat+
                // File path and type from the given file
                String fileStorageLink = FileUtils.getAbsolutePathFromUri(getContext(), desiredAbsoluteFilePath);
                String fileType = FileUtils.getFileExtensionFromUri(getContext(), desiredAbsoluteFilePath);
                Log.d(LOG_TAG, "File Storage Link: " + fileStorageLink);
                Log.d(LOG_TAG, "File Type: " + fileType);
                Uri fileProviderUri = FileProvider.getUriForFile(getContext(),
                        getString(R.string.file_provider_authority),
                        new File(fileStorageLink));

                // Setup the data and type
                // Appropriately determine mime type for the file
                selectViewingApp.setDataAndType(fileProviderUri, FileUtils.getMimeType(fileType, getContext()));

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    selectViewingApp.setClipData(ClipData.newRawUri("", fileProviderUri));
                    selectViewingApp.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                Log.d(LOG_TAG, "Quack: " + fileProviderUri.toString());
                startActivity(selectViewingApp);
            }
        });

        // Find out if the file is null
        if (desiredAbsoluteFilePath != null && !(desiredAbsoluteFilePath.toString().isEmpty())) {
            if (desiredAbsoluteFilePath != null) {
                // File was found
                mFileNotFoundView.setVisibility(View.GONE);

                // Setup view data
                // Check to see if the view is representable by glide
                if (FileUtils.representableByGlide(FileUtils.getFileExtensionFromUri(getContext(), desiredAbsoluteFilePath))) {
                    Glide.with(this)
                            .load(FileProvider.getUriForFile(getContext(),
                                    getString(R.string.file_provider_authority),
                                    new File(desiredAbsoluteFilePath.toString())))
                            .apply(RequestOptions.fitCenterTransform())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(mFileView);
                } else {
                    // If not, let the user know
                    Log.d(LOG_TAG, "Welp, something still went wrong!");
                }
            } else {
                UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
                        "File location was not found", Snackbar.LENGTH_LONG);
            }
        } else {
            UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
                    "Unable to obtain chosen file", Snackbar.LENGTH_LONG);

            // Make upload file inaccessible and inform the user.
            fabUploadFile.setVisibility(View.GONE);
            mFileNotFoundView.setVisibility(View.VISIBLE);
        }
    }

    //TODO: Do this during a loader and during the loader hide the upload button.

    /**
     * This method will create a downscaled bitmap  image utilizing FileUtils createDownscaledImageFile,
     * and upon success, accordingly change mFileView's onClickListener and displayed image. Upon failure
     * Snackbars will be shown.
     *
     * @param downscaleRatioSelected a passed value determined on the onClick
     */
    private void createDownscaledFile(int downscaleRatioSelected) {
        // Create a bitmap of the original file
        Bitmap bitmap = BitmapFactory.decodeFile(mFileUriAbsolutePath.toString());

        // Create the file that the result will populate
        File imageFile = null;

        try {
            // Create the file name that we'd like to use and populate
            imageFile = createImageFile();
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error occurred while creating the file: " + e);
            e.printStackTrace();
        }

        if (imageFile != null) {

            // Create a downscaled bitmap of the original file
            boolean downscaledFileCreated = FileUtils.createDownscaledImageFile(bitmap, SELECTIONRATIO[downscaleRatioSelected],
                    imageFile, mRootLayout.findViewById(R.id.gallery_detail_content));

            // Check to see if the file is actually downscaled
            if (downscaledFileCreated) {
                mDownscaledImageCreated = FileUtils.checkSuccessfulBitmapDownscale(mFileUriAbsolutePath,
                        mDownscaledImageAbsolutePath);
                if (mDownscaledImageCreated) {
                    // Change the file to be opened accordingly
                    determineFileViewContent(mDownscaledImageAbsolutePath);

                    // Write the appropriate EXIF data to the image
                    FileUtils.transferExifData(mFileUriAbsolutePath, mDownscaledImageAbsolutePath);

                    // Let the user know the image was successfully downscaled
                    Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content),
                            R.string.sb_image_successfully_downscaled, Snackbar.LENGTH_LONG).show();
                }
            } else {
                // Let the user know the image was unsuccessfully downscaled
                Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content),
                        R.string.sb_image_unsuccessfully_downscaled, Snackbar.LENGTH_LONG).show();
            }
        } else {
            // Let the user know the image was unsuccessfully downscaled
            Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content),
                    R.string.sb_image_unsuccessfully_downscaled, Snackbar.LENGTH_LONG).show();
        }
        // Generate large sized image (75%)
    }

    /**
     * Creates an image file with a given name at the location
     *
     * @return returns the generated file created at the desired location
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create a unique image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = getString(R.string.image_file_start) + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        storageDir.mkdir();
        File image = File.createTempFile(
                imageFileName,
                getString(R.string.image_file_extension),
                storageDir
        );

        // File path
        mDownscaledImageAbsolutePath = Uri.parse(image.getAbsolutePath());

        return image;
    }

    /**
     * Send a broadcast to the media scanner to add this photo
     */
    private void sendMediaBroadcast(String imagePath) {
        // Create intent to put in the broadcast
        Intent imageMediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        // Create an object of the file we want to broadcast to obtain the Uri
        File imageFile = new File(imagePath);
        Uri imageContentUri = Uri.fromFile(imageFile);

        //TODO: Delete later
        Log.d(LOG_TAG, "mDownscaledImageAbsolutePath: " + imagePath);
        Log.d(LOG_TAG, "imageContentUri: " + imageContentUri.toString());

        // Set the data for the intent and broadcast it
        imageMediaScanIntent.setData(imageContentUri);
        getContext().sendBroadcast(imageMediaScanIntent);
    }

    /**
     * Currently a backup method for storage permission access.
     * Can likely be deleted soon.
     * TODO: Check this out later
     */
    private void requestStoragePermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

            // If the user has previously denied granting the permission, offer the rationale
            Snackbar.make(mRootLayout, R.string.sb_permission_storage_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestPermissions(PERMISSIONS_STORAGE, REQUEST_STORAGE);
                        }
                    }).show();
        } else {
            // No explanation needed, request permission
            requestPermissions(PERMISSIONS_STORAGE, REQUEST_STORAGE);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    private void uploadFile(Uri fileUriAbsolutePath) {

        // Create Retrofit Instance
        Retrofit retrofit = RetrofitClient.getClient(getString(R.string.wetfish_base_url));

        // Create REST Interface
        RESTInterface restInterface = retrofit.create(RESTInterface.class);

        // Provide the correct image to Wetfish depending on the images currently available
        if (mDownscaledImageCreated) {
            // Should a downscaled image be present
            // Populate the file with the correct data to later pass to the  RequestBody instance
            File file = new File(mDownscaledImageAbsolutePath.toString());

            // Gather file extension from chosen file for database
            final String fileExtension = FileUtils.getFileExtensionFromUri(getContext(), mDownscaledImageAbsolutePath);

            // Gather file URI from chosen file for database.
            final String filePath = mDownscaledImageAbsolutePath.toString();

            Log.d(LOG_TAG, "mFileUriAbsolutePath: " + mDownscaledImageAbsolutePath);
            Log.d(LOG_TAG, "filePath: " + filePath);

            // Create RequestBody & MultipartBody to create a Call.
            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("Image", file.getName(), requestBody);
            Call<ResponseBody> call = restInterface.postFile(body);

            // Execute call request
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        // Get response body as a string
                        String onResponseString = response.body().string();
                        Log.d(LOG_TAG, "onResponse: " + onResponseString);

                        //  Downscaled image path
                        String downscaledFilePath;
                        if (mDownscaledImageCreated) {
                            downscaledFilePath = mDownscaledImageAbsolutePath.toString();
                        } else {
                            downscaledFilePath = "";
                        }

                        // If response body is not empty get returned URL
                        if (!(onResponseString.isEmpty())) {
                            Pattern pattern = Pattern.compile("url=(.*?)'>");
                            Matcher matcher = pattern.matcher(onResponseString);
                            if (matcher.find()) {
                                // Obtain the link given in response to the image
                                responseViewURL = getString(R.string.wetfish_base_url) + matcher.group(1);

                                responseDeleteURL = getContext().getString(R.string.not_implemented);

                                // Add to database
                                uploadID = FileUtils.insertFileData(getContext(),
                                        mFileEditTitleView.getText().toString(),
                                        mFileEditTagsView.getText().toString(),
                                        mFileEditDescriptionView.getText().toString(),
                                        Calendar.getInstance().getTimeInMillis(),
                                        fileExtension,
                                        filePath,
                                        responseViewURL,
                                        responseDeleteURL,
                                        downscaledFilePath);

                                /**
                                 *  Check to see if upload was successful to determine if the downscaled image
                                 * should be kept or deleted
                                 */
                                if (uploadID >= 0) {
                                    mDatabaseAdditionSuccessful = true;

                                    // Update media
                                    sendMediaBroadcast(downscaledFilePath);
                                } else {
                                    mDatabaseAdditionSuccessful = false;
                                }

                                Log.d(LOG_TAG, "onResponse: " + responseViewURL);
                                Log.d(LOG_TAG, "id: " + uploadID);

//                            UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
//                                    "File Uploaded!", Snackbar.LENGTH_LONG);
                                mFabProgressCircle.beginFinalAnimation();
                            } else {
                                responseViewURL = getString(R.string.wetfish_base_uploader_url);

                                responseDeleteURL = getContext().getString(R.string.not_implemented);

                                // Add to database
                                uploadID = FileUtils.insertFileData(getContext(),
                                        mFileEditTitleView.getText().toString(),
                                        mFileEditTagsView.getText().toString(),
                                        mFileEditDescriptionView.getText().toString(),
                                        Calendar.getInstance().getTimeInMillis(),
                                        fileExtension,
                                        filePath,
                                        responseViewURL,
                                        responseDeleteURL,
                                        downscaledFilePath);

                            /*
                               Check to see if upload was successful to determine if the downscaled image
                              should be kept or deleted
                             */
                                if (uploadID >= 0) {
                                    mDatabaseAdditionSuccessful = true;

                                    // Update media
                                    sendMediaBroadcast(downscaledFilePath);
                                } else {
                                    mDatabaseAdditionSuccessful = false;
                                }

                                Log.d(LOG_TAG, "onResponse: " + responseViewURL);
                                Log.d(LOG_TAG, "id: " + uploadID);

//                            UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
//                                    "File Uploaded!", Snackbar.LENGTH_LONG);
                                mFabProgressCircle.beginFinalAnimation();
                            }
                        } else {
                            responseViewURL = getString(R.string.wetfish_base_uploader_url);

                            responseDeleteURL = getContext().getString(R.string.not_implemented);

                            // Add to database
                            uploadID = FileUtils.insertFileData(getContext(),
                                    mFileEditTitleView.getText().toString(),
                                    mFileEditTagsView.getText().toString(),
                                    mFileEditDescriptionView.getText().toString(),
                                    Calendar.getInstance().getTimeInMillis(),
                                    fileExtension,
                                    filePath,
                                    responseViewURL,
                                    responseDeleteURL,
                                    downscaledFilePath);

                        /*
                           Check to see if upload was successful to determine if the downscaled image
                          should be kept or deleted
                         */
                            if (uploadID >= 0) {
                                mDatabaseAdditionSuccessful = true;

                                // Update media
                                sendMediaBroadcast(downscaledFilePath);
                            } else {
                                mDatabaseAdditionSuccessful = false;
                            }

                            Log.d(LOG_TAG, "onResponse: " + responseViewURL);
                            Log.d(LOG_TAG, "id: " + uploadID);

//                        UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
//                                "File Uploaded!", Snackbar.LENGTH_LONG);
                            mFabProgressCircle.beginFinalAnimation();
                        }
                    } catch (IOException e) {
                        Log.d(LOG_TAG, "onFailure Catch: ");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    fabUploadFile.setClickable(true);
                    mFabProgressCircle.hide();
                    UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
                            "File Upload Failed!", Snackbar.LENGTH_LONG);
                    Log.d(LOG_TAG, "onFailure Response: " + t);
                }
            });

        } else {
            // Should no downscaled image be present
            // Populate the file with the correct data to later pass to the  RequestBody instance
            File file = new File(fileUriAbsolutePath.toString());

            // Gather file extension from chosen file for database
            final String fileExtension = FileUtils.getFileExtensionFromUri(getContext(), fileUriAbsolutePath);

            // Gather file URI from chosen file for database.
            final String filePath = fileUriAbsolutePath.toString();

            Log.d(LOG_TAG, "mFileUriAbsolutePath: " + fileUriAbsolutePath);
            Log.d(LOG_TAG, "filePath: " + filePath);

            // Create RequestBody & MultipartBody to create a Call.
            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("Image", file.getName(), requestBody);
            Call<ResponseBody> call = restInterface.postFile(body);

            // Execute call request
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        // Get response body as a string
                        String onResponseString = response.body().string();
                        Log.d(LOG_TAG, "onResponse: " + onResponseString);

                        //  Downscaled image path
                        String downscaledFilePath;
                        if (mDownscaledImageCreated) {
                            downscaledFilePath = mDownscaledImageAbsolutePath.toString();
                        } else {
                            downscaledFilePath = "";
                        }

                        // If response body is not empty get returned URL
                        if (!(onResponseString.isEmpty())) {
                            Pattern pattern = Pattern.compile("url=(.*?)'>");
                            Matcher matcher = pattern.matcher(onResponseString);
                            if (matcher.find()) {
                                // Obtain the link given in response to the image
                                responseViewURL = getString(R.string.wetfish_base_url) + matcher.group(1);

                                responseDeleteURL = getContext().getString(R.string.not_implemented);

                                // Add to database
                                uploadID = FileUtils.insertFileData(getContext(),
                                        mFileEditTitleView.getText().toString(),
                                        mFileEditTagsView.getText().toString(),
                                        mFileEditDescriptionView.getText().toString(),
                                        Calendar.getInstance().getTimeInMillis(),
                                        fileExtension,
                                        filePath,
                                        responseViewURL,
                                        responseDeleteURL,
                                        downscaledFilePath);

                                /**
                                 *  Check to see if upload was successful to determine if the downscaled image
                                 * should be kept or deleted
                                 */
                                if (uploadID >= 0) {
                                    mDatabaseAdditionSuccessful = true;

                                    // Update media
                                    sendMediaBroadcast(downscaledFilePath);
                                } else {
                                    mDatabaseAdditionSuccessful = false;
                                }

                                Log.d(LOG_TAG, "onResponse: " + responseViewURL);
                                Log.d(LOG_TAG, "id: " + uploadID);

//                            UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
//                                    "File Uploaded!", Snackbar.LENGTH_LONG);
                                mFabProgressCircle.beginFinalAnimation();
                            } else {
                                responseViewURL = getString(R.string.wetfish_base_uploader_url);

                                responseDeleteURL = getContext().getString(R.string.not_implemented);

                                // Add to database
                                uploadID = FileUtils.insertFileData(getContext(),
                                        mFileEditTitleView.getText().toString(),
                                        mFileEditTagsView.getText().toString(),
                                        mFileEditDescriptionView.getText().toString(),
                                        Calendar.getInstance().getTimeInMillis(),
                                        fileExtension,
                                        filePath,
                                        responseViewURL,
                                        responseDeleteURL,
                                        downscaledFilePath);

                            /*
                               Check to see if upload was successful to determine if the downscaled image
                              should be kept or deleted
                             */
                                if (uploadID >= 0) {
                                    mDatabaseAdditionSuccessful = true;

                                    // Update media
                                    sendMediaBroadcast(downscaledFilePath);
                                } else {
                                    mDatabaseAdditionSuccessful = false;
                                }

                                Log.d(LOG_TAG, "onResponse: " + responseViewURL);
                                Log.d(LOG_TAG, "id: " + uploadID);

//                            UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
//                                    "File Uploaded!", Snackbar.LENGTH_LONG);
                                mFabProgressCircle.beginFinalAnimation();
                            }
                        } else {
                            responseViewURL = getString(R.string.wetfish_base_uploader_url);

                            responseDeleteURL = getContext().getString(R.string.not_implemented);

                            // Add to database
                            uploadID = FileUtils.insertFileData(getContext(),
                                    mFileEditTitleView.getText().toString(),
                                    mFileEditTagsView.getText().toString(),
                                    mFileEditDescriptionView.getText().toString(),
                                    Calendar.getInstance().getTimeInMillis(),
                                    fileExtension,
                                    filePath,
                                    responseViewURL,
                                    responseDeleteURL,
                                    downscaledFilePath);

                        /*
                           Check to see if upload was successful to determine if the downscaled image
                          should be kept or deleted
                         */
                            if (uploadID >= 0) {
                                mDatabaseAdditionSuccessful = true;

                                // Update media
                                sendMediaBroadcast(downscaledFilePath);
                            } else {
                                mDatabaseAdditionSuccessful = false;
                            }

                            Log.d(LOG_TAG, "onResponse: " + responseViewURL);
                            Log.d(LOG_TAG, "id: " + uploadID);

//                        UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
//                                "File Uploaded!", Snackbar.LENGTH_LONG);
                            mFabProgressCircle.beginFinalAnimation();
                        }
                    } catch (IOException e) {
                        Log.d(LOG_TAG, "onFailure Catch: ");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    fabUploadFile.setClickable(true);
                    mFabProgressCircle.hide();
                    UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
                            "File Upload Failed!", Snackbar.LENGTH_LONG);
                    Log.d(LOG_TAG, "onFailure Response: " + t);
                }
            });
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO:
        void onUploadFragmentInteraction(Uri uri);
    }
}
