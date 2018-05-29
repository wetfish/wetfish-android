package net.wetfish.wetfish.ui.viewpager;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.wetfish.wetfish.R;
import net.wetfish.wetfish.adapters.ExifDataAdapter;
import net.wetfish.wetfish.utils.ExifUtils;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditExifFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditExifFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditExifFragment extends Fragment implements
        ExifDataAdapter.ExifDataAdapterOnClickHandler{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    /* Constants */
    private static final String LOG_TAG = EditExifFragment.class.getSimpleName();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_FILE_URI = "file_uri";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /* Views */
    private RecyclerView mRecyclerView;
    private ExifDataAdapter mExifDataAdapter;

    /* Data */
    private Uri mFileUriAbsolutePath;
    ArrayList<Object> exifDataArrayList;


    private OnFragmentInteractionListener mListener;

    public EditExifFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param fileUri Parameter 2.
     * @return A new instance of fragment EditExifFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditExifFragment newInstance(String param1, Uri fileUri) {
        EditExifFragment fragment = new EditExifFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_FILE_URI, fileUri.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mFileUriAbsolutePath = Uri.parse(getArguments().getString(ARG_FILE_URI));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_edit_exif_view_pager, container, false);

        // Recycler View for Files
        mRecyclerView = rootView.findViewById(R.id.rv_exif_data);

        // Setup layout for the Recycler View
//        TODO: Possibly set up a grid
//        mGridLayoutManager = new GridLayoutManager(this, 3);
//        mRecyclerView.setLayoutManager(mGridLayoutManager);

        // Setup adapter for Recycler View
        exifDataArrayList = ExifUtils.gatherExifData(mFileUriAbsolutePath, getContext());


        return rootView;
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

        // Create an adapter and populate it with our harvested image data
        mExifDataAdapter = new ExifDataAdapter(getContext(), this);
        mExifDataAdapter.swapEXIFData(exifDataArrayList);

        // Create a layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        // Attach the layout manager and adapter to the recycler view
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mExifDataAdapter);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onEditExifFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(int file) {

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
