package net.wetfish.wetfish.ui;

import android.Manifest;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import net.wetfish.wetfish.R;
import net.wetfish.wetfish.adapters.FilesAdapter;
import net.wetfish.wetfish.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GalleryActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        FilesAdapter.FileAdapterOnClickHandler {

    /* Constants */
    // Logging Tag
    private static final String LOG_TAG = GalleryActivity.class.getSimpleName();
    // Loader ID
    private static final int FILES_LOADER = 0;
    // Key for saved instance state value
    private static final String BUNDLE_KEY = "fileCursorKey";
    // Intent Request Codes
    private static final int REQUEST_PICK_FILE = 1;
    private static final int REQUEST_CAPTURE_IMAGE = 2;
    private static final int REQUEST_CAPTURE_VIDEO = 3;
    private static final int REQUEST_STORAGE = 0;
    private static final String[] PERMISSIONS_STORAGE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    // Content Provider Auto Increment Buffer
    private int POSITION_BUFFER = 1;

    /* Views */
    // Progress bar utilized during loader
    private ProgressBar mProgressBar;
    // Text view for an empty gallery
    private TextView mEmptyStateView;
    // Recycler view to represent gallery
    private RecyclerView mRecyclerView;
    // Files adapter for Recycler View
    private FilesAdapter mFilesAdapter;
    // Layout Manager for recycler view
    private GridLayoutManager mGridLayoutManager;
    // FAB menu view
    private FloatingActionMenu mFAM;
    // FAM menu option FABs
    private FloatingActionButton mTakePictureFAB;
    private FloatingActionButton mTakeVideoFAB;
    private FloatingActionButton mSelectFileFab;

    /* Data */
    // Network information
    private NetworkInfo mNetworkInfo;
    // FileProvider Uri for the image file
    private Uri mCurrentImageUri;
    // File path for the image file
    private String mCurrentImagePath;
    // FileProvider Uri for the video file
    private Uri mCurrentVideoUri;
    // File path for the video file
    private String mCurrentVideoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Reference included layout
        View includeLayout = findViewById(R.id.include_layout_gallery);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Progress Bar
        mProgressBar = includeLayout.findViewById(R.id.pb_loading);

        // Empty State text view and refresh button
        mEmptyStateView = includeLayout.findViewById(R.id.tv_no_results);

        // Recycler View for Files
        mRecyclerView = includeLayout.findViewById(R.id.rv_files);
        mRecyclerView.setHasFixedSize(true);

        // Setup layout for the Recycler View
        mGridLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        // Setup adapter for Recycler View
        mFilesAdapter = new FilesAdapter(this, this);
        mRecyclerView.setAdapter(mFilesAdapter);

        // FAM
        mFAM = findViewById(R.id.fam_gallery);

        // Close FAM if clicking outside of a button
        mFAM.setClosedOnTouchOutside(true);

        // FAB to start intent to select a file then pass the user to another activity
        mTakePictureFAB = findViewById(R.id.fab_take_picture);
        mTakeVideoFAB = findViewById(R.id.fab_take_video);
        mSelectFileFab = findViewById(R.id.fab_select_file);

        mTakePictureFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Ask for storage permission if @ or above Android version 23
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(getBaseContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(getBaseContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Permissions have not been granted, inform the user and ask again
                        requestStoragePermission();

                    } else {
                        // Storage permissions granted!
                        captureImageToUpload();
                    }
                } else {
                    captureImageToUpload();
                }
            }
        });

        mTakeVideoFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Ask for storage permission if @ or above Android version 23
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(getBaseContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(getBaseContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Permissions have not been granted, inform the user and ask again
                        requestStoragePermission();

                    } else {
                        // Storage permissions granted!
                        captureVideoToUpload();
                    }
                } else {
                    captureVideoToUpload();
                }
            }
        });

        mSelectFileFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFileToUpload();
            }
        });

        // Acquire a connectivity manager to see network status
        ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        mNetworkInfo = connectivityManager.getActiveNetworkInfo();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

            // If the user has previously denied granting the permission, offer the rationale
            Snackbar.make(findViewById(android.R.id.content), R.string.permission_storage_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onClick(View view) {
                            requestPermissions(PERMISSIONS_STORAGE, REQUEST_STORAGE);
                        }
                    }).show();
        } else {
            // No explanation needed, request permission
            {
                requestPermissions(PERMISSIONS_STORAGE, REQUEST_STORAGE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        return true;
    }

    //Here's a comment
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Update files every time onStart is called
        updateFiles();
    }

    /**
     * Select an image or video to upload
     * //TODO: Is ready to also select more than video/* and image/*
     */
    private void selectFileToUpload() {
        // Attempt to open gallery
        try {
            // TODO: This chunk of code allows for files to be picked from the file system and allow for other file types to be added.
//            Intent pickFileIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            Intent pickFileIntentTwo = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            pickFileIntent.setType(getString(R.string.file_mime_type));
//            pickFileIntentTwo.setType(getString(R.string.file_mime_type));
//
//            Intent useCameraIntentPicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            Intent useCameraIntentVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//
//            Intent chooserIntent = Intent.createChooser(pickFileIntent, getString(R.string.chooser_title));
//            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{useCameraIntentPicture, useCameraIntentVideo, pickFileIntentTwo});

            Intent pickFileIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickFileIntent.setType(getString(R.string.file_mime_type));

            Intent chooserIntent = Intent.createChooser(pickFileIntent, getString(R.string.select_upload_file));

            startActivityForResult(chooserIntent, REQUEST_PICK_FILE);
        } catch (Exception e) {
            Log.d(LOG_TAG, "An exception occurred!: " + e.toString());
        }

    }

    /**
     * Capture an image or video to upload
     */
    private void captureImageToUpload() {
        // Setup intent
        Intent cameraImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Check to see if an application is available to open the intent and a camera is available
        if (cameraImageIntent.resolveActivity(getPackageManager()) != null &&
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {

            // Create the file that the result will populate
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error occurred while creating the file: " + e);
                e.printStackTrace();
            }

            if (imageFile != null) {
                // Get a shareable content:// uri
                Uri imageUri = FileProvider.getUriForFile(this,
                        getString(R.string.file_provider_authority),
                        imageFile);

                // Set member variable to that uri to pass to the next activity
                mCurrentImageUri = imageUri;
                Log.d(LOG_TAG, "New Image File Provider Path: " +  mCurrentImageUri);

                cameraImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraImageIntent, REQUEST_CAPTURE_IMAGE);
            }
        }
    }

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
        mCurrentImagePath = image.getAbsolutePath();
        Log.d(LOG_TAG, "New Image Absolute Path: " + mCurrentImagePath);

        return image;
    }

    /**
     * Capture a video to upload
     */
    private void captureVideoToUpload() {
        // Setup intent
        Intent cameraVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        // Check to see if an application is available to open the intent and a camera is available
        if (cameraVideoIntent.resolveActivity(getPackageManager()) != null &&
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {

            // Create teh file that the result will populate
            File videoFile = null;
            try {
                videoFile = createVideoFile();
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error occurred while creating the file: " + e);
                e.printStackTrace();
            }

            if (videoFile != null) {
                // Get a shareable content:// uri
                Uri videoUri = FileProvider.getUriForFile(this,
                        getString(R.string.file_provider_authority),
                        videoFile);

                // Set member variable to that uri to pass to the next activity
                mCurrentVideoUri = videoUri;

                cameraVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
                startActivityForResult(cameraVideoIntent, REQUEST_CAPTURE_VIDEO);
            } else {
                Log.d(LOG_TAG, "Video file was null");
            }
        } else {
            Log.d(LOG_TAG, "Nothing exists to take a video?");
        }
    }

    private File createVideoFile() throws IOException {
        // Create a unique image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName = getString(R.string.image_file_start) + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        storageDir.mkdir();
        File video = File.createTempFile(
                videoFileName,
                getString(R.string.video_file_extension),
                storageDir
        );

        //File path
        mCurrentVideoPath = video.getAbsolutePath();

        return video;
    }

    /**
     * Gather the result code from our intent result and start GalleryUploadActivity.class
     * This passes the absolute image path through the intent
     *
     * @param reqCode
     * @param resultCode Determines the result of the request
     * @param data       Gathers data from the passed intent
     */
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        // Close FAM.
        mFAM.close(true);

        // Determine the result of the activity.
        if (resultCode == RESULT_OK) {
            switch (reqCode) {
                case REQUEST_PICK_FILE:
                    // User decided to select an already existing file.
                    Uri contentUri = data.getData();

                    Log.d(LOG_TAG, contentUri.toString());
                    contentUri = Uri.parse(FileUtils.getRealPathFromUri(this, contentUri));

                    Log.d(LOG_TAG, contentUri.toString());

                    startActivity(new Intent(this, GalleryUploadActivity.class)
                            .setDataAndType(contentUri, getString(R.string.file_mime_type)));
                    break;
                case REQUEST_CAPTURE_IMAGE:
                    // User decided to capture an image
                    // Inform media scanner so that is immediately available
                    Intent imageMediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File imageFile = new File(mCurrentImagePath);
                    Uri imageContentUri = Uri.fromFile(imageFile);
                    imageMediaScanIntent.setData(imageContentUri);
                    this.sendBroadcast(imageMediaScanIntent);

                    startActivity(new Intent(this, GalleryUploadActivity.class)
                            .setDataAndType(Uri.parse(mCurrentImagePath), getString(R.string.image_mime_type)));
                    break;
                case REQUEST_CAPTURE_VIDEO:
                    // User decided to capture a video
                    // Inform media scanner so that is immediately available
                    Intent videoMediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File videoFile = new File(mCurrentVideoPath);
                    Uri videoContentUri = Uri.fromFile(videoFile);
                    videoMediaScanIntent.setData(videoContentUri);
                    this.sendBroadcast(videoMediaScanIntent);

                    startActivity(new Intent(this, GalleryUploadActivity.class)
                            .setDataAndType(Uri.parse(mCurrentVideoPath), getString(R.string.video_mime_type)));
                    break;
                default:
                    Snackbar.make(findViewById(android.R.id.content), R.string.no_file_selected, Snackbar.LENGTH_LONG).show();

                    Log.d(LOG_TAG, "Result Code Returned: " + resultCode);
                    break;

            }
        } else {
            Snackbar.make(findViewById(android.R.id.content), R.string.no_file_selected, Snackbar.LENGTH_LONG).show();

            Log.d(LOG_TAG, "Result Code Returned: " + resultCode);

        }
    }

    /**
     * Take in the id from the adapter of the current cursor location. This will allow for the querying
     * of the correct row
     *
     * @param id position of cursor
     */
    @Override
    public void onListItemClick(int id) {
        // Create file detail activity intent
        Intent fileDetails = new Intent(this, GalleryDetailActivity.class);

        // Pass the Uri to the corresponding gallery item
        fileDetails.putExtra(getString(R.string.file_details),
                FileUtils.getFileData(this, id + POSITION_BUFFER));

        // Start GalleryDetailActivity
        startActivity(fileDetails);
    }

    private void updateFiles() {
        // TODO: Work out where the image will be loading from and options therein. Can incorporate network connection and more.
        // Get loader and see if it exists
        LoaderManager loaderManager = getLoaderManager();
        Loader<Cursor> fileLoader = loaderManager.getLoader(FILES_LOADER);

        // If loader doesn't exist
        if (fileLoader == null) {
            // Initialize loader
            Log.d(LOG_TAG, "Initialize Loader");
            loaderManager.initLoader(FILES_LOADER, null, this).forceLoad();
        } else {
            // Restart loader
            loaderManager.restartLoader(FILES_LOADER, null, this).forceLoad();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Check for the loader
        return new AsyncTaskLoader<Cursor>(this) {


            public void onStartLoading() {
                // TODO: Make and show progress bar
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public Cursor loadInBackground() {
                return FileUtils.getFilesData(getContext());
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // TODO: Make and Hide progress bar

        if (cursor != null && cursor.moveToFirst()) {
            // If the cursor has data
            mFilesAdapter.swapCursor(cursor);
            mEmptyStateView.setVisibility(View.GONE);
        } else if (cursor != null && !(cursor.moveToFirst())) {
            // If the cursor has no data
            mEmptyStateView.setVisibility(View.VISIBLE);
        } else {
            mEmptyStateView.setText(getString(R.string.error_loading_db));
            mEmptyStateView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFilesAdapter.swapCursor(null);
    }
}
