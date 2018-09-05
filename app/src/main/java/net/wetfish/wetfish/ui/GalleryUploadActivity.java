package net.wetfish.wetfish.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.ViewGroup;

import net.wetfish.wetfish.R;
import net.wetfish.wetfish.ui.viewpager.EditExifFragment;
import net.wetfish.wetfish.ui.viewpager.EditFileFragment;
import net.wetfish.wetfish.ui.viewpager.FileUploadFragment;
import net.wetfish.wetfish.utils.FileUtils;
import net.wetfish.wetfish.utils.UIUtils;

import java.util.HashMap;
import java.util.Map;

public class GalleryUploadActivity extends AppCompatActivity implements
        FileUploadFragment.UploadFragmentUriUpdate,
        EditFileFragment.OnFragmentInteractionListener,
        EditExifFragment.EditExifFragmentUriUpdate {

    // Logging Tag
    private static final String LOG_TAG = GalleryUploadActivity.class.getSimpleName();
    /* Constants */
    public static final int VIEWPAGER_OFF_SCREEN_PAGE_LIMIT = 2;
    public static final int VIEWPAGER_UPLOAD_FRAGMENT = 0;
    public static final int VIEWPAGER_EDIT_EXIF_FRAGMENT = 1;
    private static final int VIEWPAGER_EDIT_FILE_FRAGMENT = 2;
    // View Variables
    private TabLayout tabLayout;
    private ViewPager mViewPager;

    // Data Variables
    private Uri fileUri;
    private Uri mEditedFileUri;

//    private boolean videoFile;

    // ViewPager Variables
    public SectionsPagerAdapter mSectionsPagerAdapter;

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //TODO: Possibly remove
//    private boolean imageFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_upload);

        // Get intent & receive data
        Intent intent = getIntent();

        // Gather intent data
        if (intent.getData() != null) {
            // Handle intents with image data from Wetfish app explicit intent...
            fileUri = getIntent().getData();
        } else if (intent.getExtras().get(Intent.EXTRA_STREAM) != null) {
            // Handle intents with image data from share smenu implicit intents...
            fileUri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
            fileUri = Uri.parse(FileUtils.getAbsolutePathFromUri(this, fileUri));
            Log.d(LOG_TAG, "File Data URI: " + fileUri.toString());
        } else {
            Log.d(LOG_TAG, "Bundle returned null");
            Snackbar.make(findViewById(android.R.id.content), R.string.sb_unable_to_obtain_file, Snackbar.LENGTH_LONG).show();

//            UIUtils.generateSnackbar(getApplicationContext(), findViewById(android.R.id.content),
//                    "Unable to obtain chosen file", Snackbar.LENGTH_LONG);
        }
        // Set view data
        if (fileUri != null) {
            Log.d(LOG_TAG, "File Data URI: " + fileUri.toString());
        } else {
            Log.d(LOG_TAG, "mFileUri returned null");
            UIUtils.generateSnackbar(getApplicationContext(), findViewById(android.R.id.content),
                    "File location was not found", Snackbar.LENGTH_LONG);
        }

        // Setup ViewPager & ViewPager's adapter
        mViewPager = findViewById(R.id.vp_gallery_detail);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), fileUri, this);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Setup off screen page limit
        mViewPager.setOffscreenPageLimit(VIEWPAGER_OFF_SCREEN_PAGE_LIMIT);

        // Setup TabLayout to interact with ViewPager
        tabLayout = findViewById(R.id.tl_gallery_detail);
        tabLayout.setupWithViewPager(mViewPager);

        //Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public void onEditFileFragmentInteraction(Uri uri) {

    }

    /**
     * Fragment interaction from @{@link EditExifFragment} to the other viewpager fragments
     *
     *
     * @param editedFileUri Uri of an edited image
     */
    @Override
    public void editExifTransferEditedUri(Uri editedFileUri) {
        FileUploadFragment uploadFragment = (FileUploadFragment) mSectionsPagerAdapter.getFragment(VIEWPAGER_UPLOAD_FRAGMENT);
        uploadFragment.receiveEditExifFragmentData(editedFileUri);
    }

    /**
     * Fragment interaction from @{@link FileUploadFragment} to the other viewpager fragments
     *
     *
     * @param editedFileUri Uri of an edited image
     */
    @Override
    public void uploadTransferEditedUri(Uri editedFileUri) {
        EditExifFragment editExifFragment = (EditExifFragment) mSectionsPagerAdapter.getFragment(VIEWPAGER_EDIT_EXIF_FRAGMENT);
        editExifFragment.receiveUploadFragmentData(editedFileUri);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        // Constants
        private static final int PAGE_COUNT = 3;

        /* Data */
        //Data from intent
        Uri mFileUri;
        // Store fragment tags
        private Map<Integer, String> mFragmentTags;
        // Fragment Manager
        private FragmentManager mFragmentManager;

        // Context
        Context mContext;

        // Title and Image arrays for tabs
        private String tabTitles[] = new String[] {
                getString(R.string.tv_title_upload),
                getString(R.string.tv_title_edit_exif),
                getString(R.string.tv_title_edit_file)};

        private int[] imageResId = {
                R.drawable.ic_upload_file_white_24dp,
                R.drawable.ic_exif_edit,
                R.drawable.ic_file_edit};

        // Default constructor
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // Custom constructor
        SectionsPagerAdapter(FragmentManager fm, Uri fileUri, Context context) {
            super(fm);
            mFileUri = fileUri;
            mEditedFileUri = Uri.parse(""); // Placeholder value
            mContext = context;
            mFragmentManager = fm;
            mFragmentTags = new HashMap<Integer, String>();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Object obj = super.instantiateItem(container, position);
            if (obj instanceof Fragment) {
                Fragment fragment = (Fragment) obj;
                String tag = fragment.getTag();
                mFragmentTags.put(position, tag);
            }
            return obj;
        }

        public Fragment getFragment (int position) {
            String tag = mFragmentTags.get(position);
            if (tag == null) {
                return null;
            }
            return mFragmentManager.findFragmentByTag(tag);
        }

        /**
         * When clicking upon tabs within TabLayout generate the appropriate fragment
         *
         * @param position Position of the clicked tab
         * @return given case fragment
         */
        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return FileUploadFragment.newInstance(mEditedFileUri, mFileUri);
                case 1:
                    //TODO: Implement EXIF editing
                    return EditExifFragment.newInstance(mEditedFileUri, mFileUri);
                case 2:
                    //TODO: Implement File editing
                    return EditFileFragment.newInstance("Cat", "Cat");
            }
            Log.d(LOG_TAG, "Something went wrong");
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return PAGE_COUNT;
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
        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            Drawable image = mContext.getResources().getDrawable(imageResId[position]);
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());

            // Replace blank spaces with image icon
            SpannableString spannableString = new SpannableString("   " + tabTitles[position]);
            ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
            spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannableString;
        }
    }
}
