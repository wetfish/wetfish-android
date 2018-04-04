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
    // Image file switch constant
    private static final String IMAGE_FILE = "image/*";
    // Video file switch constant
    private static final String VIDEO_FILE = "video/*";

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
    // View the original image
    private FloatingActionButton mViewOriginalFile;

    /* Views */
    // File image view TODO: Impelemnt exoplayer later if video playback is desired
    private ImageView mFileView;
    // File name text view
    private TextView mFileTitleTextView;
    // File tags text view
    private TextView mFileTagsTextView;
    // File description text view
    private TextView mFileDescriptionTextView;
    // File playback length text view
    private TextView mFileViewLength;
    // File size text view
    private TextView mFileViewSize;
    // File resolution text view
    private TextView mFileViewResolution;
    // Layout include reference
    private View mIncludeLayout;

    /* Data */
    // Uri for the sent cursor
    private Uri mUri;
    // FileInfo object that holds all data
    private FileInfo mFileInfo;
    // FileInfo string that holds file location
    private String desiredFileStorageLink;
    // FileType string that holds the file extension type
    private String mFileType;

    //TODO: Later on when Video Playback is possible with exoplayer the focus feature will only be for images
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_detail);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // FAM
        mFAM = findViewById(R.id.fam_gallery_detail);

        // FABs
        //TODO: Add an upload FAB!
        mViewOriginalFile = findViewById(R.id.fab_view_original);
        mVisitFileFAB = findViewById(R.id.fab_visit_upload_link);
        mCopyFileURLFAB = findViewById(R.id.fab_copy_upload_link);
        mVisitFileDeleteFAB = findViewById(R.id.fab_visit_deletion_link);
        mCopyFileDeleteURLFAB = findViewById(R.id.fab_copy_deletion_link);

        // Intent Data
        Bundle bundle = getIntent().getExtras();
        mUri = (Uri) bundle.get(getString(R.string.file_details_key));

        // Setup FileInfo
        if (mFileInfo == null) {
            mFileInfo = new FileInfo();
        }

        // Utilize FileInfo & setup interaction listeners for the views, FAM and FABs
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
        // String to determine the appropriate Uri to use
        String desiredFileStorageLink;

        // String only present should there be an edited copy populating desiredFileStorageLink
        String originalFileStorageLink = null;

        // Boolean to determine if an edited image exists
        boolean editedFilePresent;

        // Boolean to determine if deletion links have been provided for the image
        boolean deletionLinkPresent;

        // Determine if an edited version of the file has been provided for the given database entry
        if (fileInfo.getEditedFileDeviceStorageLink() != null) {
            if (!fileInfo.getEditedFileDeviceStorageLink().isEmpty() && !fileInfo.getEditedFileDeviceStorageLink().equals("")) {
                // Setup the desiredFileStorageLink to reference the correct uri and show mViewOriginalImage FAB
                Log.d(LOG_TAG, "editedFile exists and is not empty");


                editedFilePresent = true;
                originalFileStorageLink = fileInfo.getFileDeviceStorageLink();
                desiredFileStorageLink = fileInfo.getEditedFileDeviceStorageLink();
            } else {
                // Setup the desiredFileStorageLink to reference the correct uri and hide mViewOriginalImage FAB
                Log.d(LOG_TAG, "file does not exist and/or is empty");

                editedFilePresent = false;
                desiredFileStorageLink = fileInfo.getFileDeviceStorageLink();
            }
        } else {
            // Setup the desiredFileStorageLink to reference the correct uri and hide mViewOriginalImage FAB
            Log.d(LOG_TAG, "file does not exist and/or is empty");

            editedFilePresent = false;
            desiredFileStorageLink = fileInfo.getFileDeviceStorageLink();
        }

        // Determine the mime type
        String mimeType = FileUtils.getMimeType(fileInfo.getFileExtensionType(), this);

        // Inflate the proper layout depending on the mime type
        switch (mimeType) {
            case IMAGE_FILE: // This layout is for image files
                // Reference the image layout and hide the video layout
                mIncludeLayout = findViewById(R.id.include_layout_gallery_image_detail);
                findViewById(R.id.include_layout_gallery_video_detail).setVisibility(View.GONE);

                // Views
                mFileView = mIncludeLayout.findViewById(R.id.iv_gallery_item_detail);
                mFileTitleTextView = mIncludeLayout.findViewById(R.id.tv_title);
                mFileTagsTextView = mIncludeLayout.findViewById(R.id.tv_tags);
                mFileDescriptionTextView = mIncludeLayout.findViewById(R.id.tv_description);
                mFileViewSize = mIncludeLayout.findViewById(R.id.tv_image_size);
                mFileViewResolution = mIncludeLayout.findViewById(R.id.tv_image_resolution);

                // Setup view data
                mFileViewSize.setText(FileUtils.getFileSize(Uri.parse(desiredFileStorageLink), this));
                mFileViewResolution.setText(FileUtils.getImageResolution(Uri.parse(desiredFileStorageLink), this));

                break;
            case VIDEO_FILE: // This layout is for video files
                // Reference the video layout and hide the image layout
                mIncludeLayout = findViewById(R.id.include_layout_gallery_video_detail);
                findViewById(R.id.include_layout_gallery_image_detail).setVisibility(View.GONE);

                // Views
                mFileView = mIncludeLayout.findViewById(R.id.iv_gallery_item_detail);
                mFileTitleTextView = mIncludeLayout.findViewById(R.id.tv_title);
                mFileTagsTextView = mIncludeLayout.findViewById(R.id.tv_tags);
                mFileDescriptionTextView = mIncludeLayout.findViewById(R.id.tv_description);
                mFileViewSize = mIncludeLayout.findViewById(R.id.tv_video_size);
                mFileViewLength = mIncludeLayout.findViewById(R.id.tv_video_length);

                // Setup view data
                mFileViewSize.setText(FileUtils.getFileSize(Uri.parse(desiredFileStorageLink), this));
                mFileViewLength.setText(FileUtils.getVideoLength(Uri.parse(desiredFileStorageLink), this));

                break;
            default:
                //TODO: Potentially make an error page
                break;
        }

        // See if the deletion link has been provided for the given database entry
        deletionLinkPresent = !(fileInfo.getFileWetfishDeletionLink().equals(getString(R.string.not_implemented)) ||
                !(fileInfo.getFileWetfishDeletionLink().isEmpty()) || !(fileInfo.getFileWetfishDeletionLink().equals("")));

        // Setup the interaction listeners now that the appropriate data has been received
        setupOnInteractionListeners(editedFilePresent, deletionLinkPresent, originalFileStorageLink);

        mFileType = fileInfo.getFileExtensionType();

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
                        .load(desiredFileStorageLink)
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
                //TODO: Figure out a method to better illustrate errors
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
            //TODO: Figure out a method to better illustrate errors
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

        // File storage link to be used as a passed value for the intent when the file is clicked
        this.desiredFileStorageLink = desiredFileStorageLink;

        // Show file's title, description and tags.
        mFileTitleTextView.setText(fileInfo.getFileTitle());
        mFileTagsTextView.setText(fileInfo.getFileTags());
        mFileDescriptionTextView.setText(fileInfo.getFileDescription());

    }

    private void setupOnInteractionListeners(boolean editedFilePresent, boolean deletionLinkPresent,
                                             final String originalImageStorageLink) {
        // String of the originalImageStorageLink
        final String originalDesiredFileStorageLink = originalImageStorageLink;

        // Close FAM if clicking outside of a button.
        mFAM.setClosedOnTouchOutside(true);

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
                Log.d(LOG_TAG, "File Storage Link: " + desiredFileStorageLink);
                fileProviderUri = FileProvider.getUriForFile(GalleryDetailActivity.this,
                        getString(R.string.file_provider_authority),
                        new File(desiredFileStorageLink));

                // Setup the data and type
                // Appropriately determine mime type for the file
                selectViewingApp.setDataAndType(fileProviderUri, FileUtils.getMimeType(mFileType, GalleryDetailActivity.this));

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

        // Setup mViewOriginalFile FAB if an edited file is present
        if (editedFilePresent && originalDesiredFileStorageLink != null) {
            // Edited file is present
            mViewOriginalFile.setVisibility(View.VISIBLE);

            mViewOriginalFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Intent to find proper app to open file
                    Intent selectViewingApp = new Intent();
                    selectViewingApp.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    selectViewingApp.setAction(Intent.ACTION_VIEW);

                    // Uri path to the file
                    Uri fileProviderUri;

                    // Use FileProvider to get an appropriate URI compatible with version Nougat+
                    Log.d(LOG_TAG, "File Storage Link: " + originalDesiredFileStorageLink);
                    fileProviderUri = FileProvider.getUriForFile(GalleryDetailActivity.this,
                            getString(R.string.file_provider_authority),
                            new File(originalDesiredFileStorageLink));

                    // Setup the data and type
                    // Appropriately determine mime type for the file
                    selectViewingApp.setDataAndType(fileProviderUri, FileUtils.getMimeType(mFileType, GalleryDetailActivity.this));

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
        } else {
            // Edited file is not present
            mViewOriginalFile.setVisibility(View.GONE);
        }


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

        if (deletionLinkPresent) {
            // Deletion link is present, show the FABs
            mVisitFileDeleteFAB.setVisibility(View.VISIBLE);
            mCopyFileDeleteURLFAB.setVisibility(View.VISIBLE);

            // Setup onClickListeners for the deletion options
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
        } else {
            // Deletion link not present, hide the FABs
            mVisitFileDeleteFAB.setVisibility(View.GONE);
            mCopyFileDeleteURLFAB.setVisibility(View.GONE);
        }
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
