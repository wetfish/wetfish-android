package net.wetfish.wetfish.ui.viewpager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import net.wetfish.wetfish.R;
import net.wetfish.wetfish.retrofit.RESTInterface;
import net.wetfish.wetfish.retrofit.RetrofitClient;
import net.wetfish.wetfish.utils.FileUtils;
import net.wetfish.wetfish.utils.UIUtils;

import java.io.File;
import java.io.IOException;
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

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FileUploadFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FileUploadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileUploadFragment extends Fragment {

    // Logging Tag
    private static final String LOG_TAG = FileUploadFragment.class.getSimpleName();

    // Fragment initialization parameter keys
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_FILE_URI = "file_uri";

    // Fragment initialization parameter variables
    private Integer sectionNumber;
    private Uri fileUri;

    // Data
    private String responseURL;
    private boolean responseURLAcquired;
    private boolean fileFound;

    // Views
    private TextView fileNotFoundView;
    private ImageView fileView;
    private FloatingActionMenu fam;
    private FloatingActionButton fabUploadFile;
    private FloatingActionButton fabCopyToClipboard;
    private FloatingActionButton fabChooseFile;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_file_upload_view_pager, container, false);

        // View to show passed data
        fileView = (ImageView) rootView.findViewById(R.id.iv_fragment_file_upload);

        // View to show if data wasn't accessible. Hidden/Shown depending on the result
        fileNotFoundView = (TextView) rootView.findViewById(R.id.tv_file_not_found);
        if (fileFound) {
            fileNotFoundView.setVisibility(View.GONE);
        } else {
            fileNotFoundView.setVisibility(View.VISIBLE);
        }
        // Fam to hold relevant fab actions
        fam = (FloatingActionMenu) rootView.findViewById(R.id.fam_gallery_detail);

        // Fab to upload file to Wetfish server
        fabUploadFile = (FloatingActionButton) rootView.findViewById(R.id.fab_upload_file);
        fabUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile(fileUri);
            }
        });

        // TODO: Implement snackbar button to copy to clipboard in case someone moves away from the window!
        // Fab to copy file link to clipboard
        fabCopyToClipboard = (FloatingActionButton) rootView.findViewById(R.id.fab_copy_to_clipboard);
        fabCopyToClipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Uploaded File Url", responseURL));
            }
        });

        // Hide copy file link till a file is uploaded
        if (!(responseURLAcquired)) {
            fabCopyToClipboard.setVisibility(View.GONE);
        } else {
            fabCopyToClipboard.setVisibility(View.VISIBLE);
        }

        // Fab to select a different file to upload
        fabChooseFile = (FloatingActionButton) rootView.findViewById(R.id.fab_choose_file);
        fabChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: To be implemented!
            }
        });

        // Hidden till further notice
        fabChooseFile.setVisibility(View.GONE);


        // Find out if the file is null
        if (fileUri != null && !(fileUri.toString().isEmpty())) {
            Log.d(LOG_TAG, "Image file");
            if (fileUri != null) {
                // File was found
                fileNotFoundView.setVisibility(View.GONE);
                fileFound = true;

                Log.d(LOG_TAG, "File Data URI: " + fileUri.toString());

                // insert the photo
                Glide.with(this)
                        .load(fileUri)
                        .into(fileView);
            } else {
                Log.d(LOG_TAG, "fileUri returned null");
                UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
                        "File location was not found", Snackbar.LENGTH_LONG);
            }
        } else {
            Log.d(LOG_TAG, "Bundle returned null");
            UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
                    "Unable to obtain chosen file", Snackbar.LENGTH_LONG);

            // Make upload file inaccessible and inform the user.
            fabUploadFile.setVisibility(View.GONE);
            fileNotFoundView.setVisibility(View.VISIBLE);

        }

        return rootView;
    }

    private void uploadFile(Uri fileUri) {

        // Create Retrofit Instance
        Retrofit retrofit = RetrofitClient.getClient(getString(R.string.wetfish_base_url));

        // Create REST Interface
        RESTInterface restInterface = retrofit.create(RESTInterface.class);

        // Create RequestBody instance from our chosen file
        File file = new File(FileUtils.getRealPathFromUri(getContext(), fileUri));

        //TODO: Remove later
        Log.d(LOG_TAG, "NAME OF THING: " + FileUtils.getRealPathFromUri(getContext(), fileUri)
                + "\n" + file.getName());

        // Create RequestBody & MultipartBody to create a Call.
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("Image", file.getName(), requestBody);
        Call<ResponseBody> call = restInterface.postFile(body);

        // Execute call request
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                UIUtils.generateSnackbar(getActivity(), getActivity().findViewById(android.R.id.content),
                        "File Uploaded!", Snackbar.LENGTH_LONG);
                try {
                    Log.d(LOG_TAG, "Try block: ");

                    // Get response body as a string
                    String onResponseString = response.body().string();
                    Log.d(LOG_TAG, "onResponse: " + onResponseString);

                    // If response body is not empty get returned URL
                    if (!(onResponseString.isEmpty())) {
                        Pattern pattern = Pattern.compile("url=(.*?)'>");
                        Matcher matcher = pattern.matcher(onResponseString);
                        if (matcher.find()) {
                            responseURL = getString(R.string.wetfish_base_url) + matcher.group(1);
                            fabCopyToClipboard.setVisibility(View.VISIBLE);
                            Log.d(LOG_TAG, "onResponse: " + responseURL);
                        }
                    } else {
                        responseURL = getString(R.string.wetfish_base_uploader_url);
                        fabCopyToClipboard.setVisibility(View.VISIBLE);
                    }
                } catch (IOException e) {
                    Log.d(LOG_TAG, "onFailure Catch: ");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
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
     * tied to {@link Activity#onStart() Activity.onStart} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStart() {
        super.onStart();
        responseURLAcquired = false;
    }

    //TODO: Definitely look at this at somepoint.

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
        public void onUploadFragmentInteraction(Uri uri);
    }
}
