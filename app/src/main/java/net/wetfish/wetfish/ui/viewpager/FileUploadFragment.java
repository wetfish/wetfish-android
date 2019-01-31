package net.wetfish.wetfish.ui.viewpager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;

import net.wetfish.wetfish.R;
import net.wetfish.wetfish.data.EditedFileData;
import net.wetfish.wetfish.retrofit.RESTInterface;
import net.wetfish.wetfish.retrofit.RetrofitClient;
import net.wetfish.wetfish.ui.GalleryActivity;
import net.wetfish.wetfish.ui.GalleryCollectionActivity;
import net.wetfish.wetfish.ui.GalleryUploadActivity;
import net.wetfish.wetfish.utils.ExifUtils;
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

//import com.github.clans.fab.FloatingActionButton;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UploadFragmentUriUpdate} interface
 * to handle interaction events.
 * Use the {@link FileUploadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileUploadFragment extends Fragment implements FABProgressListener,
        AdapterView.OnItemSelectedListener {


    /* Constants */
    private static final String LOG_TAG = FileUploadFragment.class.getSimpleName();
    private static final String ARG_EDITED_FILE_URI = "section_number";
    private static final String ARG_ORIGINAL_FILE_URI = "file_uri";
    private static final int REQUEST_STORAGE = 0;
    private static final String[] PERMISSIONS_STORAGE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int ORIGINAL_SIZE_SELECTION = 0;
    private static final int LARGE_SIZE_SELECTION = 1;
    private static final int MEDIUM_SIZE_SELECTION = 2;
    private static final int SMALL_SIZE_SELECTION = 3;
    private static final double[] SELECTIONRATIO = {1, .66, .44, .22};
    private static final String IMAGE_FILE = "image/*";
    private static final String VIDEO_FILE = "video/*";
    private static final int NULL_INTEGER = 1;
    private static final int RESCALE_FAILED = 0;
    private int START_AT_MOST_RECENT_FIRST_INTEGER = 0;

    /* Views */
    private ImageView mFileView;
    private TextView mFileLength;
    private TextView mFileViewSize;
    private TextView mFileNotFoundView;
    private TextView mFileViewResolution;
    private EditText mFileEditTagsView;
    private EditText mFileEditTitleView;
    private EditText mFileEditDescriptionView;
    private FloatingActionButton mFabUploadFile;
    private FABProgressCircle mFabProgressCircleUpload;
    private ProgressBar mFileProcessingBar;
    private View mRootLayout;
    private View fileUploadContent;
    private Spinner mSpinner;
    private CustomTabLayout mTabLayout;
    private CustomLockingViewPager mViewpager;

    /* Data */
    // Temporary original file path variable
    private Uri mOriginalFileAbsolutePath;
    // Temporary file path variable for downscaled images. Allows EXIF data to be transferred to @mEditedFileAbsolutePath
    private Uri mRescaledImageAbsolutePath;
    // Final file path variable for all image edits.
    private Uri mEditedFileAbsolutePath;
    // Final file path variable for all temp files
    private Uri mEditedFileAbsolutePathTemp;
    // Final file path variable for uploading @mEditedFileAbsolutePath if it exists or @mOriginalFileAbsolutePath otherwise
    private Uri mUploadFileAbsolutePath;
    private String mFileType;
    private String mMimeType;
    private String responseViewURL;
    private String responseDeleteURL;
    private boolean mImageEdited = false;
    private boolean mCallCanceled = false;
    private boolean mEditedFileCreated = false;
    private boolean mRescaledImageCreated = false;
    private boolean mRescaledImageDownscaled = false;
    private boolean mDatabaseAdditionSuccessful = false;
    private boolean mImageCreationOccurring = false;
    private boolean mCancelableCallThreadUpload;
    private int uploadID;
    private int mCurrentSpinnerSelection = 0;
    private EditedFileData mEditedFileData;
    private double mImageFileSize = 0;

    /* Threads */
    // Thread to upload image to Wetfish
    private Handler mCallThreadUpload;
    // Thread to rescale images
    private Handler mCallThreadRescaleImage;
    // Thread to determine images
    private Handler mCallThreadDetermineImage;

    private Call<ResponseBody> mCall;

    /* Fragment Interaction Interfaces */
    private UploadFragmentUriUpdate mSendUri;
    private boolean mDuplicateImageCreated;

    public FileUploadFragment() {
        // Required empty public constructor
    }

    /**
     * Create an instance of {@link FileUploadFragment} with the original fle Uri and edited file Uri if present
     *
     * @param editedFileUri The Uri of files edited off of the original file
     * @param fileUri       The Uri of the the original upload file
     * @return A new instance of fragment FileUploadFragment.
     */
    public static FileUploadFragment newInstance(Uri editedFileUri, Uri fileUri) {
        FileUploadFragment fragment = new FileUploadFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EDITED_FILE_URI, editedFileUri.toString());
        args.putString(ARG_ORIGINAL_FILE_URI, fileUri.toString());
        fragment.setArguments(args);
        return fragment;
    }

    // TODO: This is now obsolete but will be used for image editing.
    /* Fragment interaction methods */
    public void receiveEditExifFragmentData(EditedFileData editedFileData) {
        mEditedFileData = editedFileData;
        mEditedFileAbsolutePath = editedFileData.getEditedFileUri();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEditedFileAbsolutePath = Uri.parse(getArguments().getString(ARG_EDITED_FILE_URI));
            mOriginalFileAbsolutePath = Uri.parse(getArguments().getString(ARG_ORIGINAL_FILE_URI));
            Log.d(LOG_TAG, mOriginalFileAbsolutePath.toString() + " " + mEditedFileAbsolutePath);
        }

        if (mEditedFileData == null) {
            mEditedFileData = new EditedFileData();
        }
    }

    // TODO: Fix the initial error with "File Not Found" even though it was
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Determine the mime type
        mFileType = FileUtils.getFileExtensionFromUri(getContext(), mOriginalFileAbsolutePath);
        mMimeType = FileUtils.getMimeType(mFileType, getContext());

        // Viewpager views
        mViewpager = getActivity().findViewById(R.id.vp_gallery_detail);
        mTabLayout = getActivity().findViewById(R.id.tl_gallery_detail);



        // Inflate the proper layout depending on the mime type
        switch (mMimeType) {
            case IMAGE_FILE: // This layout is for image files
                // Inflate the layout for this fragment
                mRootLayout = inflater.inflate(R.layout.fragment_file_upload_image_view_pager, container, false);

                Button mButton = mRootLayout.findViewById(R.id.button);
                mButton.setOnClickListener(new View.OnClickListener() {
                    /**
                     * Called when a view has been clicked.
                     *
                     * @param v The view that was clicked.
                     */
                    @Override
                    public void onClick(View v) {
                        Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content), getContext().getString(R.string.sb_cloud_upload_cancelled), Snackbar.LENGTH_SHORT)
                                .show();
                    }
                });

                // Reference to file upload layout content
                fileUploadContent = mRootLayout.findViewById(R.id.cl_file_upload_content_container);

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
                fileUploadContent = mRootLayout.findViewById(R.id.cl_file_upload_content_container);

                // File views
                mFileView = mRootLayout.findViewById(R.id.iv_fragment_file_upload);
                mFileLength = mRootLayout.findViewById(R.id.tv_video_length);
                mFileViewSize = mRootLayout.findViewById(R.id.tv_video_size);

                // Setup view data
                mFileViewSize.setText(FileUtils.getFileSize(mOriginalFileAbsolutePath, getContext()));
                mFileLength.setText(FileUtils.getVideoLength(mOriginalFileAbsolutePath, getContext()));

                // Remove unused tabs for now
                removeTabsForVideoFiles();

                break;
            default:
                //TODO: Potentially make an error page
                break;
        }


        // Views
        mFileEditTitleView = mRootLayout.findViewById(R.id.et_title);
        mFileEditTagsView = mRootLayout.findViewById(R.id.et_tags);
        mFileEditDescriptionView = mRootLayout.findViewById(R.id.et_description);
        mFabProgressCircleUpload = mRootLayout.findViewById(R.id.fab_progress_circle_upload);
        mFileNotFoundView = mRootLayout.findViewById(R.id.tv_file_not_found);
        mFileProcessingBar = mRootLayout.findViewById(R.id.pb_processing_file);

        // Show the process bar to indicate the beginning of the image loading
        mFileProcessingBar.setVisibility(View.VISIBLE);

        // Setup mFileView's image and onClickListener with the correct file Uri during app start
        mCallThreadDetermineImage = new Handler();
        mCallThreadDetermineImage.post(new Runnable() {
            @Override
            public void run() {

                if (mEditedFileCreated || (mEditedFileAbsolutePath != null && !mEditedFileAbsolutePath.toString().isEmpty())) {
                    // If @mEditedFileAbsolutePath has been created or provided by another fragment, use it
                    determineFileViewContent(mEditedFileAbsolutePath);

                    // Hide the process bar to indicate the end of the image loading
                    mFileProcessingBar.setVisibility(View.INVISIBLE);
                } else {
                    // If @mEditedFileAbsolutePath has not been created or provided by another fragment, use the original
                    determineFileViewContent(mOriginalFileAbsolutePath);

                    // Hide the process bar to indicate the end of the image loading
                    mFileProcessingBar.setVisibility(View.INVISIBLE);
                }
            }
        });

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
        mFabProgressCircleUpload.attachListener(this);

        //TODO: Check out why the snackbars aren't popping up??

        // Fab to upload file to Wetfish server
        mFabUploadFile = mRootLayout.findViewById(R.id.fab_upload_file);
        mFabUploadFile.setOnClickListener(new View.OnClickListener() {
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
                    // Thread to upload the file with a delay to allow easier cancellation
                    if (mCallThreadUpload == null) {
                        // Start the progress circle and change the image to depict the FAB's new functionality
                        mFabProgressCircleUpload.show();
                        mFabUploadFile.setImageResource(R.drawable.ic_cancel_white_24dp);

                        // Disable Viewpager swiping
                        mViewpager.setViewpagerSwitching(false);

                        // Get a reference to the mTabLayout's children views to disable tabs
                        ViewGroup viewGroup = (ViewGroup) mTabLayout.getChildAt(0);

                        // Determine the amount of tabs present
                        int tabsCount = viewGroup.getChildCount();

                        // Iterate through the tabs and enable them
                        for (int i = 0; i < tabsCount; i++) {
                            // Get the child view at position i
                            ViewGroup viewGroupTag = (ViewGroup) viewGroup.getChildAt(i);

                            // Disable the tab
                            viewGroupTag.setEnabled(false);
                        }

                        // Disable spinner during upload
                        if (mMimeType.equals(IMAGE_FILE)) {
                            mSpinner.setEnabled(false);
                        }

                        // Thread cancelled during image creation?
                        mImageCreationOccurring = false;

                        // Create separate thread to do network processing
                        mCallThreadUpload = new Handler();
                        mCallThreadUpload.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                // Placeholder for a check on successful exif transfer
                                boolean exifTransferSuccessful = false;

                                // Placeholder for a check on whether the file is ready for upload or not
                                boolean fileUploadReady = false;

                                try {
                                    // Edit the EXIF data of the image based on the user's preferences, be it the edited or original image
                                    if (mEditedFileAbsolutePath != null && !mEditedFileAbsolutePath.toString().isEmpty()) {
                                        // Check to see if file supports EXIF data
                                        if (mFileType.matches("(?i).jpeg|.jpg(?-i)")) {
                                            // If an edited image already exists, write the desired EXIF data to the image
                                            exifTransferSuccessful = ExifUtils.createEditedExifList(
                                                    ExifUtils.gatherExifData(mOriginalFileAbsolutePath, getContext()),
                                                    mOriginalFileAbsolutePath, mEditedFileAbsolutePath, getContext());
                                        } else {
                                            // File is a pre-created edited file with no EXIF data
                                            fileUploadReady = true;
                                        }
                                    } else {
                                        // Check to see the file type
                                        if (mMimeType.equals(IMAGE_FILE)) {
                                            // If it's an image that has EXIF data
                                            if (mFileType.matches("(?i).jpeg|.jpg|(?-i)")) {
                                                // Attempt to create an edited file that can house the edited EXIF data
                                                if (exifCreateEditedImageFile()) {
                                                    // Image is no longer being created on the handler thread
                                                    mImageCreationOccurring = false;

                                                    // Write the desired EXIF data to the image
                                                    exifTransferSuccessful = ExifUtils.createEditedExifList(
                                                            ExifUtils.gatherExifData(mOriginalFileAbsolutePath, getContext()),
                                                            mOriginalFileAbsolutePath, mEditedFileAbsolutePath, getContext());
                                                } else {
                                                    // Unsuccessful, image creation failed
                                                    Log.d(LOG_TAG, "Unsuccessful, image creation failed");
                                                    fileUploadReady = false;

                                                    // Image is no longer being created on the handler thread
                                                    mImageCreationOccurring = false;


                                                    mEditedFileCreated = false;
                                                }
                                            } else {
                                                // No need to generate a new image since EXIF isn't being transferred
                                                Log.d(LOG_TAG, "No need to generate a new image since EXIF isn't being transferred");
                                                fileUploadReady = true;

                                                mEditedFileCreated = false;
                                            }
                                        } else {
                                            // No need to generate a new image since the file isn't an image
                                            Log.d(LOG_TAG, "No need to generate a new image since the file isn't an image" +
                                                    "\n" + mFileType);
                                            fileUploadReady = true;

                                            mEditedFileCreated = false;
                                        }
                                    }
                                } finally {
                                    if (exifTransferSuccessful || mFileType.equals(VIDEO_FILE) || mEditedFileCreated || fileUploadReady) {
                                        Log.d(LOG_TAG, "Run If if");

                                        // Add the edited photo to the content provider if it exists
                                        if (mEditedFileCreated && mMimeType.equals(IMAGE_FILE)) {
                                            sendMediaBroadcast(mEditedFileAbsolutePath.toString());
                                        }

                                        // Upload the photo
                                        uploadFile();
                                    } else {
                                        // TODO: Later on delete files created solely for EXIF transfer and just upload the original
                                        // This will need to take into consideration  downscaling/image editing.

                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogThemeAppVersionSummary);

                                        builder.setMessage(R.string.ad_message_exif_transfer_failed)
                                                .setTitle(R.string.ad_title_return_home)
                                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // Add photo to a content provider
                                                        sendMediaBroadcast(mEditedFileAbsolutePath.toString());

                                                        // Try and remove the UserComment since something went wrong with the EXIF Transfer
                                                        ExifUtils.removeWetfishTagFromEXIF(mEditedFileAbsolutePath, getContext());

                                                        // User decided to return to the home screen
                                                        uploadFile();
                                                    }
                                                })
                                                .setNegativeButton(R.string.ad_cancel, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // Reset the UI and end the handler thread

                                                        // Enable Viewpager swiping
                                                        mViewpager.setViewpagerSwitching(false);

                                                        // Get a reference to the mTabLayout's children views t------------o enable tabs
                                                        ViewGroup viewGroup = (ViewGroup) mTabLayout.getChildAt(0);

                                                        // Determine the amount of tabs present
                                                        int tabsCount = viewGroup.getChildCount();

                                                        // Iterate through the tabs and enable them
                                                        for (int i = 0; i < tabsCount; i++) {
                                                            // Get the child view at position i
                                                            ViewGroup viewGroupTag = (ViewGroup) viewGroup.getChildAt(i);

                                                            // Enable the tab
                                                            viewGroupTag.setEnabled(true);
                                                        }

                                                        // Reset the FAB and hide the upload progress bar
                                                        mFabProgressCircleUpload.hide();
                                                        mFabUploadFile.setImageResource(R.drawable.ic_upload_file_white_24dp);
                                                        if (mMimeType.equals(IMAGE_FILE)) {
                                                            mSpinner.setEnabled(true);
                                                        }

                                                        // Pass the user a success notification of cancellation
                                                        Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content), getContext().getString(R.string.sb_cloud_upload_cancelled), Snackbar.LENGTH_SHORT)
                                                                .show();

                                                        // Remove callback and return thread back to normal
                                                        mCallThreadUpload.removeCallbacksAndMessages(null);
                                                        mCallThreadUpload = null;
                                                    }
                                                });
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                }
                            }
                        }, 2000 /* 2 second delay */);

                    } else {
                        if (mCallThreadUpload != null) {
                            // If mCall has been instantiated, cancel it
                            if (mCall != null) {
                                // Notate that the call was cancelled for the appropriate snackbar message
                                mCallCanceled = true;

                                // Cancel mCall
                                mCall.cancel();
                            } else {
                                // Pass the user a success notification of cancellation
                                Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content), getContext().getString(R.string.sb_cloud_upload_cancelled), Snackbar.LENGTH_SHORT)
                                        .show();
                            }

                            // Enable Viewpager swiping
                            mViewpager.setViewpagerSwitching(false);


                            // Get a reference to the mTabLayout's children views to enable tabs
                            ViewGroup viewGroup = (ViewGroup) mTabLayout.getChildAt(0);

                            // Determine the amount of tabs present
                            int tabsCount = viewGroup.getChildCount();

                            // Iterate through the tabs and enable them
                            for (int i = 0; i < tabsCount; i++) {
                                // Get the child view at position i
                                ViewGroup viewGroupTag = (ViewGroup) viewGroup.getChildAt(i);

                                // Enable the tab
                                viewGroupTag.setEnabled(true);
                            }

                            // Reset the FAB and hide the upload progress bar
                            mFabProgressCircleUpload.hide();
                            mFabUploadFile.setImageResource(R.drawable.ic_upload_file_white_24dp);
                            if (mMimeType.equals(IMAGE_FILE)) {
                                mSpinner.setEnabled(true);
                            }

                            if (mImageCreationOccurring) {
                                // Remove callback and return thread back to normal
                                mCallThreadUpload.removeCallbacksAndMessages(null);
                                mCallThreadUpload = null;

                                // Delete the file that is currently being created
                                deleteEditedFile();
                            } else {
                                // Remove callback and return thread back to normal
                                mCallThreadUpload.removeCallbacksAndMessages(null);
                                mCallThreadUpload = null;
                            }
                        }
                    }
                }
            }
        });

        return mRootLayout;
    }

    //TODO: Later on when Video Playback is possible with exoplayer the focus feature will only be for images

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mSendUri = (UploadFragmentUriUpdate) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Error in data retrieval");
        }
