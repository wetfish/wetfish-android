package net.wetfish.wetfish.ui.viewpager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;

import net.wetfish.wetfish.R;
import net.wetfish.wetfish.adapters.ExifDataAdapter;
import net.wetfish.wetfish.data.EditedFileData;
import net.wetfish.wetfish.ui.GalleryUploadActivity;
import net.wetfish.wetfish.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static net.wetfish.wetfish.utils.ExifUtils.gatherExifData;
import static net.wetfish.wetfish.utils.ExifUtils.transferEditedExifData;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditExifFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditExifFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditExifFragment extends Fragment implements FABProgressListener,
        ExifDataAdapter.ExifDataAdapterOnClickHandler {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    /* Constants */
    private static final String LOG_TAG = EditExifFragment.class.getSimpleName();
    private static final String ARG_EDITED_FILE_URI = "edited_file_uri";
    private static final String ARG_ORIGINAL_FILE_URI = "file_uri";

    /* Views */
    private RecyclerView mRecyclerView;
    private ExifDataAdapter mExifDataAdapter;
    private View mRootLayout;
    private FloatingActionButton mFabEditFileExif;
    private FABProgressCircle mFabProgressCircleEditExif;
    private CustomLockingViewPager mViewpager;
    private TabLayout mTabLayout;

    /* Data */
    private Uri mFileAbsolutePath;
    private Uri mEditedImageAbsolutePath;
    private Uri mEditedImageAbsolutePathTemp;
    private double mEditedImageQuality;
    private EditedFileData mEditedFileData;
    private ArrayList<Object> mExifDataArrayList;
    private boolean mDuplicateImageCreated;
    private boolean mCancelableCallThreadEditExif;
    private boolean mSuccessfulExifEdit;

    /* Threads */
    private Handler mCallThreadEditExif;

    /* Fragment Interaction Interfaces */
    private EditExifFragmentUriUpdate mSendUri;

    /* Fragment interaction methods */
    public void receiveUploadFragmentData(EditedFileData editedFileUri) {
        mEditedFileData = editedFileUri;
        mEditedImageAbsolutePath = editedFileUri.getEditedFileUri();
        mEditedImageQuality = editedFileUri.getRescaledImageQuality();
    }

    /**
     * Blank constructor
     */
    public EditExifFragment() {
        // Required empty public constructor
    }

    /**
     * Create an instance of {@link EditExifFragment} with the original fle Uri and edited file Uri if present
     *
     * @param editedFileUri The Uri of files edited off of the original file
     * @param fileUri       The Uri of the the original upload file
     * @return A new instance of fragment EditExifFragment.
     */
    public static EditExifFragment newInstance(Uri editedFileUri, Uri fileUri) {
        EditExifFragment fragment = new EditExifFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EDITED_FILE_URI, editedFileUri.toString());
        args.putString(ARG_ORIGINAL_FILE_URI, fileUri.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEditedImageAbsolutePath = Uri.parse(getArguments().getString(ARG_EDITED_FILE_URI));
            mFileAbsolutePath = Uri.parse(getArguments().getString(ARG_ORIGINAL_FILE_URI));
        }

        if (mEditedFileData == null) {
            mEditedFileData = new EditedFileData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootLayout = inflater.inflate(R.layout.fragment_edit_exif_view_pager, container, false);

        // Views
        mRecyclerView = mRootLayout.findViewById(R.id.rv_exif_data);
        mFabProgressCircleEditExif = mRootLayout.findViewById(R.id.fab_progress_circle_edit_exif);
        mFabProgressCircleEditExif.attachListener(this);
        mViewpager = getActivity().findViewById(R.id.vp_gallery_detail);
        mTabLayout = getActivity().findViewById(R.id.tl_gallery_detail);

        // Create an adapter
        mExifDataAdapter = new ExifDataAdapter(getContext(), this);

        // FAB to edit EXIF data
        mFabEditFileExif = mRootLayout.findViewById(R.id.fab_edit_exif);
        mFabEditFileExif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Edit the EXIF
                // TODO: Probably make a prompt

                if (mExifDataAdapter.isEditedExifDataListInstantiated() && mExifDataAdapter.getCheckboxesSelectedAmount() > 0) {
                    if (mCallThreadEditExif == null) {
                        // Start the progress circle and change the image to depict the FAB's new functionality
                        mFabProgressCircleEditExif.show();
                        mFabEditFileExif.setImageResource(R.drawable.ic_cancel_white_24dp);

                        // Disable Viewpager swiping
                        mViewpager.setViewpagerSwitching(false);

                        // Get a reference to the mTabLayout's children views
                        ViewGroup viewGroup = (ViewGroup) mTabLayout.getChildAt(0);

                        // Determine the amount of tabs present
                        int tabsCount = viewGroup.getChildCount();

                        // Iterate through the tabs and disable them
                        for (int i = 0; i < tabsCount; i++) {
                            // Get the child view at position i
                            ViewGroup viewGroupTag = (ViewGroup) viewGroup.getChildAt(i);

                            // Disable the tab
                            viewGroupTag.setEnabled(false);
                        }

                        // Thread can be cancelled
                        mCancelableCallThreadEditExif = true;



                        // Make the adapter consume recycler view's touch events
                        mExifDataAdapter.setClickable(false);

                        mCallThreadEditExif = new Handler();
                        mCallThreadEditExif.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Set thread priority
                                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);

                                // Thread can no longer be cancelled
                                mCancelableCallThreadEditExif = false;

                                if (mEditedImageAbsolutePath != null && !mEditedImageAbsolutePath.toString().isEmpty()) {
                                    // Initialize our boolean
                                    mSuccessfulExifEdit = false;

                                    // If @mEditedImageAbsolutePath exists and isn't empty, transfer the EXIF data to the file
                                    if (createImageFile()) {
                                        // Populate the edited image with the new EXIF data
                                        if (updateEditedFileExif()) {
                                            Log.d(LOG_TAG, "Run If if");

                                            // If successful, gather the modified EXIF data
                                            mExifDataArrayList = gatherExifData(mEditedImageAbsolutePath, getContext());

                                            // Edited Exif transfer success
                                            mSuccessfulExifEdit = true;
                                        }
                                    } else {
                                        Log.d(LOG_TAG, "Run if if else");

                                        // Edited EXIF transfer failed
                                        mSuccessfulExifEdit = false;
                                    }
                                } else {
                                    // Initialize our boolean
                                    mSuccessfulExifEdit = false;

                                    // If @mEditedImageAbsolutePath doesn't exist, create an image file and transfer the EXIF data to the file
                                    if (createImageFile()) {
                                        Log.d(LOG_TAG, "Run if Else if");
                                        // If the image was successfully created, initialize the file's edited EXIF data

                                        if (initializeEditedFileExif()) {
                                            // If successful, gather the modified EXIF data
                                            mExifDataArrayList = gatherExifData(mEditedImageAbsolutePath, getContext());

                                            // Edited EXIF initialization success
                                            mSuccessfulExifEdit = true;
                                        } else {
                                            // Edited EXIF initialization failed
                                            mSuccessfulExifEdit = false;
                                        }
                                    } else {
                                        Log.d(LOG_TAG, "Run Else if else");

                                        // Update boolean for @endCallThreadEditExif
                                        mSuccessfulExifEdit = false;
                                    }
                                }

                                if (mSuccessfulExifEdit) {

                                    // Send the EditedFileData object with update information
                                    mEditedFileData.setEditedFileUri(mEditedImageAbsolutePath);
                                    mEditedFileData.setExifChanged(true);

                                    // If successfully initialized, send the file Uri to the other fragments and update @mExifDataAdapter
                                    mSendUri.editExifTransferEditedFileData(mEditedFileData);
                                    ((GalleryUploadActivity) getActivity()).mSectionsPagerAdapter
                                            .getFragment(GalleryUploadActivity.VIEWPAGER_UPLOAD_FRAGMENT).onResume();

                                    mSendUri.editExifTransferEditedFileData(mEditedFileData);
                                    ((GalleryUploadActivity) getActivity()).mSectionsPagerAdapter
                                            .getFragment(GalleryUploadActivity.VIEWPAGER_EDIT_FILE_FRAGMENT).onResume();
                                }

                                // Clear the thread and update the FAB upon final animation
                                mFabProgressCircleEditExif.beginFinalAnimation();
                            }
                        }, 3000 /* 3 second delay */);
                    } else {
                        if (mCancelableCallThreadEditExif) {
                            Log.d(LOG_TAG, "Canceling thread?");
                            // Enable Viewpager swiping
                            mViewpager.setViewpagerSwitching(true);

                            // Get a reference to the mTabLayout's children views
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

                            // If the thread has already been instantiated the user has indicated stopping it
                            // Remove callback and return thread back to normal
                            mCallThreadEditExif.removeCallbacksAndMessages(null);
                            mCallThreadEditExif = null;

                            // Reset the FAB and hide the editing progress bar
                            mFabProgressCircleEditExif.hide();
                            mFabEditFileExif.setImageResource(R.drawable.ic_exif_edit);

                            // Pass the user a success notification of cancellation
                            Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content),
                                    getContext().getString(R.string.sb_edit_exif_cancelled), Snackbar.LENGTH_SHORT).show();

                            // Enable clicking on the EXIF checkboxes
                            mExifDataAdapter.setClickable(true);
                        }
                    }
                } else {
                    Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content),
                            getContext().getString(R.string.sb_edit_exif_no_changes), Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        // Setup layout for the Recycler View
