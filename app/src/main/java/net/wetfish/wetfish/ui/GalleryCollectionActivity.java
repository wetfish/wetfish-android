package net.wetfish.wetfish.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import net.wetfish.wetfish.R;
import net.wetfish.wetfish.data.FileContract.FileColumns;
import net.wetfish.wetfish.data.FileContract.Files;
import net.wetfish.wetfish.data.FileInfo;
import net.wetfish.wetfish.utils.FileUtils;

import java.io.File;

/**
 * Created by ${Michael} on 4/2/2018.
 */
public class GalleryCollectionActivity extends AppCompatActivity {

    /* Constants */
    // Logging Tag
    private static final String LOG_TAG = GalleryCollectionActivity.class.getSimpleName();
    // Bundle key to save instance state
    private static final String BUNDLE_KEY_FILE_URI = "fileInfoKey";
    private static final String BUNDLE_KEY_ADAPTER_POSITION = "adapterPositionKey";
    private static final String BUNDLE_KEY_SORTING_PREF = "sortingPreferenceKey";

    /* Adapter */
    /**
     * Pager adapter will provide fragments to represent our gallery while the FragmentStatePagerAdapter
     * will destroy and re-create fragments while saving and restoring their state in the process
     */
    GalleryCollectionPagerAdapter mGalleryCollectionPagerAdapter;

