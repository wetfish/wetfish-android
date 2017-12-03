package net.wetfish.wetfish.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
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

import net.wetfish.wetfish.R;
import net.wetfish.wetfish.ui.viewpager.EditExifFragment;
import net.wetfish.wetfish.ui.viewpager.EditFileFragment;
import net.wetfish.wetfish.ui.viewpager.FileUploadFragment;
import net.wetfish.wetfish.utils.UIUtils;

public class GalleryDetailActivity extends AppCompatActivity implements
        FileUploadFragment.OnFragmentInteractionListener,
        EditFileFragment.OnFragmentInteractionListener,
        EditExifFragment.OnFragmentInteractionListener {

    // Logging Tag
    private static final String LOG_TAG = GalleryDetailActivity.class.getSimpleName();

    // View Variables
    private TabLayout tabLayout;
    private ViewPager viewPager;

    // Data Variables
    private Uri fileUri;
    //TODO: Possibly remove
//    private boolean imageFile;
//    private boolean videoFile;

    // ViewPager Variables
    private SectionsPagerAdapter mSectionsPagerAdapter;

    //TODO: Implement all this
    @Override
    public void onUploadFragmentInteraction(Uri uri) {

    }

    @Override
    public void onEditFileFragmentInteraction(Uri uri) {

    }

    @Override
    public void onEditExifFragmentInteraction(Uri uri) {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_detail);

        // Get intent & receive data
        Intent intent = getIntent();

        // Gather intent data
        if (intent.getData() != null) {
            // Handle intents with image data from Wetfish app explicit intent...
            fileUri = getIntent().getData();
        } else if (intent.getExtras().get(Intent.EXTRA_STREAM) != null) {
            // Handle intents with image data from share smenu implicit intents...
            fileUri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
            Log.d(LOG_TAG, "File Data URI: " + fileUri.toString());
        } else {
            Log.d(LOG_TAG, "Bundle returned null");
            UIUtils.generateSnackbar(getApplicationContext(), findViewById(android.R.id.content),
                    "Unable to obtain chosen file", Snackbar.LENGTH_LONG);
        }
        // Set view data
        if (fileUri != null) {
            Log.d(LOG_TAG, "File Data URI: " + fileUri.toString());
        } else {
            Log.d(LOG_TAG, "fileUri returned null");
            UIUtils.generateSnackbar(getApplicationContext(), findViewById(android.R.id.content),
                    "File location was not found", Snackbar.LENGTH_LONG);
        }

        // Setup ViewPager & ViewPager's adapter
        viewPager = (ViewPager) findViewById(R.id.vp_gallery_detail);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), fileUri, this);
        viewPager.setAdapter(mSectionsPagerAdapter);

        // Setup TabLayout to interact with ViewPager
        tabLayout = (TabLayout) findViewById(R.id.tl_gallery_detail);
        tabLayout.setupWithViewPager(viewPager);

        //Setup Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        // Constants
        private static final int PAGE_COUNT = 3;

        // Data from intent
        Uri fileUri;

        // Context
        Context mContext;

        // Title and Image arrays for tabs
        private String tabTitles[] = new String[] {
                getString(R.string.title_upload),
                getString(R.string.title_edit_exif),
                getString(R.string.title_edit_file)};

        private int[] imageResId = {
                R.drawable.ic_upload_file,
                R.drawable.ic_exif_edit,
                R.drawable.ic_file_edit};

        // Default constructor
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // Custom constructor
        public SectionsPagerAdapter(FragmentManager fm, Uri uri, Context context) {
            super(fm);
            fileUri = uri;
            mContext = context;
        }

        /**
         * When clicking upon tabs within TabLayout generate the appropriate fragment
         *
         * @param position Position of the clicked tab
         * @return
         */
        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return FileUploadFragment.newInstance(position + 1, fileUri);
                case 1:
                    //TODO: Implement EXIF editing
                    return EditExifFragment.newInstance("Cat", "Cat");
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