//        if (context instanceof UploadFragmentUriUpdate) {
//            mSendUri = (UploadFragmentUriUpdate) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement UploadFragmentUriUpdate");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSendUri = null;
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to {@link net.wetfish.wetfish.ui.GalleryUploadActivity#onStart() Activity.onStart} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStart() {
        Log.d(LOG_TAG, "onStart: FileUploadFragment");
        super.onStart();
    }

    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to {@link Fragment#onPause() Activity.onPause} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onPause() {
        Log.d(LOG_TAG, "onPause: FileUploadFragment");
        super.onPause();
        //TODO: Potentially delete and recreate image on onPause()?
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {@link Activity#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume: FileUploadFragment");

        // Check to see if mEditedFileData has an edited file Uri
        if (mEditedFileData != null && mEditedFileData.getEditedFileUri() != null && !mEditedFileData.getEditedFileUri().toString().isEmpty()) {
            mEditedFileAbsolutePath = mEditedFileData.getEditedFileUri();
            mEditedFileCreated = true;
        }

        // Setup mFileView's image and onClickListener with the correct file Uri
        mCallThreadDetermineImage = new Handler();
        mCallThreadDetermineImage.post(new Runnable() {
            @Override
            public void run() {
                if (mEditedFileCreated || (mEditedFileAbsolutePath != null && !mEditedFileAbsolutePath.toString().isEmpty())) {
                    Log.d(LOG_TAG, "Edited Image Triggered");

                    // If @mEditedFileAbsolutePath has been created or provided by another fragment, use it.
                    determineFileViewContent(mEditedFileAbsolutePath);
                } else {
                    determineFileViewContent(mOriginalFileAbsolutePath);
                }
            }
        });

        // Check to see if this is a video file. If so, remove all unnecessary tabs.
        if (mMimeType.equals(VIDEO_FILE)) {
            removeTabsForVideoFiles();
        }
    }

    // TODO: This call me be the one causing additional lagging on the UI thread...

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy: FileUploadFragment");
        super.onDestroy();
        if (!mDatabaseAdditionSuccessful) {

            /* This is important if the application quits before the transaction between
               @mRescaledImageAbsolutePath and @mEditedFileAbsolutePath occurs */
            if (mRescaledImageCreated) {
                deleteRescaledFile();
            }
            if (mEditedFileCreated) {
                deleteEditedFile();
            }

            Log.d(LOG_TAG, "Database addition wasn't successful, delete file");

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
        Snackbar.make(mFabProgressCircleUpload, getContext().getString(R.string.tv_cloud_upload_complete), Snackbar.LENGTH_SHORT)
                .show();
        // Create file detail activity intent
        Intent fileDetails = new Intent(getContext(), GalleryCollectionActivity.class);

        // Create artificial backstack to populate the intent
        Intent backStackIntent = new Intent(getContext(), GalleryActivity.class);
        Intent[] intents = {backStackIntent, fileDetails};

        // Reset the threading being able to be cancelled
        mCancelableCallThreadUpload = false;

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
                        uploadFile();
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
    public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {

        Log.d(LOG_TAG, "Current Spinner Selection: " + position);
        mCurrentSpinnerSelection = position;

        switch (position) {
            case ORIGINAL_SIZE_SELECTION:
                // Only if the user has EXIF changes to save when selecting original resolution refer back to original image
                // Check to see if a file has been generated before this
                if (mEditedFileCreated) {
                    // Show the progress bar
                    mFileProcessingBar.setVisibility(View.VISIBLE);

                    // Hide the image view
                    mFileView.setVisibility(View.INVISIBLE);

                    // Disable the spinner while the thread processes the request
                    mSpinner.setEnabled(false);

                    mCallThreadRescaleImage = new Handler();
                    mCallThreadRescaleImage.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // Create a rescaled image
                                createRescaledImage(mCurrentSpinnerSelection);
                            } finally {
                                // Update the view field with the newly rescaled image if successfully rescaled
                                if (mEditedFileAbsolutePath != null) {
                                    determineFileViewContent(mEditedFileAbsolutePath);
                                } else {
                                    mSpinner.setSelection(RESCALE_FAILED);
                                    determineFileViewContent(mOriginalFileAbsolutePath);
                                }
                            }
                        }
                    }, 0 /* No delay for file deletion */);
                }

                // If handler is broken or doesn't instantiate re-enable spinner
                if (mCallThreadRescaleImage == null) {
                    // Hide the progress bar
                    mFileProcessingBar.setVisibility(View.GONE);

                    // Show the image view
                    mFileView.setVisibility(View.VISIBLE);

                    // Disable the spinner while the thread processes the request
                    mSpinner.setEnabled(true);
                }

                break;
            case LARGE_SIZE_SELECTION:
                // Show the progress bar
                mFileProcessingBar.setVisibility(View.VISIBLE);

                // Hide the image view
                mFileView.setVisibility(View.INVISIBLE);

                // Disable the spinner while the thread processes the request
                mSpinner.setEnabled(false);

                // Generate medium sized image (75%)) and setup mFileView accordingly
                mCallThreadRescaleImage = new Handler();
                mCallThreadRescaleImage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Create a rescaled image
                            createRescaledImage(mCurrentSpinnerSelection);
                        } finally {
                            // Update the view field with the newly rescaled image if successfully rescaled
                            if (mEditedFileAbsolutePath != null) {
                                determineFileViewContent(mEditedFileAbsolutePath);
                            } else {
                                mSpinner.setSelection(RESCALE_FAILED);
                                determineFileViewContent(mOriginalFileAbsolutePath);
                            }
                        }
                    }
                }, 0 /* No delay for file deletion*/);

                // If handler is broken or doesn't instantiate re-enable spinner
                if (mCallThreadRescaleImage == null) {
                    // Hide the progress bar
                    mFileProcessingBar.setVisibility(View.GONE);

                    // Show the image view
                    mFileView.setVisibility(View.VISIBLE);

                    // Disable the spinner while the thread processes the request
                    mSpinner.setEnabled(true);

                }


                break;
            case MEDIUM_SIZE_SELECTION:
                // Show the progress bar
                mFileProcessingBar.setVisibility(View.VISIBLE);

                // Hide the image view
                mFileView.setVisibility(View.INVISIBLE);

                // Disable the spinner while the thread processes the request
                mSpinner.setEnabled(false);

                // Generate medium sized image (75%)) and setup mFileView accordingly
                mCallThreadRescaleImage = new Handler();
                mCallThreadRescaleImage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Create a rescaled image
                            createRescaledImage(mCurrentSpinnerSelection);
                        } finally {
                            // Update the view field with the newly rescaled image if successfully rescaled
                            if (mEditedFileAbsolutePath != null) {
                                determineFileViewContent(mEditedFileAbsolutePath);
                            } else {
                                mSpinner.setSelection(RESCALE_FAILED);
                                determineFileViewContent(mOriginalFileAbsolutePath);
                            }
                        }
                    }
                }, 0 /* No delay for file deletion*/);

                // If handler is broken or doesn't instantiate re-enable spinner
                if (mCallThreadRescaleImage == null) {
                    // Hide the progress bar
                    mFileProcessingBar.setVisibility(View.GONE);

                    // Show the image view
                    mFileView.setVisibility(View.VISIBLE);

                    // Disable the spinner while the thread processes the request
                    mSpinner.setEnabled(true);

                }
                break;

            case SMALL_SIZE_SELECTION:
                // Show the progress bar
                mFileProcessingBar.setVisibility(View.VISIBLE);

                // Hide the image view
                mFileView.setVisibility(View.INVISIBLE);

                // Disable the spinner while the thread processes the request
                mSpinner.setEnabled(false);

                // Generate medium sized image (75%)) and setup mFileView accordingly
                mCallThreadRescaleImage = new Handler();
                mCallThreadRescaleImage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Create a rescaled image
                            createRescaledImage(mCurrentSpinnerSelection);
                        } finally {
                            // Update the view field with the newly rescaled image if successfully rescaled
                            if (mEditedFileAbsolutePath != null) {
                                determineFileViewContent(mEditedFileAbsolutePath);
                            } else {
                                mSpinner.setSelection(RESCALE_FAILED);
                                determineFileViewContent(mOriginalFileAbsolutePath);
                            }
                        }
                    }
                }, 0 /* No delay for file deletion*/);

                // If handler is broken or doesn't instantiate re-enable spinner
                if (mCallThreadRescaleImage == null) {
                    // Hide the progress bar
                    mFileProcessingBar.setVisibility(View.GONE);

                    // Show the image view
                    mFileView.setVisibility(View.VISIBLE);

                    // Disable the spinner while the thread processes the request
                    mSpinner.setEnabled(true);

                }
                break;
            default:
                Log.d(LOG_TAG, "Y'never know!");
        }
    }

    /**
     * Sets up the given file's stats
     */
    private void setupFileStats() {
        if (mEditedFileCreated) {
            mFileViewSize.setText(FileUtils.getFileSize(mEditedFileAbsolutePath, getContext()));
            mFileViewResolution.setText(FileUtils.getImageResolution(mEditedFileAbsolutePath, getContext()));
        } else {
            mFileViewSize.setText(FileUtils.getFileSize(mOriginalFileAbsolutePath, getContext()));
            mFileViewResolution.setText(FileUtils.getImageResolution(mOriginalFileAbsolutePath, getContext()));
        }
    }

    private boolean deleteRescaledFile() {
        // Delete Previous File
        File file = new File(mRescaledImageAbsolutePath.toString());

        String canonicalPath;

        // If handler is broken or doesn't instantiate re-enable spinner
        if (mCallThreadRescaleImage == null) {
            // Hide the progress bar
            mFileProcessingBar.setVisibility(View.GONE);
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
                            MediaStore.Files.FileColumns.DATA + "=?", new String[]{mRescaledImageAbsolutePath.toString()});
                }

                // Successfully deleted the file
                mRescaledImageCreated = false;
                return true;
            }

            // Failed to delete the file
            return false;
        }

        return false;
    }

    private boolean deleteEditedFile() {
        // Delete Previous File
        File file = new File(mEditedFileAbsolutePath.toString());

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
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{mEditedFileAbsolutePath.toString()});
            }

            // Successfully deleted the file
            mEditedFileCreated = false;
            mEditedFileAbsolutePath = null;
            return true;
        }

        // Failed to delete the file
        return false;
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
        if (desiredAbsoluteFilePath != null) {
            // File has potentially been found
            mFileNotFoundView.setVisibility(View.GONE);

            // Find out if the file path is present
            if (!(desiredAbsoluteFilePath.toString().isEmpty())) {
                // File was found, setup view data & check to see if the view is representable by glide
                if (FileUtils.representableByGlide(FileUtils.getFileExtensionFromUri(getContext(), desiredAbsoluteFilePath))) {
                    Glide.with(this)
                            .load(FileProvider.getUriForFile(getContext(),
                                    getString(R.string.file_provider_authority),
                                    new File(desiredAbsoluteFilePath.toString())))
                            .apply(RequestOptions.fitCenterTransform())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(mFileView);

                    if (mMimeType.equals(IMAGE_FILE)) {
                        setupFileStats();
                    }

                } else {

                    // Update views to reflect that the file is unable to be shown by glide
                    mFileNotFoundView.setVisibility(View.VISIBLE);
                    mFileNotFoundView.setText("File is unable \nto be shown");

                    // Tell the user
                    UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
                            "File is unable to be shown by Glide", Snackbar.LENGTH_LONG);
                }
            } else {
                // Update views to reflect that the file was not found
                mFabUploadFile.setVisibility(View.GONE);
                mFileNotFoundView.setVisibility(View.VISIBLE);

                // Tell the user
                UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
                        "File location was not found", Snackbar.LENGTH_LONG);
            }
        } else {
            // Update views to reflect that the file is unable to be accessed
            mFabUploadFile.setVisibility(View.GONE);
            mFileNotFoundView.setVisibility(View.VISIBLE);

            // Tell the user
            UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
                    "Unable to obtain chosen file", Snackbar.LENGTH_LONG);

            // Make upload file inaccessible and inform the user.
            mFabUploadFile.setVisibility(View.GONE);
            mFileNotFoundView.setVisibility(View.VISIBLE);
        }

        if (mCallThreadDetermineImage != null) {
            mCallThreadDetermineImage.removeCallbacksAndMessages(null);
        } else if (mCallThreadRescaleImage != null) {
            // Delete the thread
            mCallThreadRescaleImage.removeCallbacksAndMessages(null);
        }
    }

    /**
     * This method will create a rescaled bitmap  image utilizing @FileUtils @createDownscaledImageFile,
     * and upon success, accordingly change mFileView's onClickListener and displayed image. Upon failure
     * Snackbars will be shown.
     *
     * @param rescaleRatioSelected a passed value determined on the onClick
     */
    private void createRescaledImage(int rescaleRatioSelected) {
        //TODO: This will need to be edited when File Fragment Editing is available.
        // Create a bitmap of the original file
        Bitmap bitmap = BitmapFactory.decodeFile(mOriginalFileAbsolutePath.toString());

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

            // Create a rescaled bitmap of the original file
            if (rescaleRatioSelected == 0) {
                // This method must create a bitmap of the original image's resolution
                mRescaledImageCreated = FileUtils.createOriginalScaledImageFile(bitmap, imageFile);
            } else {
                // This method must create a bitmap of the original image's resolution downscaled
                mRescaledImageCreated = FileUtils.createDownscaledImageFile(bitmap, SELECTIONRATIO[rescaleRatioSelected], imageFile);
            }

            // Check to see if the rescaled image has been created
            if (mRescaledImageCreated) {
                // Verify that the image is actually rescaled appropriately
                if (rescaleRatioSelected == 0) {
                    mRescaledImageDownscaled = FileUtils.checkSuccessfulBitmapUpscale(mOriginalFileAbsolutePath,
                            mRescaledImageAbsolutePath);
                    Log.d(LOG_TAG, "rescaleRatio: 0: " + mRescaledImageCreated);
                } else {
                    mRescaledImageDownscaled = FileUtils.checkSuccessfulBitmapDownscale(mOriginalFileAbsolutePath,
                            mRescaledImageAbsolutePath);
                    Log.d(LOG_TAG, "rescaleRatio: " + SELECTIONRATIO[rescaleRatioSelected] + mRescaledImageCreated);
                }

                //
                if (mRescaledImageDownscaled) {
                    // if an edited image exists delete it
                    if (mEditedFileCreated && !mEditedFileAbsolutePath.toString().isEmpty()) {
                        deleteEditedFile();
                    }

                    // Point resources to their appropriate variables
                    mEditedFileAbsolutePath = mRescaledImageAbsolutePath;
                    mEditedFileCreated = true;

                    // Setup the EditedFileInfo object
                    mEditedFileData.setRescaledImageQuality(SELECTIONRATIO[rescaleRatioSelected]);

                    // Send the updated Uri to the other fragments and update them
                    mSendUri.uploadTransferEditedFileData(mEditedFileData);
                    ((GalleryUploadActivity) getActivity()).mSectionsPagerAdapter
                            .getFragment(GalleryUploadActivity.VIEWPAGER_EDIT_EXIF_FRAGMENT).onResume();

                    // Remove rescaled image absolute path uri
                    mRescaledImageAbsolutePath = null;
                    mRescaledImageCreated = false;

                    // Return the UI back to normal
                    closeOutRescaleImageThread(getString(R.string.sb_image_successfully_rescaled));
                } else {
                    Log.d(LOG_TAG, "Created image wasn't rescaled properly");

                    // Delete the rescaled file
                    deleteRescaledFile();

                    // Remove rescaled image absolute path uri
                    mRescaledImageAbsolutePath = null;
                    mRescaledImageCreated = false;

                    // Return the UI back to normal
                    closeOutRescaleImageThread(getString(R.string.sb_image_unsuccessfully_rescaled));
                }
            } else {
                Log.d(LOG_TAG, "Created image wasn't created properly");

                // Return the UI back to normal
                closeOutRescaleImageThread(getString(R.string.sb_image_unsuccessfully_created));
            }
        } else {
            Log.d(LOG_TAG, "No image file was created");

            // Return the UI back to normal
            closeOutRescaleImageThread(getString(R.string.sb_image_unsuccessfully_created));
        }
    }

    /**
     * Enables and shows the views disabled and hidden during @mCallThreadRescaleImage and hides
     * the processing bar before closing out thread
     */
    private void closeOutRescaleImageThread(String snackbarMessage) {
        // Enable the spinner
        mSpinner.setEnabled(true);

        // Hide the progress bar
        mFileProcessingBar.setVisibility(View.GONE);

        // Show the image view
        mFileView.setVisibility(View.VISIBLE);

        // Let the user know the image was successfully rescaled
        Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content),
                snackbarMessage, Snackbar.LENGTH_LONG).show();

        // Delete the thread
        mCallThreadRescaleImage.removeCallbacksAndMessages(null);
    }

    /**
     * Creates an image file with a given name at the location
     *
     * @return returns the generated file created at the desired location
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create a unique image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HH-mm-ss").format(new Date());
        String imageFileName = getString(R.string.image_file_start) + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        storageDir.mkdir();
        File image = File.createTempFile(
                imageFileName,
                getString(R.string.image_file_extension),
                storageDir
        );

        // File path
        mRescaledImageAbsolutePath = Uri.parse(image.getAbsolutePath());

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
        Log.d(LOG_TAG, "mRescaledImageAbsolutePath: " + imagePath);
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
    private void uploadFile() {

        // Create Retrofit Instance
        Retrofit retrofit = RetrofitClient.getClient(getString(R.string.wetfish_base_url));

        // Create REST Interface
        RESTInterface restInterface = retrofit.create(RESTInterface.class);

        Log.d(LOG_TAG, "mEditedFileCreated?: " + mEditedFileCreated + " " + mEditedFileAbsolutePath.toString());

        // Provide the correct image to Wetfish depending on the images currently available
        if (mEditedFileCreated) {

            // Should a rescaled image be present
            // Populate the file with the correct data to later pass to the  RequestBody instance
            File file = new File(mEditedFileAbsolutePath.toString());

            // Gather file extension from chosen file for database
            final String fileExtension = FileUtils.getFileExtensionFromUri(getContext(), mEditedFileAbsolutePath);

            // Gather file URI from the chosen file for database.
            final String filePath = mOriginalFileAbsolutePath.toString();

            Log.d(LOG_TAG, "mFileEditedAbsolutePath: " + mEditedFileAbsolutePath);
            Log.d(LOG_TAG, "filePath: " + filePath);

            // Create RequestBody & MultipartBody to create a Call.
            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("Image", file.getName(), requestBody);
            mCall = restInterface.postFile(body);

            // Execute call request
            mCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        // Get response body as a string
                        String onResponseString = response.body().string();
                        Log.d(LOG_TAG, "onResponse: " + onResponseString);

                        //  Edited image path
                        String editedFilePath;
                        if (mEditedFileCreated) {
                            editedFilePath = mEditedFileAbsolutePath.toString();
                        } else {
                            editedFilePath = "";
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
                                        editedFilePath);

                                Log.d(LOG_TAG, "\nFilePath: " + filePath + "\nEditedFilePath: " + editedFilePath);

                                /**
                                 *  Check to see if upload was successful to determine if the rescaled image
                                 * should be kept or deleted
                                 */
                                if (uploadID >= 0) {
                                    mDatabaseAdditionSuccessful = true;

                                    // Update media
                                    if (mMimeType.equals(IMAGE_FILE)) {
                                        sendMediaBroadcast(editedFilePath);
                                    }

                                } else {
                                    mDatabaseAdditionSuccessful = false;
                                }

                                Log.d(LOG_TAG, "onResponse: " + responseViewURL);
                                Log.d(LOG_TAG, "id: " + uploadID);

