package net.wetfish.wetfish.ui.viewpager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.github.clans.fab.FloatingActionButton;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;

import net.wetfish.wetfish.R;
import net.wetfish.wetfish.retrofit.RESTInterface;
import net.wetfish.wetfish.retrofit.RetrofitClient;
import net.wetfish.wetfish.ui.GalleryActivity;
import net.wetfish.wetfish.ui.GalleryDetailActivity;
import net.wetfish.wetfish.utils.FileUtils;
import net.wetfish.wetfish.utils.UIUtils;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FileUploadFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FileUploadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileUploadFragment extends Fragment implements FABProgressListener,
        AdapterView.OnItemSelectedListener{

    /* Fragment initialization parameter keys */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_FILE_URI = "file_uri";

    /* Constants */
    private static final String LOG_TAG = FileUploadFragment.class.getSimpleName();
    private static final int REQUEST_STORAGE = 0;
    private static final String[] PERMISSIONS_STORAGE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int ORIGINAL_SIZE_SELECTION = 0;
    private static final int LARGE_SIZE_SELECTION = 1;
    private static final int MEDIUM_SIZE_SELECTION = 2;
    private static final int SMALL_SIZE_SELECTION = 3;


    /* Fragment initialization parameter variables */
    private int sectionNumber;
    private Uri fileUri;

    /* Views */
    private TextView fileNotFoundView;
    private ImageView fileView;
    private EditText fileEditTitleView;
    private EditText fileEditTagsView;
    private EditText fileEditDescriptionView;
    private FloatingActionButton fabUploadFile;
    private FABProgressCircle fabProgressCircle;
    private View mRootLayout;
    private View fileUploadContent;
    private Spinner mSpinner;

    /* Data */
    private String responseViewURL;
    private String responseDeleteURL;
    private boolean responseURLAcquired;
    private boolean fileFound;
    private int uploadID;
    //TODO: Potentially remove.
    private OnFragmentInteractionListener mListener;

    public FileUploadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param position The position of the fragment
     * @param fileUri  The Uri of the data passed to the fragment
     * @return A new instance of fragment FileUploadFragment.
     */
    public static FileUploadFragment newInstance(Integer position, Uri fileUri) {
        FileUploadFragment fragment = new FileUploadFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, position);
        args.putString(ARG_FILE_URI, fileUri.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            fileUri = Uri.parse(getArguments().getString(ARG_FILE_URI));
        }
    }

    //TODO: Later on when Video Playback is possible with exoplayer the focus feature will only be for images
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        mRootLayout = inflater.inflate(R.layout.fragment_file_upload_view_pager, container, false);

        // Reference to file upload layout content
        fileUploadContent = mRootLayout.findViewById(R.id.file_upload_content_container);

        // Views
        // TODO: Support Video Views soon. (Glide/VideoView/Exoplayer)
        fileView = mRootLayout.findViewById(R.id.iv_fragment_file_upload);
        fileEditTitleView = mRootLayout.findViewById(R.id.et_title);
        fileEditTagsView = mRootLayout.findViewById(R.id.et_tags);
        fileEditDescriptionView = mRootLayout.findViewById(R.id.et_description);
        fabProgressCircle = mRootLayout.findViewById(R.id.fab_progress_circle);

        // Setup Spinner
        mSpinner = (Spinner) mRootLayout.findViewById(R.id.spinner_fragment_file_upload);

        // Array Adapter for Spinner
        @SuppressLint("ResourceType") ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.upload_fragment_spinner_array, R.xml.custom_spinner_item);

        // Specific array adapter layout
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the mSpinner
        mSpinner.setAdapter(spinnerAdapter);

        // Setup onItemSelectedListener
        mSpinner.setOnItemSelectedListener(this);


        // Set a focus change listener to allow for focus to dictate the appearance of the keyboard
        View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
            /**
             * Called when the focus state of a view has changed.
             *
             * @param v        The view whose state has changed.
             * @param hasFocus The new focus state of v.
             */
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    fileView.setClickable(false);
                } else if (!hasFocus) {
                    fileView.setClickable(true);
                    UIUtils.hideKeyboard(v, getContext());
                }
            }
        };

        fileEditTitleView.setOnFocusChangeListener(focusChangeListener);
        fileEditTagsView.setOnFocusChangeListener(focusChangeListener);
        fileEditDescriptionView.setOnFocusChangeListener(focusChangeListener);

        // Setup listener for progress bar
        fabProgressCircle.attachListener(this);

        // Setup file interaction
        fileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to find proper app to open file
                Intent selectViewingApp = new Intent();
                selectViewingApp.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                selectViewingApp.setAction(Intent.ACTION_VIEW);

                // Use FileProvider to get an appropriate URI compatible with version Nougat+
                // File path and type from the given file
                String fileStorageLink = FileUtils.getRealPathFromUri(getContext(), fileUri);
                String fileType = FileUtils.getFileExtensionFromUri(getContext(), fileUri);
                Log.d(LOG_TAG, "File Storage Link: " + fileStorageLink);
                Log.d(LOG_TAG, "File Type: " + fileType);
                Uri fileProviderUri = FileProvider.getUriForFile(getContext(),
                        getString(R.string.file_provider_authority),
                        new File(fileStorageLink));

                // Setup the data and type
                // Appropriately determine mime type for the file
                selectViewingApp.setDataAndType(fileProviderUri, FileUtils.determineMimeType(getContext(), fileType));

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    selectViewingApp.setClipData(ClipData.newRawUri("", fileProviderUri));
                    selectViewingApp.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                Log.d(LOG_TAG, "Quack: " + fileProviderUri.toString());
                startActivity(selectViewingApp);
            }
        });

        //TODO: Make view to show nonImageType data
        // View to show if data wasn't accessible. Hidden/Shown depending on the result
        fileNotFoundView = mRootLayout.findViewById(R.id.tv_file_not_found);
        if (fileFound) {
            fileNotFoundView.setVisibility(View.GONE);
        } else {
            fileNotFoundView.setVisibility(View.VISIBLE);
        }

        // Fab to upload file to Wetfish server
        fabUploadFile = mRootLayout.findViewById(R.id.fab_upload_file);
        fabUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Verify that the permissions necessary to complete this action have been granted
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permissions have not been granted, inform the user and ask again
                    requestStoragePermission();

                } else {

                    // Storage permissions granted!
                    fabProgressCircle.show();
                    fabUploadFile.setClickable(false);
                    uploadFile(fileUri);
                }

            }
        });

        // Find out if the file is null
        if (fileUri != null && !(fileUri.toString().isEmpty())) {
            if (fileUri != null) {
                // File was found
                fileNotFoundView.setVisibility(View.GONE);
                fileFound = true;

                // Setup view data
                // Check to see if the view is representable by glide
                if (FileUtils.representableByGlide(FileUtils.getFileExtensionFromUri(getContext(), fileUri))) {
                    Glide.with(this)
                            .load(FileProvider.getUriForFile(getContext(),
                                    getString(R.string.file_provider_authority),
                                    new File(fileUri.toString())))
                            .apply(RequestOptions.fitCenterTransform())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(fileView);
                } else {
                    // If not, let the user know
                    Log.d(LOG_TAG, "Welp, something still went wrong!");

                }

            } else {
                UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
                        "File location was not found", Snackbar.LENGTH_LONG);
            }
        } else {
            UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
                    "Unable to obtain chosen file", Snackbar.LENGTH_LONG);

            // Make upload file inaccessible and inform the user.
            fabUploadFile.setVisibility(View.GONE);
            fileNotFoundView.setVisibility(View.VISIBLE);

        }

        return mRootLayout;
    }

    @Override
    public void onFABProgressAnimationEnd() {
        Snackbar.make(fabProgressCircle, getContext().getString(R.string.cloud_upload_complete), Snackbar.LENGTH_SHORT)
                .show();
        // Create file detail activity intent
        Intent fileDetails = new Intent(getContext(), GalleryDetailActivity.class);
        Intent backStackIntent = new Intent(getContext(), GalleryActivity.class);
        Intent[] intents = {backStackIntent, fileDetails};

        // Pass the Uri to the corresponding gallery item
        fileDetails.putExtra(getString(R.string.file_details),
                FileUtils.getFileData(getContext(), uploadID));

        // Start GalleryDetailActivity with an artificial back stack
        getContext().startActivities(intents);
    }


    //TODO: Potentially Remove Permission Questioning here, or keep just in case
    private void requestStoragePermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

            // If the user has previously denied granting the permission, offer the rationale
            Snackbar.make(mRootLayout, R.string.permission_storage_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestPermissions(PERMISSIONS_STORAGE, REQUEST_STORAGE);
                        }
                    }).show();
        } else {
            // No explanation needed, request permission
            requestPermissions(PERMISSIONS_STORAGE, REQUEST_STORAGE);
        }
    }

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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE: {
                if (requestCode == REQUEST_STORAGE) {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // Storage permissions were granted. Upload file.
                        uploadFile(fileUri);
                    } else {
                        // Storage Permissions were not granted
                        Snackbar.make(mRootLayout.findViewById(R.id.gallery_detail_content),
                                R.string.permission_not_granted_storage,
                                Snackbar.LENGTH_LONG).show();
                    }
                }
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
            }
        }
    }

    private void uploadFile(Uri fileUri) {

        // Create Retrofit Instance
        Retrofit retrofit = RetrofitClient.getClient(getString(R.string.wetfish_base_url));

        // Create REST Interface
        RESTInterface restInterface = retrofit.create(RESTInterface.class);

        // Create RequestBody instance from our chosen file
        File file = new File(FileUtils.getRealPathFromUri(getContext(), fileUri));

        // Gather file extension from chosen file for database
        final String fileExtension = FileUtils.getFileExtensionFromUri(getContext(), fileUri);

        // Gather file URI from chosen file for database
        final String filePath = FileUtils.getRealPathFromUri(getContext(), fileUri);

        // Create RequestBody & MultipartBody to create a Call.
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("Image", file.getName(), requestBody);
        Call<ResponseBody> call = restInterface.postFile(body);

        // Execute call request
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    // Get response body as a string
                    String onResponseString = response.body().string();
                    Log.d(LOG_TAG, "onResponse: " + onResponseString);

                    // If response body is not empty get returned URL
                    if (!(onResponseString.isEmpty())) {
                        Pattern pattern = Pattern.compile("url=(.*?)'>");
                        Matcher matcher = pattern.matcher(onResponseString);
                        if (matcher.find()) {
                            // Obtain the link given in response to the image
                            responseViewURL = getString(R.string.wetfish_base_url) + matcher.group(1);

                            responseDeleteURL = getContext().getString(R.string.not_implemented);

                            // Add to database
                            uploadID = FileUtils.insertFileData(getContext(),
                                    fileEditTitleView.getText().toString(),
                                    fileEditTagsView.getText().toString(),
                                    fileEditDescriptionView.getText().toString(),
                                    Calendar.getInstance().getTimeInMillis(),
                                    fileExtension,
                                    filePath,
                                    responseViewURL,
                                    responseDeleteURL);

                            Log.d(LOG_TAG, "onResponse: " + responseViewURL);
                            Log.d(LOG_TAG, "id: " + uploadID);

//                            UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
//                                    "File Uploaded!", Snackbar.LENGTH_LONG);
                            fabProgressCircle.beginFinalAnimation();
                        } else {
                            responseViewURL = getString(R.string.wetfish_base_uploader_url);

                            responseDeleteURL = getContext().getString(R.string.not_implemented);

                            // Add to database
                            uploadID = FileUtils.insertFileData(getContext(),
                                    fileEditTitleView.getText().toString(),
                                    fileEditTagsView.getText().toString(),
                                    fileEditDescriptionView.getText().toString(),
                                    Calendar.getInstance().getTimeInMillis(),
                                    fileExtension,
                                    filePath,
                                    responseViewURL,
                                    responseDeleteURL);

                            Log.d(LOG_TAG, "onResponse: " + responseViewURL);
                            Log.d(LOG_TAG, "id: " + uploadID);

//                            UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
//                                    "File Uploaded!", Snackbar.LENGTH_LONG);
                            fabProgressCircle.beginFinalAnimation();
                        }
                    } else {
                        responseViewURL = getString(R.string.wetfish_base_uploader_url);

                        responseDeleteURL = getContext().getString(R.string.not_implemented);

                        // Add to database
                        uploadID = FileUtils.insertFileData(getContext(),
                                fileEditTitleView.getText().toString(),
                                fileEditTagsView.getText().toString(),
                                fileEditDescriptionView.getText().toString(),
                                Calendar.getInstance().getTimeInMillis(),
                                fileExtension,
                                filePath,
                                responseViewURL,
                                responseDeleteURL);

                        Log.d(LOG_TAG, "onResponse: " + responseViewURL);
                        Log.d(LOG_TAG, "id: " + uploadID);

//                        UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
//                                "File Uploaded!", Snackbar.LENGTH_LONG);
                        fabProgressCircle.beginFinalAnimation();
                    }
                } catch (IOException e) {
                    Log.d(LOG_TAG, "onFailure Catch: ");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                fabUploadFile.setClickable(true);
                fabProgressCircle.hide();
                UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
                        "File Upload Failed!", Snackbar.LENGTH_LONG);
                Log.d(LOG_TAG, "onFailure Response: " + t);
            }
        });
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

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to {@link net.wetfish.wetfish.ui.GalleryUploadActivity#onStart() Activity.onStart} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStart() {
        super.onStart();
        responseURLAcquired = false;
    }

    /**
     * <p>Callback method to be invoked when an item in this view has been
     * selected. This callback is invoked only when the newly selected
     * position is different from the previously selected position or if
     * there was no selected item.</p>
     * <p>
     * Impelmenters can call getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param parent   The AdapterView where the selection happened
     * @param view     The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id       The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch(position) {
            case ORIGINAL_SIZE_SELECTION:
                Log.d(LOG_TAG, "Case 0");
                break;
            case LARGE_SIZE_SELECTION:
                Log.d(LOG_TAG, "Case 1");
                break;
            case MEDIUM_SIZE_SELECTION:
                Log.d(LOG_TAG, "Case 2");
                break;
            case SMALL_SIZE_SELECTION:
                Log.d(LOG_TAG, "Case 3");
                break;
            default:
                Log.d(LOG_TAG, "Y'never know!");
        }
    }

    /**
     * Callback method to be invoked when the selection disappears from this
     * view. The selection can disappear for instance when touch is activated
     * or when the adapter becomes empty.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothin'
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
        // TODO:
        void onUploadFragmentInteraction(Uri uri);
    }
}