//        TODO: Possibly set up a grid
//        mGridLayoutManager = new GridLayoutManager(this, 3);
//        mRecyclerView.setLayoutManager(mGridLayoutManager);

        return mRootLayout;
    }
    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Gather the most recent image's EXIF data
        if (mEditedImageAbsolutePath != null && !mEditedImageAbsolutePath.toString().isEmpty()) {
            mExifDataArrayList = gatherExifData(mEditedImageAbsolutePath, getContext());
        } else {
            mExifDataArrayList = gatherExifData(mFileAbsolutePath, getContext());
        }

        // Populate the adapter with our harvested image data
        mExifDataAdapter.swapExifData(mExifDataArrayList);

        // Create a layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        // Attach the layout manager and adapter to the recycler view
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mExifDataAdapter);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof EditExifFragmentUriUpdate) {
            mSendUri = (EditExifFragmentUriUpdate) getActivity();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement UploadFragmentUriUpdate");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSendUri = null;
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {@link Activity#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
        Log.d(LOG_TAG, "onResume");
        if (mEditedFileData != null) {
            String quack = null;
            if(mEditedFileData.getEditedFileUri() != null ) {
                quack = mEditedFileData.getEditedFileUri().toString();
            }
            Log.d(LOG_TAG, "Is this stuff saved?" + quack);
        }
        super.onResume();
    }

    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to {@link Activity#onPause() Activity.onPause} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onPause() {
        Log.d(LOG_TAG, "onPause");
        super.onPause();
    }

    /**
     * Called when the Fragment is no longer started.  This is generally
     * tied to {@link Activity#onStop() Activity.onStop} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStop() {
        Log.d(LOG_TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onListItemClick(int file) {

    }

    /**
     * Animation to depict the uploading process
     */
    @Override
    public void onFABProgressAnimationEnd() {
        // Remove callback and return thread back to normal
        mCallThreadEditExif.removeCallbacksAndMessages(null);
        mCallThreadEditExif = null;

        // Reset the FAB and hide the editing progress bar
        mFabEditFileExif.setImageResource(R.drawable.ic_exif_edit);

        // Refresh the data adapter with new data
        mExifDataAdapter.renewEditedExifDataList();

        mExifDataAdapter.swapExifData(mExifDataArrayList);

        // Provide a snackbar to inform the user
        if (mSuccessfulExifEdit) {
            Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content),
                    R.string.sb_exif_transfer_data_successful, Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content),
                    R.string.sb_exif_transfer_data_unsuccessful, Snackbar.LENGTH_LONG).show();
        }

        // Enable Viewpager switching
        mViewpager.setViewpagerSwitching(true);

        // Enable Exif Adapter clicking
        mExifDataAdapter.setClickable(true);

        // Get a reference to the mTabLayout's children views
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
    }

    public interface EditExifFragmentUriUpdate {
        void editExifTransferEditedFileData(EditedFileData mEditedFileData);
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onEditExifFragmentInteraction(Uri uri);
    }

    // TODO: Appropriately check for downscaled image
    /* Image Creation Methods*/
    /**
     * Utilizes @createFile to generate an image file that's a copy of @mFileAbsolutePath save
     * for the exif values
     *
     * @return a true if the image was successfully copied, otherwise return false
     */
    private boolean createImageFile() {
        // Create a bitmap of the most recent file
        Bitmap bitmap;
        if (mEditedImageAbsolutePath != null && !mEditedImageAbsolutePath.toString().isEmpty()) {
            // If an edited image exists, use the edited as a base
            bitmap = BitmapFactory.decodeFile(mEditedImageAbsolutePath.toString());
        } else {
            // If no edited image exists, use the original as a base
            bitmap = BitmapFactory.decodeFile(mFileAbsolutePath.toString());
        }

        // Create the file that the result will populate
        File imageFile = null;

        try {
            // Create the file name that we'd like to use and populate
            imageFile = createFile();
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error occurred while creating the file: " + e);
            e.printStackTrace();
        }

        if (imageFile != null) {
            mDuplicateImageCreated = FileUtils.createOriginalScaledImageFile(bitmap, imageFile);

            // Check to see if the duplicate image has been created
            if (mDuplicateImageCreated) {
                // Verify that the image is actually rescaled appropriately
                if (mEditedImageAbsolutePath != null && !mEditedImageAbsolutePath.toString().isEmpty()) {
                    // If an edited image exists, use the edited
                    mDuplicateImageCreated = FileUtils.checkSuccessfulBitmapDuplication(mEditedImageAbsolutePath,
                            mEditedImageAbsolutePathTemp);
                } else {
                    // If no edited image exists, use the original
                    mDuplicateImageCreated = FileUtils.checkSuccessfulBitmapDuplication(mFileAbsolutePath,
                            mEditedImageAbsolutePathTemp);
                }


                if (mDuplicateImageCreated) {
                    return true;
                } else {
                    Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content),
                            R.string.sb_image_unsuccessfully_created, Snackbar.LENGTH_LONG).show();

                    // Delete the failed file
                    deleteTempEditedFile();

                    // Make the edited image path point to nothing
                    mEditedImageAbsolutePathTemp = null;

                    return false;
                }
            } else {
                Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content),
                        R.string.sb_image_unsuccessfully_created, Snackbar.LENGTH_LONG).show();

                // Delete the failed file
                deleteTempEditedFile();

                // Make the edited image path point to nothing
                mEditedImageAbsolutePathTemp = null;

                return false;
            }
        } else {
            Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content),
                    R.string.sb_image_unsuccessfully_created, Snackbar.LENGTH_LONG).show();

            // Delete the failed file
            deleteTempEditedFile();

            // Make the edited image path point to nothing
            mEditedImageAbsolutePathTemp = null;

            return false;
        }
    }

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
        mEditedImageAbsolutePathTemp = Uri.parse(image.getAbsolutePath());

        return image;
    }

    /* Image Deletion Methods */
    /**
     * Deletes the edited file being utilized
     *
     * @return true if the file was successfully deleted, otherwise return false upon failure
     */
    private boolean deleteEditedFile() {
        // Delete Previous File
        File file = new File(mEditedImageAbsolutePath.toString());

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
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{mEditedImageAbsolutePath.toString()});
            }

            // Successfully deleted the file
            mEditedImageAbsolutePath = null;
            return true;
        }

        // Failed to delete the file
        return false;
    }

    /**
     * Deletes the temporary edited file being utilized
     *
     * @return true if the file was successfully deleted, otherwise return false upon failure
     */
    private boolean deleteTempEditedFile() {
        // Delete Previous File
        File file = new File(mEditedImageAbsolutePathTemp.toString());

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
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{mEditedImageAbsolutePath.toString()});
            }

            // Successfully deleted the file
            mEditedImageAbsolutePathTemp = null;
            return true;
        }

        // Failed to delete the file
        return false;
    }

    /* Image Exif Editing Methods */
    /**
     * Method to update @mEditedImageAbsolutePath's EXIF
     *
     * @return
     */
    public boolean updateEditedFileExif() {
        Log.d(LOG_TAG, "This is new EXIF data population");
        if (transferEditedExifData(mExifDataAdapter.getEditedExifDataTransferList(), mEditedImageAbsolutePath, mEditedImageAbsolutePathTemp)) {
            Log.d(LOG_TAG, "Boom check shit"  +  mEditedImageAbsolutePath.toString() + " " + mEditedImageAbsolutePathTemp.toString());

            // EXIF  transfer successful, delete the base edited file
            if (!deleteEditedFile()) {
                Log.e(LOG_TAG, "Edited file deletion failed");
            }

            // Make the temp file the new base edited file
            mEditedImageAbsolutePath = mEditedImageAbsolutePathTemp;

            // Update successful
            return true;
        } else {
            // EXIF transfer failed, destroy the temp file
            if (!deleteTempEditedFile()) {
                Log.e(LOG_TAG, "Temp Edited file deletion failed");
            }

            // Update failed
            return false;
        }
    }

    /**
     * Method to initialize @mEditedImageAbsolutePath's EXIF
     *
     * @return
     */
    public boolean initializeEditedFileExif() {
        Log.d(LOG_TAG, "This is new EXIF data population");
        if (transferEditedExifData(mExifDataAdapter.getEditedExifDataTransferList(), mFileAbsolutePath, mEditedImageAbsolutePathTemp)) {
            Log.d(LOG_TAG, "Boom check shit"  +  mEditedImageAbsolutePath.toString() + " " + mEditedImageAbsolutePathTemp.toString());
            Log.d(LOG_TAG, "Boom check shit"  +  mEditedImageAbsolutePath.toString() + " " + mEditedImageAbsolutePathTemp.toString());

            // EXIF creation successful, make the t mep file the new base edited file
            mEditedImageAbsolutePath = mEditedImageAbsolutePathTemp;

            // Reset the temp file
            mEditedImageAbsolutePathTemp = null;

            // Initialization was successful
            return true;
        } else {
            // EXIF transfer failed, destroy the temp file
            if (!deleteTempEditedFile()) {
                Log.e(LOG_TAG, "Temp Edited file deletion failed");
            }

            // Initialization failed
            return false;
        }
    }

    /* Handler Thread */
    /**
     * Remove all callbacks and messages from the handler thread then refresh the FAB and its progress circle
     *
     * @param successfulExifEdit
     */
    public void endCallThreadEditExif(boolean successfulExifEdit) {
        Log.d(LOG_TAG, "endCAllThread called");


    }
}