//                            UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
//                                    "File Uploaded!", Snackbar.LENGTH_LONG);
                                mFabProgressCircleUpload.beginFinalAnimation();
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
                                        editedFilePath);

                            /*
                               Check to see if upload was successful to determine if the rescaled image
                              should be kept or deleted
                             */
                                if (uploadID >= 0) {
                                    mDatabaseAdditionSuccessful = true;

                                    // Update media
                                    if (mMimeType.equals(IMAGE_FILE)) {
                                        sendMediaBroadcast(editedFilePath);
                                    }

                                } else {
                                    mDatabaseAdditionSuccessful = false;
                                }

                                Log.d(LOG_TAG, "onResponse: " + responseViewURL);
                                Log.d(LOG_TAG, "id: " + uploadID);

//                            UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
//                                    "File Uploaded!", Snackbar.LENGTH_LONG);
                                mFabProgressCircleUpload.beginFinalAnimation();
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
                                    editedFilePath);

                        /*
                           Check to see if upload was successful to determine if the rescaled image
                          should be kept or deleted
                         */
                            if (uploadID >= 0) {
                                mDatabaseAdditionSuccessful = true;

                                // Update media
                                if (mMimeType.equals(IMAGE_FILE)) {
                                    sendMediaBroadcast(editedFilePath);
                                }

                            } else {
                                mDatabaseAdditionSuccessful = false;
                            }

                            Log.d(LOG_TAG, "onResponse: " + responseViewURL);
                            Log.d(LOG_TAG, "id: " + uploadID);

