package net.wetfish.wetfish.ui.viewpager;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.wetfish.wetfish.R;
import net.wetfish.wetfish.adapters.ExifDataAdapter;
import net.wetfish.wetfish.data.EditedFileData;

import java.util.ArrayList;

import static net.wetfish.wetfish.utils.ExifUtils.gatherExifData;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditExifFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditExifFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditExifFragment extends Fragment implements
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
        mViewpager = getActivity().findViewById(R.id.vp_gallery_detail);
        mTabLayout = getActivity().findViewById(R.id.tl_gallery_detail);

        // Create an adapter
        mExifDataAdapter = new ExifDataAdapter(getContext(), this);

//                                    mEditedFileData.setEditedFileUri(mEditedImageAbsolutePath);
//                                    mEditedFileData.setExifChanged(true);
                                    // If successfully initialized, send the file Uri to the other fragments and update @mExifDataAdapter
//                                    mSendUri.editExifTransferEditedFileData(mEditedFileData);
//                                    ((GalleryUploadActivity) getActivity()).mSectionsPagerAdapter
//                                            .getFragment(GalleryUploadActivity.VIEWPAGER_UPLOAD_FRAGMENT).onResume();
//
//                                    mSendUri.editExifTransferEditedFileData(mEditedFileData);
//                                    ((GalleryUploadActivity) getActivity()).mSectionsPagerAdapter
//                                            .getFragment(GalleryUploadActivity.VIEWPAGER_EDIT_FILE_FRAGMENT).onResume();

        // Setup layout for the Recycler View
//        TODO: Possibly set up a grid
//        mGridLayoutManager = new GridLayoutManager(this, 3);
//        mRecyclerView.setLayoutManager(mGridLayoutManager);

        return mRootLayout;
    }

    // TODO: Make sure to make the exif always come from the original image
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

}
