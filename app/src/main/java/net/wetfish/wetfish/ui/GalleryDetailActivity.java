package net.wetfish.wetfish.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
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

    /* Constants */
    // Logging Tag
    private static final String LOG_TAG = GalleryDetailActivity.class.getSimpleName();
    // Loader ID
    private static final int FILES_DETAIL_LOADER = 1;
    // Bundle key to save instance state
    private static final String BUNDLE_KEY = "fileInfoKey";

    /* FAM & FABs */
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

    /* Views */
    // File image view TODO: Impelemnt exoplayer later if video playback is desired
    private ImageView fileView;
    // File name text view
    private TextView fileTitleTextView;
    // File tags text view
    private TextView fileTagsTextView;
    // File description text view
    private TextView fileDescriptionTextView;
    // Layout include reference
    private View includeLayout;
    // Layout include content reference
    private View galleryDetailContent;

    /* Data */
    // Uri for the sent cursor
    private Uri mUri;
    // FileInfo object that holds all data
    private FileInfo mFiileInfo;

    /* Animator Variables */
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;


    //TODO: Later on when Video Playback is possible with exoplayer the focus feature will only be for images
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_detail);

        // Reference included layout
        includeLayout = findViewById(R.id.include_layout_gallery_detail);

        // Reference Gallery Detail include layout content
        galleryDetailContent = includeLayout.findViewById(R.id.gallery_detail_content_container);

        // Views
        fileView = includeLayout.findViewById(R.id.iv_gallery_item_detail);
        fileTitleTextView = includeLayout.findViewById(R.id.tv_title);
        fileTagsTextView = includeLayout.findViewById(R.id.tv_tags);
        fileDescriptionTextView = includeLayout.findViewById(R.id.tv_description);

        // Setup Animator
        fileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                magnifyImage(fileView, fileView.getDrawable());
            }
        });

        // Set animation duration to the system's short animation time
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // FAM
        fileFAM = findViewById(R.id.fam_gallery_detail);

        // FABs
        //TODO: Add an upload FAB!
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
                webIntent.setData(Uri.parse(mFiileInfo.getFileWetfishStorageLink()));

                // Start intent
                startActivity(webIntent);
            }
        });

        copyFileURLFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Allow the link to be copied to the clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Uploaded File Url", mFiileInfo.getFileWetfishStorageLink()));
            }
        });

        visitFileDeleteFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent to visit webpage
                Intent webIntent = new Intent(Intent.ACTION_VIEW);

                // Link data
                webIntent.setData(Uri.parse(mFiileInfo.getFileWetfishDeletionLink()));

                // Start intent
                startActivity(webIntent);
            }
        });

        copyFileDeleteURLFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Allow link to be copied to the clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Uploaded File Url", mFiileInfo.getFileWetfishDeletionLink()));
            }
        });

        // Get intent data
        Bundle bundle = getIntent().getExtras();
        mUri = (Uri) bundle.get(getString(R.string.file_details));

        // Setup FileInfo
        if (mFiileInfo == null) {
            mFiileInfo = new FileInfo();
        }

        getLoaderManager().initLoader(FILES_DETAIL_LOADER, null, this);
    }

    /**
     * Method to magnify the image if clicked
     *
     * @param fileImageView the smaller image view
     * @param drawable the image within the view
     */
    private void magnifyImage(final View fileImageView, Drawable drawable) {
        // If an animation is in progress cancel it an proceed with the new one
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load high-res image
        final ImageView focusedFileImageView = (ImageView) includeLayout.findViewById(R.id.expanded_image);
        focusedFileImageView.setImageDrawable(drawable);

        // Calculate start and end bounds for the image.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // Start bounds are the visible rectangle of the thumbnail whlie the final bounds
        // are the visible rectangle of the container view. We set the container view's offset as
        // the origin for the bounds since that's the origin for the positioning animation properties.
        // (X, Y).
        fileImageView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.gallery_detail_container).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust start bounds to be the same aspect ratio as the final bounds with center crop.
        // Stretching prevents stretching during the animation. Calculate the start scaling factor.
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {

            // Extend start bounds horizontally off the start scale
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically off the start scale
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail view and show the focused view. When the animation begins it will
        // position the focused view in place of the thumbnail.
        fileImageView.setAlpha(0f);
        focusedFileImageView.setVisibility(View.VISIBLE);

        // Turn off clicking for the smaller view of the file to allow proper focusing of the image
        // and dim the background
        fileImageView.setClickable(false);

        // Pivot point of the SCALE_X and SCALE_Y transformations are set to the top-left corner
        // of the focused view instead of the center (default).
        focusedFileImageView.setPivotX(0f);
        focusedFileImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and scale properties
        // (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(focusedFileImageView, View.X, startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(focusedFileImageView, View.Y, startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(focusedFileImageView, View.SCALE_X, startScale, 1f))
                .with(ObjectAnimator.ofFloat(focusedFileImageView, View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ViewCompat.setTranslationZ(focusedFileImageView, 5);
                galleryDetailContent.setAlpha(.5f);
                fileImageView.setAlpha(0f);
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                ViewCompat.setTranslationZ(focusedFileImageView, 5);
                galleryDetailContent.setAlpha(.5f);
                fileImageView.setAlpha(0f);
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the focused image, it should zoom back down to the original bounds,
        // revealing the smaller image.
        final float startScaleFinal = startScale;
        focusedFileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                //Animate the four positioning/sizing properties in parallel back to their original values
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator.ofFloat(focusedFileImageView, View.X, startBounds.left))
                        .with(ObjectAnimator.ofFloat(focusedFileImageView, View.Y, startBounds.top))
                        .with(ObjectAnimator.ofFloat(focusedFileImageView, View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator.ofFloat(focusedFileImageView, View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ViewCompat.setTranslationZ(focusedFileImageView, 5);
                        galleryDetailContent.setAlpha(1f);
                        fileImageView.setAlpha(1f);
                        fileImageView.setClickable(true);
                        focusedFileImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        ViewCompat.setTranslationZ(focusedFileImageView, 5);
                        galleryDetailContent.setAlpha(1f);
                        fileImageView.setAlpha(1f);
                        fileImageView.setClickable(true);
                        focusedFileImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });

                set.start();
                mCurrentAnimator = set;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFiileInfo != null && mFiileInfo.getFileInfoInitialized()) {
            getIntent().putExtra(BUNDLE_KEY, Parcels.wrap(mFiileInfo));
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
            mFiileInfo = fileInfo;
            displayFileDetails(mFiileInfo);
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
                .apply(RequestOptions.fitCenterTransform())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(fileView);

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
            mFiileInfo = new FileInfo(data);
            displayFileDetails(mFiileInfo);
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