//                        UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
//                                "File Uploaded!", Snackbar.LENGTH_LONG);
                            mFabProgressCircleUpload.beginFinalAnimation();
                        }
                    } catch (IOException e) {
                        Log.d(LOG_TAG, "onFailure Catch: ");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    mFabUploadFile.setClickable(true);
                    mFabProgressCircleUpload.hide();
                    mSpinner.setClickable(true);

                    if (mCallCanceled) {
                        // Pass the user a success notification of cancellation
                        Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content), getContext().getString(R.string.sb_cloud_upload_cancelled), Snackbar.LENGTH_SHORT)
                                .show();

                        // Reset mCallCanceled
                        mCallCanceled = false;
                    } else {
                        //
                        UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
                                "File Upload Failed!", Snackbar.LENGTH_LONG);
                    }

                    Log.d(LOG_TAG, "onFailure Response: " + t);
                }
            });

        } else {
            // Should no edited image be present

            // Populate the file with the correct data to later pass to the  RequestBody instance
            File file = new File(mOriginalFileAbsolutePath.toString());

            // Gather file extension from chosen file for database
            final String fileExtension = FileUtils.getFileExtensionFromUri(getContext(), mOriginalFileAbsolutePath);

            // Gather file URI from chosen file for database.
            final String filePath = mOriginalFileAbsolutePath.toString();

            Log.d(LOG_TAG, "mOriginalFileAbsolutePath: " + mOriginalFileAbsolutePath);
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

                        //  There is no edited file
                        String editedFilePath = "";

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
                                        editedFilePath);

                                Log.d(LOG_TAG, "\nFilePath: " + filePath + "\nEditedFilePath: " + editedFilePath);

                                /**
                                 *  Check to see if upload was successful to determine if the edited image
                                 * should be kept or deleted
                                 */
                                if (uploadID >= 0) {
                                    mDatabaseAdditionSuccessful = true;

                                    // Update media
                                    if (mMimeType.equals(IMAGE_FILE)) {
                                        sendMediaBroadcast(editedFilePath);
                                    }

                                } else {
                                    mDatabaseAdditionSuccessful = false;
                                }

                                Log.d(LOG_TAG, "onResponse: " + responseViewURL);
                                Log.d(LOG_TAG, "id: " + uploadID);

