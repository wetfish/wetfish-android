package net.wetfish.wetfish.ui;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import net.wetfish.wetfish.R;
import net.wetfish.wetfish.data.FileInfo;
import net.wetfish.wetfish.utils.FileUtils;

import org.parceler.Parcels;

import java.io.File;

public class GalleryDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /* Constants */
    // Logging Tag
    private static final String LOG_TAG = GalleryDetailActivity.class.getSimpleName();
    // Loader ID
    private static final int FILES_DETAIL_LOADER = 1;
    // Bundle key to save instance state
    private static final String BUNDLE_KEY = "fileInfoKey";

    /* FAM & FABs */
    // Display FABs
    private FloatingActionMenu mFAM;
    // Visit file URL
    private FloatingActionButton mVisitFileFAB;
    // Copy visit file URL
    private FloatingActionButton mCopyFileURLFAB;
    // Visit delete file URL
    private FloatingActionButton mVisitFileDeleteFAB;
    // Copy visit delete file URL
    private FloatingActionButton mCopyFileDeleteURLFAB;

    /* Views */
    // File image view TODO: Impelemnt exoplayer later if video playback is desired
    private ImageView mFileView;
    // File name text view
    private TextView mFileTitleTextView;
    // File tags text view
    private TextView mFileTagsTextView;
    // File description text view
    private TextView mFileDescriptionTextView;
    // Layout include reference
    private View mIncludeLayout;

    /* Data */
    // Uri for the sent cursor
    private Uri mUri;
    // FileInfo object that holds all data
    private FileInfo mFileInfo;
    // FileInfo string that holds file location
    private String mFileStorageLink;
    // FileType string that holds the file extension type
    private String mFileType;

    //TODO: Later on when Video Playback is possible with exoplayer the focus feature will only be for images
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_detail);

        // Reference included layout
        mIncludeLayout = findViewById(R.id.include_layout_gallery_detail);

        // Views
        // TODO: Support Video Views soon. (Glide/VideoView/Exoplayer)
        mFileView = mIncludeLayout.findViewById(R.id.iv_gallery_item_detail);
        mFileTitleTextView = mIncludeLayout.findViewById(R.id.tv_title);
        mFileTagsTextView = mIncludeLayout.findViewById(R.id.tv_tags);
        mFileDescriptionTextView = mIncludeLayout.findViewById(R.id.tv_description);

        // Setup file interaction
        mFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to find proper app to open file
                Intent selectViewingApp = new Intent();
                selectViewingApp.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                selectViewingApp.setAction(Intent.ACTION_VIEW);

                // Uri path to the file
                Uri fileProviderUri;

                // Use FileProvider to get an appropriate URI compatible with version Nougat+
                Log.d(LOG_TAG, "File Storage Link: " + mFileStorageLink);
                fileProviderUri = FileProvider.getUriForFile(GalleryDetailActivity.this,
                        getString(R.string.file_provider_authority),
                        new File(mFileStorageLink));

                // Setup the data and type
                // Appropriately determine mime type for the file
                selectViewingApp.setDataAndType(fileProviderUri, FileUtils.determineMimeType(GalleryDetailActivity.this, mFileType));

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    selectViewingApp.setClipData(ClipData.newRawUri("", fileProviderUri));
                    selectViewingApp.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                Log.d(LOG_TAG, "Quack: " + fileProviderUri.toString());

                // Check to see if an app can open this file. If so, do so, if not, inform the user
                PackageManager packageManager = getPackageManager();
                if (selectViewingApp.resolveActivity(packageManager) != null) {
                    startActivity(selectViewingApp);
                } else {
                    Snackbar.make(mIncludeLayout, R.string.sb_no_app_available, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // FAM
        mFAM = findViewById(R.id.fam_gallery_detail);

        // Close FAM if clicking outside of a button.
        mFAM.setClosedOnTouchOutside(true);

        // FABs
        //TODO: Add an upload FAB!
        mVisitFileFAB = findViewById(R.id.fab_visit_upload_link);
        mCopyFileURLFAB = findViewById(R.id.fab_copy_upload_link);
        mVisitFileDeleteFAB = findViewById(R.id.fab_visit_deletion_link);
        mCopyFileDeleteURLFAB = findViewById(R.id.fab_copy_deletion_link);

        mVisitFileFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Close FAM.
                mFAM.close(true);

                // Intent to visit webpage
                Intent webIntent = new Intent(Intent.ACTION_VIEW);

                // Link data
                webIntent.setData(Uri.parse(mFileInfo.getFileWetfishStorageLink()));

                // Start intent
                startActivity(webIntent);
            }
        });

        mCopyFileURLFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Close FAM.
                mFAM.close(true);

                // Allow the link to be copied to the clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Uploaded File Url", mFileInfo.getFileWetfishStorageLink()));

                // Split the string to obtain the link
                String tokens[] = clipboard.getPrimaryClip().toString().split("\\{T:");
                String tokensTwo[] = tokens[1].split("\\}");
                String clipboardClipData = tokensTwo[0];

                // Check to see if the clipboard data link equals the database stored link
                if (clipboardClipData.equals(mFileInfo.getFileWetfishStorageLink())) {
                    Snackbar.make(findViewById(android.R.id.content), R.string.sb_url_clipboard_success,
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), R.string.sb_url_clipboard_failure,
                            Snackbar.LENGTH_SHORT).show();
                }

            }
        });

        mVisitFileDeleteFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Close FAM.
                mFAM.close(true);

                // Intent to visit webpage
                Intent webIntent = new Intent(Intent.ACTION_VIEW);

                // Link data
                webIntent.setData(Uri.parse(mFileInfo.getFileWetfishDeletionLink()));

                // Start intent
                startActivity(webIntent);
            }
        });

        mCopyFileDeleteURLFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Close FAM.
                mFAM.close(true);

                // Allow link to be copied to the clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Uploaded File Url", mFileInfo.getFileWetfishDeletionLink()));

                if (clipboard.getPrimaryClip().equals(mFileInfo.getFileWetfishDeletionLink())) {
                    Snackbar.make(findViewById(android.R.id.content), R.string.sb_url_clipboard_success,
                            Snackbar.LENGTH_LONG);
                }
            }
        });

        // Get intent data
        Bundle bundle = getIntent().getExtras();
        mUri = (Uri) bundle.get(getString(R.string.file_details));

        // Setup FileInfo
        if (mFileInfo == null) {
            mFileInfo = new FileInfo();
        }

        getLoaderManager().initLoader(FILES_DETAIL_LOADER, null, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFileInfo != null && mFileInfo.getFileInfoInitialized()) {
            getIntent().putExtra(BUNDLE_KEY, Parcels.wrap(mFileInfo));
        }
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        FileInfo fileInfo = Parcels.unwrap(getIntent().getParcelableExtra(BUNDLE_KEY));
        if (fileInfo != null && fileInfo.getFileInfoInitialized()) {
            mFileInfo = fileInfo;
            displayFileDetails(mFileInfo);
        }

    }

    public void displayFileDetails(FileInfo fileInfo) {

        // Should deletion not yet be available, hide these options from view
        if (fileInfo.getFileWetfishDeletionLink().equals(getString(R.string.not_implemented))) {
            mVisitFileDeleteFAB.setVisibility(View.GONE);
            mCopyFileDeleteURLFAB.setVisibility(View.GONE);
        }

        mFileType = fileInfo.getFileExtensionType();
        String fileDevicePath;

        // Show the edited version of the original image if present
        if (fileInfo.getEditedFileDeviceStorageLink() != null) {
            if (!fileInfo.getEditedFileDeviceStorageLink().isEmpty() && !fileInfo.getEditedFileDeviceStorageLink().equals("")) {
                Log.d(LOG_TAG, "editedFile exists and is not empty");
                fileDevicePath = fileInfo.getEditedFileDeviceStorageLink();
            } else {
                Log.d(LOG_TAG, "file does not exist and/or is empty");
                fileDevicePath = fileInfo.getFileDeviceStorageLink();
            }
        } else {
            Log.d(LOG_TAG, "file does not exist and/or is empty");
            fileDevicePath = fileInfo.getFileDeviceStorageLink();
        }

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        // If network is connected search the device for the stored image, then wetfish if not found
        if (networkInfo != null && networkInfo.isConnected()) {
            // Check to see if the image is representable by glide. If not, let the user know.
            if (FileUtils.representableByGlide(mFileType)) {
                Log.d(LOG_TAG, "Representable By Glide: " + fileInfo.getFileWetfishStorageLink());
                Log.d(LOG_TAG, "Representable By Glide: " + fileInfo.getFileDeviceStorageLink());
                Log.d(LOG_TAG, "Representable by Glide: " + fileInfo.getEditedFileDeviceStorageLink());

                Glide.with(this)
                        .load(fileDevicePath)
                        .error(Glide.with(this)
                                .load(fileInfo.getFileWetfishStorageLink())
                                .apply(RequestOptions.centerCropTransform()))
                        .apply(RequestOptions.placeholderOf(new ColorDrawable(Color.DKGRAY)))
                        .apply(RequestOptions.fitCenterTransform())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(mFileView);
            } else {
                Log.d(LOG_TAG, "Representable By Glide: " + fileInfo.getFileWetfishStorageLink());
                Log.d(LOG_TAG, "Representable By Glide: " + fileInfo.getFileDeviceStorageLink());
                Log.d(LOG_TAG, "Representable by Glide: " + fileInfo.getEditedFileDeviceStorageLink());
                // If not, let the user know
                //TODO: Figure out a good method for this later. In the meantime, black image.
                Glide.with(this)
                        .load(null)
                        .apply(RequestOptions.placeholderOf(new ColorDrawable(Color.CYAN)))
                        .apply(RequestOptions.fitCenterTransform())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(mFileView);

                Snackbar.make(mIncludeLayout, R.string.sb_not_representable_by_glide, Snackbar.LENGTH_LONG).show();
            }
        } else {
            // If network is not connected search the device for the stored file on the
            // device then show a black image if not found.
            //TODO: Figure out a good method for this later. In the meantime, storage or black image.
            Glide.with(this)
                    .load(fileInfo.getFileDeviceStorageLink())
                    .error(Glide.with(this)
                            .load(new ColorDrawable(Color.BLACK))
                            .apply(RequestOptions.fitCenterTransform()))
                    .apply(RequestOptions.placeholderOf(new ColorDrawable(Color.DKGRAY)))
                    .apply(RequestOptions.fitCenterTransform())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(mFileView);

            Snackbar.make(mIncludeLayout, R.string.sb_network_not_connected, Snackbar.LENGTH_LONG).show();
        }


        mFileTitleTextView.setText(fileInfo.getFileTitle());
        mFileTagsTextView.setText(fileInfo.getFileTags());
        mFileDescriptionTextView.setText(fileInfo.getFileDescription());

        // File storage link to be used as a passed value for the intent when the file is clicked
        mFileStorageLink = fileDevicePath;
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new AsyncTaskLoader<Cursor>(this) {

            public void onStartLoading() {
                forceLoad();
            }

            @Override
            public Cursor loadInBackground() {

                // Gather the cursor at location mUri within files db
                return getContext().getContentResolver().query(mUri,
                        null,
                        null,
                        null,
                        null);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // Check cursor integrity
        if (data != null) {
            mFileInfo = new FileInfo(data);
            displayFileDetails(mFileInfo);
        } else {
            //TODO: Make error page?
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
