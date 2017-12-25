package net.wetfish.wetfish.ui;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import net.wetfish.wetfish.R;
import net.wetfish.wetfish.adapters.FilesAdapter;
import net.wetfish.wetfish.utils.FileUtils;
import net.wetfish.wetfish.utils.UIUtils;

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
    // Intent Constant
    private int PICK_FILE = 1;
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
    private GridLayoutManager gridLayoutManager;

    /* Data */
    private NetworkInfo networkInfo;


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
        gridLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        // Setup adapter for Recycler View
        mFilesAdapter = new FilesAdapter(this, this);
        mRecyclerView.setAdapter(mFilesAdapter);

        // FAB to start intent to select a file then pass the user to another activity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFileToUpload();
            }
        });

        // Acquire a connectivity manager to see network status
        ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Gather the result code from our intent result and start GalleryUploadActivity.class
     *
     * @param reqCode
     * @param resultCode Determines the result of the request
     * @param data       Gathers data from the passed intent
     */
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri contentUri = data.getData();

            startActivity(new Intent(this, GalleryUploadActivity.class)
                    .setDataAndType(contentUri, getString(R.string.file_mime_type)));
        } else {
            //TODO: Probably should remove snackbar later
            UIUtils.generateSnackbar(this, findViewById(android.R.id.content),
                    "No file selected", Snackbar.LENGTH_SHORT);
            Log.d(LOG_TAG, "Result Code Returned: " + resultCode);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Update files every time onStart is called
        updateFiles();
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
            loaderManager.initLoader(FILES_LOADER, null, this).forceLoad();
        } else {
            // Restart loader
            loaderManager.restartLoader(FILES_LOADER, null, this).forceLoad();
        }
    }

    /**
     * This method will be invoked when the FAB is activated.
     */
    private void selectFileToUpload() {
        // Attempt to open gallery
        try {

            Intent pickFileIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickFileIntent.setType(getString(R.string.file_mime_type));

            Intent useCameraIntentPicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Intent useCameraIntentVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

            Intent chooserIntent = Intent.createChooser(pickFileIntent, getString(R.string.chooser_title));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{useCameraIntentPicture, useCameraIntentVideo});

            startActivityForResult(chooserIntent, PICK_FILE);
        } catch (Exception e) {
            Log.d(LOG_TAG, "An exception occurred!: " + e.toString());
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
        } else if (cursor != null && !(cursor.moveToFirst())){
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