//                            UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
//                                    "File Uploaded!", Snackbar.LENGTH_LONG);
                                mFabProgressCircleUpload.beginFinalAnimation();
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
                                        editedFilePath);

                            /*
                               Check to see if upload was successful to determine if the edited image
                              should be kept or deleted
                             */
                                if (uploadID >= 0) {
                                    mDatabaseAdditionSuccessful = true;

                                    // Update media
                                    if (mMimeType.equals(IMAGE_FILE)) {
                                        sendMediaBroadcast(editedFilePath);
                                    }

                                } else {
                                    mDatabaseAdditionSuccessful = false;
                                }

                                Log.d(LOG_TAG, "onResponse: " + responseViewURL);
                                Log.d(LOG_TAG, "id: " + uploadID);

//                            UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
//                                    "File Uploaded!", Snackbar.LENGTH_LONG);
                                mFabProgressCircleUpload.beginFinalAnimation();
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
                                    editedFilePath);

                        /*
                           Check to see if upload was successful to determine if the edited image
                          should be kept or deleted
                         */
                            if (uploadID >= 0) {
                                mDatabaseAdditionSuccessful = true;

                                // Update media
                                if (mMimeType.equals(IMAGE_FILE)) {
                                    sendMediaBroadcast(editedFilePath);
                                }

                            } else {
                                mDatabaseAdditionSuccessful = false;
                            }

                            Log.d(LOG_TAG, "onResponse: " + responseViewURL);
                            Log.d(LOG_TAG, "id: " + uploadID);