    /* View Pager */
    // View pager to display object collections
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_collection);

        // Intent Data
        Bundle bundle = getIntent().getExtras();
        int startingInt = (int) bundle.get(getString(R.string.file_position_key));
        Log.d(LOG_TAG, "Here's The Starting Int: " + startingInt);

        // Pass preferences here to do as little work as possible within the fragments.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        boolean sortByMostRecentSetting = sharedPref.getBoolean(getString(R.string.pref_sortByMostRecent_key),
                getResources().getBoolean(R.bool.pref_sortByMostRecent_default_value));

        // Create the adapter that will return a fragment with an object in the collection.
        mGalleryCollectionPagerAdapter = new GalleryCollectionPagerAdapter(getSupportFragmentManager(),
                this, sortByMostRecentSetting);

        Toolbar toolbar = findViewById(R.id.toolbar);

        // Setup the ViewPager
        mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(mGalleryCollectionPagerAdapter);
        mViewPager.setCurrentItem(startingInt);
        mViewPager.setOffscreenPageLimit(1);
    }

    public static class GalleryCollectionPagerAdapter extends FragmentStatePagerAdapter {

        /* Constants */
        // Logging Tag
        private static final String LOG_TAG = GalleryCollectionActivity.class.getSimpleName();
        // Mitigate the 0 to lineup with the database
        private static int ADD_ONE_TO_MITIGATE_ZERO = 1;
        // Context
        private Context mContext;
        // Sort By Most Recent sorting method
        private boolean mSortByMostRecent;

        GalleryCollectionPagerAdapter(FragmentManager fm, Context context, boolean sortByMostRecentSetting) {
            super(fm);
            mContext = context;
            mSortByMostRecent = sortByMostRecentSetting;
        }

        /**
         * Return the Fragment associated with a specified position.
         *
         * @param position of item
         */
        @Override
        public Fragment getItem(int position) {
            // Create our fragment
            Fragment fragment = new GalleryObjectFragment();

            // Create bundle to send data in
            Bundle args = new Bundle();

            // Gather file info and store within bundle and bundle within fragment
            Uri fileInfoUri = FileUtils.getFileUri(position + ADD_ONE_TO_MITIGATE_ZERO);
            args.putString(BUNDLE_KEY_FILE_URI, fileInfoUri.toString());
            args.putInt(BUNDLE_KEY_ADAPTER_POSITION, position);
            args.putBoolean(BUNDLE_KEY_SORTING_PREF, mSortByMostRecent);
            fragment.setArguments(args);

            // Return the fragment with file data
            return fragment;
        }

        /**
         * Return the number of views available.
         */
        @Override
        public int getCount() {
            // Gather file data and the amount of entries within the preceding cursor
            Cursor filesData = null;

            try {
                filesData = mContext.getContentResolver().query(Files.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);

                if (filesData != null) {
                    // Gather amount of entries within the cursor
                    int amountOfEntries = filesData.getCount();

                    // Close cursor
                    filesData.close();

                    // Return the amount of entries
                    return amountOfEntries;
                } else {
                    // Close cursor
                    if (filesData != null) {
                        filesData.close();
                    }

                    // Cursor was null, no entries
                    return 0;
                }
            } finally {
                if (filesData != null) {
                    // Gather amount of entries
                    int amountOfEntries = filesData.getCount();

                    // Close cursor
                    filesData.close();

                    // Return entries if there are more than 0, otherwise, 0.
                    if (amountOfEntries > 0) {
                        return amountOfEntries;
                    } else {
                        return 0;

                    }
                }
            }
        }

        /**
         * This method may be called by the ViewPager to obtain a title string
         * to describe the specified page. This method may return null
         * indicating no title for this page. The default implementation returns
         * null.
         *
         * @param position The position of the title requested
         * @return A title for the requested page
         */
        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            // Return the title of the gallery item
            return "Gallery Item " + (position + ADD_ONE_TO_MITIGATE_ZERO);
        }
    }

    public static class GalleryObjectFragment extends Fragment implements
            LoaderManager.LoaderCallbacks<Cursor> {
        // Loader ID
        private static final int FILES_DETAIL_LOADER = 1;
        // Bundle keys
        private static final String BUNDLE_KEY_FILE_URI = "fileInfoKey";
        private static final String BUNDLE_KEY_ADAPTER_POSITION = "adapterPositionKey";
        private static final String BUNDLE_KEY_SORTING_PREF = "sortingPreferenceKey";
        // Image file switch constant
        private static final String IMAGE_FILE = "image/*";
        // Video file switch constant
        private static final String VIDEO_FILE = "video/*";
        /* Constants */
        // Logging Tag
        private final String LOG_TAG = GalleryObjectFragment.class.getSimpleName();
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
        // File image view TODO: Implement exoplayer later if video playback is desired
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
        // File presence on file system
        private View mFileViewPresentOnSystem;
        // Layout include reference
        private View mIncludeLayout;
        // Layout rootView
        private View mRootView;

        /* Data */
        // Uri for the sent cursor
        private Uri mUri;
        // FileInfo object that holds all data
        private FileInfo mFileInfo;
        // String to hold the desired storage link to use
        private String mDesiredFileStorageLink;
        // String to hold the original file storage link
        private String mOriginalFileStorageLink;
        // String to hold the edited file storage link
        private String mEditedFileStorageLink;
        // String to hold the wetfish file storage link
        private String mWetfishFileStorageLink;
        // FileType string that holds the file extension type
        private String mFileType;
        // Sorting Settings
        private boolean mSortByMostRecent;
        // Starting position of adapter
        private int mAdapterPosition;
        // Has this fragment been created? if so, keep from creating another async task
        private boolean mFragmentCreated;
        // Network Info
        private NetworkInfo mNetworkInfo;
        // Boolean if the original file exists
        private boolean mOriginalFilePresent;
        // Boolean if the edited file exists
        private boolean mEditedFilePresent;
        // Connectivity manager
        ConnectivityManager mConnectivityManager;
        //

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            mRootView = inflater.inflate(R.layout.fragment_gallery_collection, container, false);
            // Network Info
            ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            mNetworkInfo = cm.getActiveNetworkInfo();

            // FAM
            mFAM = mRootView.findViewById(R.id.fam_gallery_detail);

            // FABs
            //TODO: Add an upload FAB!
            mViewOriginalFile = mRootView.findViewById(R.id.fab_view_original);
            mVisitFileFAB = mRootView.findViewById(R.id.fab_visit_upload_link);
            mCopyFileURLFAB = mRootView.findViewById(R.id.fab_copy_upload_link);
            mVisitFileDeleteFAB = mRootView.findViewById(R.id.fab_visit_deletion_link);
            mCopyFileDeleteURLFAB = mRootView.findViewById(R.id.fab_copy_deletion_link);

            // Gather bundle data
            Bundle args = getArguments();
            mAdapterPosition = args.getInt(BUNDLE_KEY_ADAPTER_POSITION);
            mSortByMostRecent = args.getBoolean(BUNDLE_KEY_SORTING_PREF);
            mUri = Uri.parse(args.getString(BUNDLE_KEY_FILE_URI));

            // Utilize FileInfo & setup interaction listeners for the views, FAM and FABs

            LoaderManager loaderManager = getLoaderManager();
            Loader<Object> fileLoader = loaderManager.getLoader(FILES_DETAIL_LOADER);

            if (!mFragmentCreated) {
                // If loader doesn't exist
                if (fileLoader == null) {
                    // Initialize loader
                    Log.d(LOG_TAG, "Initialize Loader");
                    loaderManager.initLoader(FILES_DETAIL_LOADER, null, this).forceLoad();
                } else {
                    // Restart loader
                    loaderManager.restartLoader(FILES_DETAIL_LOADER, null, this).forceLoad();
                }
            }
            return mRootView;
        }

        public void displayFileDetails(FileInfo fileInfo) {
            // String only present should there be an edited copy populating desiredFileStorageLink

            // Boolean to determine if deletion links have been provided for the image
            boolean deletionLinkPresent;

            // Gather the file info Uris and links
            mOriginalFileStorageLink = mFileInfo.getFileDeviceStorageLink();
            mEditedFileStorageLink = mFileInfo.getEditedFileDeviceStorageLink();
            mWetfishFileStorageLink = mFileInfo.getFileWetfishStorageLink();

            // Boolean values to determine if a file exists or not
             mOriginalFilePresent = FileUtils.checkIfFileExists(mOriginalFileStorageLink);
             mEditedFilePresent = FileUtils.checkIfFileExists(mEditedFileStorageLink);

            // Determine the mime type
            String mimeType = FileUtils.getMimeType(fileInfo.getFileExtensionType(), getContext());

            // Inflate the proper layout depending on the mime type
            switch (mimeType) {
                case IMAGE_FILE: // This layout is for image files
                    // Reference the image layout and hide the video layout
                    mIncludeLayout = mRootView.findViewById(R.id.include_fragment_gallery_collection_object_image);
                    mRootView.findViewById(R.id.include_fragment_gallery_collection_object_video).setVisibility(View.GONE);

                    // Views
                    mFileView = mIncludeLayout.findViewById(R.id.iv_gallery_item_detail);
                    mFileTitleTextView = mIncludeLayout.findViewById(R.id.tv_title);
                    mFileTagsTextView = mIncludeLayout.findViewById(R.id.tv_tags);
                    mFileDescriptionTextView = mIncludeLayout.findViewById(R.id.tv_description);
                    mFileViewSize = mIncludeLayout.findViewById(R.id.tv_image_size);
                    mFileViewResolution = mIncludeLayout.findViewById(R.id.tv_image_resolution);
                    mFileViewPresentOnSystem = mIncludeLayout.findViewById(R.id.tv_image_not_on_local_file_system);

                    // Setup view data
                    if (mEditedFilePresent) {
                        mFileViewSize.setText(FileUtils.getFileSize(Uri.parse(mEditedFileStorageLink), getContext()));
                        mFileViewResolution.setText(FileUtils.getImageResolution(Uri.parse(mEditedFileStorageLink), getContext()));
                    } else if (mOriginalFilePresent){
                        mFileViewSize.setText(FileUtils.getFileSize(Uri.parse(mOriginalFileStorageLink), getContext()));
                        mFileViewResolution.setText(FileUtils.getImageResolution(Uri.parse(mOriginalFileStorageLink), getContext()));
                    } else {
                        mFileViewSize.setVisibility(View.GONE);
                        mFileViewResolution.setVisibility(View.GONE);
                        mFileViewPresentOnSystem.setVisibility(View.VISIBLE);
                    }


                    break;
                case VIDEO_FILE: // This layout is for video files
                    // Reference the video layout and hide the image layout
                    mIncludeLayout = mRootView.findViewById(R.id.include_fragment_gallery_collection_object_video);
                    mRootView.findViewById(R.id.include_fragment_gallery_collection_object_image).setVisibility(View.GONE);

                    // Views
                    mFileView = mIncludeLayout.findViewById(R.id.iv_gallery_item_detail);
                    mFileTitleTextView = mIncludeLayout.findViewById(R.id.tv_title);
                    mFileTagsTextView = mIncludeLayout.findViewById(R.id.tv_tags);
                    mFileDescriptionTextView = mIncludeLayout.findViewById(R.id.tv_description);
                    mFileViewSize = mIncludeLayout.findViewById(R.id.tv_video_size);
                    mFileViewLength = mIncludeLayout.findViewById(R.id.tv_video_length);

                    // Setup view data
                    if (mEditedFilePresent) {
                        mFileViewSize.setText(FileUtils.getFileSize(Uri.parse(mEditedFileStorageLink), getContext()));
                        mFileViewLength.setText(FileUtils.getVideoLength(Uri.parse(mEditedFileStorageLink), getContext()));
                    } else if (mOriginalFilePresent){
                        mFileViewSize.setText(FileUtils.getFileSize(Uri.parse(mOriginalFileStorageLink), getContext()));
                        mFileViewLength.setText(FileUtils.getVideoLength(Uri.parse(mOriginalFileStorageLink), getContext()));
                    } else {
                        mFileViewSize.setVisibility(View.GONE);
                        mFileViewLength.setVisibility(View.GONE);
                        mFileViewPresentOnSystem.setVisibility(View.VISIBLE);
                    }
                    break;
                default:
                    //TODO: Potentially make an error page
                    break;
            }

            // See if the deletion link has been provided for the given database entry
            deletionLinkPresent = !(fileInfo.getFileWetfishDeletionLink().equals(getString(R.string.not_implemented)) ||
                    !(fileInfo.getFileWetfishDeletionLink().isEmpty()) || !(fileInfo.getFileWetfishDeletionLink().equals("")));

            // Setup the interaction listeners now that the appropriate data has been received
            setupOnInteractionListeners(deletionLinkPresent);

            mFileType = fileInfo.getFileExtensionType();

            mConnectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();

            if (mNetworkInfo != null && mNetworkInfo.isConnected()) {
                // Check to see if the file is representable by glide
                if (FileUtils.representableByGlide(mFileType)) {
                        // Load the edited file from the local storage if possible, then move down the options
                        Glide.with(this)
                                .load(mEditedFileStorageLink)
                                .error(Glide.with(this)
                                        .load(mOriginalFileStorageLink)
                                        .apply(RequestOptions.fitCenterTransform()))
                                .error(Glide.with(this)
                                        .load(mWetfishFileStorageLink)
                                        .apply(RequestOptions.fitCenterTransform()))
                                .error(Glide.with(this)
                                        .load(R.drawable.glide_file_not_found_anywhere)
                                        .apply(RequestOptions.fitCenterTransform()))
                                .apply(RequestOptions.placeholderOf(new ColorDrawable(Color.DKGRAY)))
                                .apply(RequestOptions.fitCenterTransform())
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(mFileView);
                } else { // FileUtils.representableByGlide(mFileType) else
                    Log.d(LOG_TAG, "File is not representable by glide");
                    // If the file is not representable by glide depict this to the user
                    Glide.with(this)
                            .load(R.drawable.glide_not_representable)
                            .apply(RequestOptions.placeholderOf(new ColorDrawable(Color.DKGRAY)))
                            .apply(RequestOptions.fitCenterTransform())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(mFileView);
                }
            } else { // mNetworkInfo != null && mNetworkInfo.isConnected() else
                if (FileUtils.representableByGlide(mFileType)) {
                        Log.d(LOG_TAG, "No network, edited file present");
                        // Load the desired file storage link first, then
                        Glide.with(this)
                                .load(mEditedFileStorageLink)
                                .error(Glide.with(this)
                                        .load(mOriginalFileStorageLink)
                                        .apply(RequestOptions.fitCenterTransform()))
                                .error(Glide.with(this)
                                        .load(R.drawable.glide_file_not_found_no_network)
                                        .apply(RequestOptions.fitCenterTransform()))
                                .apply(RequestOptions.placeholderOf(new ColorDrawable(Color.DKGRAY)))
                                .apply(RequestOptions.fitCenterTransform())
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(mFileView);
                } else { // FileUtils.representableByGlide(mFileType) else
                    Log.d(LOG_TAG, "File is not representable by glide");
                    // If the file is not representable by glide depict this to the user
                    Glide.with(this)
                            .load(R.drawable.glide_not_representable)
                            .apply(RequestOptions.placeholderOf(new ColorDrawable(Color.DKGRAY)))
                            .apply(RequestOptions.fitCenterTransform())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(mFileView);
                }
            }
            // Show file's title, description and tags.
            mFileTitleTextView.setText(fileInfo.getFileTitle());
            mFileTagsTextView.setText(fileInfo.getFileTags());
            mFileDescriptionTextView.setText(fileInfo.getFileDescription());

        }

        private void setupOnInteractionListeners(boolean deletionLinkPresent) {

            // Close FAM if clicking outside of a button.
            mFAM.setClosedOnTouchOutside(true);

            // Setup file interaction
            mFileView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                // Uri path to the file
                Uri fileProviderUri = null;

                // Use FileProvider to get an appropriate URI compatible with version Nougat+
                if (mEditedFilePresent) {
                    fileProviderUri = FileProvider.getUriForFile(getContext(),
                            getString(R.string.file_provider_authority),
                            new File(mEditedFileStorageLink));
                } else if (mOriginalFilePresent) {
                    fileProviderUri = FileProvider.getUriForFile(getContext(),
                            getString(R.string.file_provider_authority),
                            new File(mOriginalFileStorageLink));
                }

                // If file provider doesn't result in null open the file on the system, otherwise use Wetfish
                if (fileProviderUri != null) {
                    // Intent to find proper app to open file
                    Intent selectViewingApp = new Intent();
                    selectViewingApp.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    selectViewingApp.setAction(Intent.ACTION_VIEW);

                    // Setup the data and type
                    // Appropriately determine mime type for the file
                    selectViewingApp.setDataAndType(fileProviderUri, FileUtils.getMimeType(mFileType, getContext()));

                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                        selectViewingApp.setClipData(ClipData.newRawUri("", fileProviderUri));
                        selectViewingApp.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }

                    // Check to see if an app can open this file. If so, do so, if not, inform the user
                    PackageManager packageManager = getContext().getPackageManager();
                    if (selectViewingApp.resolveActivity(packageManager) != null) {
                        startActivity(selectViewingApp);
                    } else {
                        Snackbar.make(mIncludeLayout, R.string.sb_no_app_available, Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    // Intent to find proper app to open file.
                    if (mNetworkInfo != null && mNetworkInfo.isConnected()) {
                        // If the file doesn't exist on the local file system and the user is connected to the internet, load from Wetfish
                        Intent selectViewingApp = new Intent(Intent.ACTION_VIEW, Uri.parse(mFileInfo.fileWetfishStorageLink));
                        PackageManager packageManager = getContext().getPackageManager();
                        if (selectViewingApp.resolveActivity(packageManager) != null) {
                            startActivity(selectViewingApp);
                        } else {
                            Snackbar.make(mIncludeLayout, R.string.sb_no_app_available, Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        Snackbar.make(mIncludeLayout, R.string.sb_network_not_connected, Snackbar.LENGTH_LONG).show();
                    }

                }

            }
        });

            // Setup mViewOriginalFile FAB if an edited file is present
            if (mEditedFilePresent) {
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
                        fileProviderUri = FileProvider.getUriForFile(getContext(),
                                getString(R.string.file_provider_authority),
                                new File(mEditedFileStorageLink));

                        // Setup the data and type
                        // Appropriately determine mime type for the file
                        selectViewingApp.setDataAndType(fileProviderUri, FileUtils.getMimeType(mFileType, getContext()));

                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                            selectViewingApp.setClipData(ClipData.newRawUri("", fileProviderUri));
                            selectViewingApp.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }

                        // Check to see if an app can open this file. If so, do so, if not, inform the user
                        PackageManager packageManager = getContext().getPackageManager();
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
                    ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(ClipData.newPlainText("Uploaded File Url", mFileInfo.getFileWetfishStorageLink()));

                    // Split the string to obtain the link
                    String tokens[] = clipboard.getPrimaryClip().toString().split("\\{T:");
                    String tokensTwo[] = tokens[1].split("\\}");
                    String clipboardClipData = tokensTwo[0];

                    // Check to see if the clipboard data link equals the database stored link
                    if (clipboardClipData.equals(mFileInfo.getFileWetfishStorageLink())) {
                        Snackbar.make(mRootView.findViewById(android.R.id.content), R.string.sb_url_clipboard_success,
                                Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(mRootView.findViewById(android.R.id.content), R.string.sb_url_clipboard_failure,
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
                        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
                        clipboard.setPrimaryClip(ClipData.newPlainText("Uploaded File Url", mFileInfo.getFileWetfishDeletionLink()));

                        if (clipboard.getPrimaryClip().equals(mFileInfo.getFileWetfishDeletionLink())) {
                            Snackbar.make(mRootView.findViewById(android.R.id.content), R.string.sb_url_clipboard_success,
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

            return new AsyncTaskLoader<Cursor>(getContext()) {

                public void onStartLoading() {
                    forceLoad();
                }

                @Override
                public Cursor loadInBackground() {

                    String sortOrder = null;
                    // Gather the cursor at location mUri within files db with the current sorting mechanism
                    if (mSortByMostRecent) {
                        // User selected for sorting of the newest first
                        sortOrder = FileColumns.COLUMN_FILE_UPLOAD_TIME + " DESC";
                    } else {
                        // User selected for sorting of the oldest first
                        sortOrder = FileColumns.COLUMN_FILE_UPLOAD_TIME + " ASC";
                    }

                    // Return the desired data set
                    return getContext().getContentResolver().query(Files.CONTENT_URI,
                            null,
                            null,
                            null,
                            sortOrder);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            // Check cursor integrity
            try {
                if (data != null) {
                    mFileInfo = new FileInfo(data, mAdapterPosition);
                    data.close();
                    displayFileDetails(mFileInfo);
                    mFragmentCreated = true;
                } else {
                    //TODO: Make error page?
                }
            } finally {
                if (data != null) {
                    data.close();
                }
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
}