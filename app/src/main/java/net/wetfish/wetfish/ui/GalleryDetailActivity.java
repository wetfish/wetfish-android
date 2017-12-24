package net.wetfish.wetfish.ui;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import net.wetfish.wetfish.R;
import net.wetfish.wetfish.data.FileInfo;

import org.parceler.Parcels;

public class GalleryDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    //Constants
    // Logging Tag
    private static final String LOG_TAG = GalleryDetailActivity.class.getSimpleName();
    // Loader ID
    private static final int FILES_DETAIL_LOADER = 1;
    // Bundle key to save instance state
    private static final String BUNDLE_KEY = "fileInfoKey";

    // FAM & FABs
    // Display FABs
    private FloatingActionMenu fileFAM;
    // Visit file URL
    private FloatingActionButton visitFileFAB;
    // Copy visit file URL
    private FloatingActionButton copyFileURLFAB;
    // Visit delete file URL
    private FloatingActionButton visitFileDeleteFAB;
    // Copy visit delete file URL
    private FloatingActionButton copyFileDeleteURLFAB;

    // Views
    // File image view TODO: Impelemnt exoplayer later if video playback is desired
    private ImageView fileImageView;
    // File name text view
    private TextView fileTitleTextView;
    // File tags text view
    private TextView fileTagsTextView;
    // File description text view
    private TextView fileDescriptionTextView;

    // Data
    // Uri for the sent cursor
    private Uri mUri;
    // FileInfo object that holds all data
    private FileInfo fileInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_detail);

        // Reference included layout
        View includeLayout = findViewById(R.id.include_layout_gallery_detail);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // FAM
        fileFAM = findViewById(R.id.fam_gallery_detail);

        // FABs
        visitFileFAB = findViewById(R.id.fab_visit_upload_link);
        copyFileURLFAB = findViewById(R.id.fab_copy_upload_link);
        visitFileDeleteFAB = findViewById(R.id.fab_visit_deletion_link);
        copyFileDeleteURLFAB = findViewById(R.id.fab_copy_deletion_link);

        visitFileFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Intent to visit webpage
                Intent webIntent = new Intent(Intent.ACTION_VIEW);

                // Link data
                webIntent.setData(Uri.parse(fileInfo.getFileWetfishStorageLink()));

                // Start intent
                startActivity(webIntent);
            }
        });

        copyFileURLFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Allow the link to be copied to the clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Uploaded File Url", fileInfo.getFileWetfishStorageLink()));
            }
        });

        visitFileDeleteFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Intent to visit webpage
                Intent webIntent = new Intent(Intent.ACTION_VIEW);

                // Link data
                webIntent.setData(Uri.parse(fileInfo.getFileWetfishDeletionLink()));

                // Start intent
                startActivity(webIntent);
            }
        });


        copyFileDeleteURLFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Allow link to be copied to the clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Uploaded File Url", fileInfo.getFileWetfishDeletionLink()));
            }
        });

        // ImageViews
        fileImageView = includeLayout.findViewById(R.id.iv_gallery_item_detail);

        // TextViews
        fileTitleTextView = includeLayout.findViewById(R.id.tv_title);
        fileTagsTextView = includeLayout.findViewById(R.id.tv_tags);
        fileDescriptionTextView = includeLayout.findViewById(R.id.tv_description);

        Button button = includeLayout.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayFileDetails(fileInfo);
                Log.d(LOG_TAG, "BOOM: " + fileInfo.getFileDeviceStorageLink() +
                        fileInfo.getFileUploadTime() + "\n\n" + fileInfo.getFileWetfishStorageLink());
            }
        });

        // Get intent data
        Bundle bundle = getIntent().getExtras();
        mUri = (Uri) bundle.get(getString(R.string.file_details));

        getLoaderManager().initLoader(FILES_DETAIL_LOADER, null, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getIntent().putExtra(BUNDLE_KEY, Parcels.wrap(fileInfo));
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
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            FileInfo fileInfo = Parcels.unwrap(bundle.getParcelable(BUNDLE_KEY));
            if (fileInfo != null) {
                this.fileInfo = fileInfo;
                displayFileDetails(this.fileInfo);
            }
        }
    }

    public void displayFileDetails(FileInfo fileInfo) {

        // Should deletion not yet be available, hide these options from view
        if (fileInfo.getFileWetfishDeletionLink().equals(getString(R.string.not_implemented))) {
            visitFileDeleteFAB.setVisibility(View.GONE);
            copyFileDeleteURLFAB.setVisibility(View.GONE);
        }

        // Setup view data
        Glide.with(this)
                .load(fileInfo.getFileWetfishStorageLink()) //TODO: Do file storage first
                .apply(RequestOptions.centerCropTransform())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(fileImageView);

        fileTitleTextView.setText(fileInfo.getFileTitle());
        fileTagsTextView.setText(fileInfo.getFileTags());
        fileDescriptionTextView.setText(fileInfo.getFileDescription());

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
            fileInfo = new FileInfo(data);
            displayFileDetails(fileInfo);
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