//                        UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
//                                "File Uploaded!", Snackbar.LENGTH_LONG);
                            mFabProgressCircleUpload.beginFinalAnimation();
                        }
                    } catch (IOException e) {
                        Log.d(LOG_TAG, "onFailure Catch: ");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    mFabUploadFile.setClickable(true);
                    mFabProgressCircleUpload.hide();

                    if (mCallCanceled) {
                        // Pass the user a success notification of cancellation
                        Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content), getContext().getString(R.string.sb_cloud_upload_cancelled), Snackbar.LENGTH_SHORT)
                                .show();

                        // Reset mCallCanceled
                        mCallCanceled = false;
                    } else {
                        //
                        UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
                                "File Upload Failed!", Snackbar.LENGTH_LONG);
                    }

                    Log.d(LOG_TAG, "onFailure Response: " + t);
                }
            });
        }
    }

    // TODO: Reintegrate tab 2 if & when video editing is added
    private void removeTabsForVideoFiles() {
        TabLayout.Tab uploadTab = mTabLayout.getTabAt(0);
        TabLayout.Tab exifTab = mTabLayout.getTabAt(1);
        TabLayout.Tab editTab = mTabLayout.getTabAt(2);

        if (exifTab != null) {
            mTabLayout.removeTab(exifTab);
        }

        if (editTab != null) {
            mTabLayout.removeTab(editTab);
        }

        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

    }

    /** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ **/
    /** Methods for the EXIF portion of the handler thread **/
    /** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ **/

    /**
     * Creates a file at the given location with a specified unique name
     *
     * @return returns the generated file created at the desired location
     * @throws IOException
     */
    private File createFile() throws IOException {
        // Create a unique image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HH-mm-ss").format(new Date());
        String imageFileName = getString(R.string.image_file_start) + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        storageDir.mkdir();
        File image = File.createTempFile(
                imageFileName,
                getString(R.string.image_file_extension),
                storageDir
        );

        // File path
        mEditedFileAbsolutePathTemp = Uri.parse(image.getAbsolutePath());

        return image;
    }

    /**
     * Deletes the temporary edited file being utilized
     *
     * @return true if the file was successfully deleted, otherwise return false upon failure
     */
    private boolean deleteTempEditedFile() {
        // Delete Previous File
        File file = new File(mEditedFileAbsolutePathTemp.toString());

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
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{mEditedFileAbsolutePathTemp.toString()});
            }

            // Successfully deleted the file, make it null
            mEditedFileAbsolutePathTemp = null;
            return true;
        }

        // Failed to delete the file
        return false;
    }

    /**
     * Utilizes @createFile to generate an image file that's a copy of @mFileAbsolutePath save
     * for the exif values
     *
     * @return a true if the image was successfully copied, otherwise return false
     */
    private boolean exifCreateEditedImageFile() {
        // Image creation is occurring
        mImageCreationOccurring = true;

        // Create a bitmap of the original file
        Bitmap bitmap = BitmapFactory.decodeFile(mOriginalFileAbsolutePath.toString());

        // Create the file that the result will populate
        File imageFile = null;

        // Edited image not created
        mEditedFileCreated = false;

        try {
            // Create the file name that we'd like to use and populate
            imageFile = createFile();
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error occurred while creating the file: " + e);
            e.printStackTrace();
        }

        if (imageFile != null) {
            // Utilize the bitmap collected above, being a bitmap of the original file unless an edited file exists
            mDuplicateImageCreated = FileUtils.createOriginalScaledImageFile(bitmap, imageFile);

            // Check to see if the duplicate image has been created
            if (mDuplicateImageCreated) {
                // Verify that the image is actually rescaled appropriately
                mDuplicateImageCreated = FileUtils.checkSuccessfulBitmapDuplication(mOriginalFileAbsolutePath,
                        mEditedFileAbsolutePathTemp);

                // Verify that the bitmap was appropriately rescaled
                if (mDuplicateImageCreated) {

                    mEditedFileAbsolutePath = mEditedFileAbsolutePathTemp;
                    mEditedFileAbsolutePathTemp = null;

                    // Edited image created
                    mEditedFileCreated = true;

                    return true;
                } else {
                    Log.d(LOG_TAG, "Image Creation Failed");
                    Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content),
                            R.string.sb_image_unsuccessfully_created, Snackbar.LENGTH_LONG).show();

                    // Delete the failed file
                    deleteTempEditedFile();

                    return false;
                }
            } else {
                Log.d(LOG_TAG, "Image Creation Failed");
                Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content),
                        R.string.sb_image_unsuccessfully_created, Snackbar.LENGTH_LONG).show();

                // Delete the failed file
                deleteTempEditedFile();

                return false;
            }
        } else {
            Log.d(LOG_TAG, "Image Creation Failed");
            Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content),
                    R.string.sb_image_unsuccessfully_created, Snackbar.LENGTH_LONG).show();

            // Delete the failed file
            deleteTempEditedFile();

            return false;
        }
    }

    /**
     * Method to initialize @mEditedFile
     */

    public interface UploadFragmentUriUpdate {
        void uploadTransferEditedFileData(EditedFileData editedFileData);
    }
}