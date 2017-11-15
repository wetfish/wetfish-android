package net.wetfish.wetfish.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import net.wetfish.wetfish.R;
import net.wetfish.wetfish.retrofit.RESTInterface;
import net.wetfish.wetfish.retrofit.RetrofitClient;
import net.wetfish.wetfish.utils.FileUtils;
import net.wetfish.wetfish.utils.UIUtils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class GalleryDetailActivity extends AppCompatActivity {

    // Logging Tag
    private static final String LOG_TAG = GalleryDetailActivity.class.getSimpleName();

    // Bundle Variables
    Uri dataUri;

    // View Variables
    ImageView intentDataView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_detail);

        // Setup View
        intentDataView = (ImageView) findViewById(R.id.iv_gallery_detail);

        // Gather intent
        Intent intent = getIntent();

        // Check to see if the intent is of the correct mime type.
        if (intent.getType().indexOf("image/") != -1) {
            if (intent.getData() != null) {
                // Handle intents with image data from Wetfish app explicit intent...
                dataUri = getIntent().getData();
            } else if (intent.getExtras().get(Intent.EXTRA_STREAM) != null) {
                // Handle intents with image data from share smenu implicit intents...
                dataUri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
            }

            // Set view data
            if (dataUri != null) {
                Log.d(LOG_TAG, "File Data URI: " + dataUri.toString());
                Glide.with(this)
                        .load(dataUri)
                        .into(intentDataView);
            } else {
                Log.d(LOG_TAG, "dataUri returned null");
                UIUtils.generateSnackbar(getApplicationContext(), findViewById(android.R.id.content),
                        "Unable to obtain file location", Snackbar.LENGTH_LONG);
            }
        } else {
            Log.d(LOG_TAG, "Bundle returned null");
            UIUtils.generateSnackbar(getApplicationContext(), findViewById(android.R.id.content),
                    "Unable to obtain chosen file", Snackbar.LENGTH_LONG);
        }

        //Setup Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //TODO: Turn into a radial expanding bottom action button bar!
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile(dataUri);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void uploadFile(Uri fileUri) {

        // Create Retrofit Instance
        Retrofit retrofit = RetrofitClient.getClient(getString(R.string.wetfish_base_url));

        // Create REST Interface
        RESTInterface restInterface = retrofit.create(RESTInterface.class);

        // Create RequestBody instance from our chosen file
        File file = new File(FileUtils.getRealPathFromUri(this, fileUri));

        //TODO: Remove later
        Log.d(LOG_TAG, "NAME OF THING: " + FileUtils.getRealPathFromUri(this, fileUri)
                + "\n" + file.getName());

        // Create the RequestBody & multipart
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("Image", file.getName(), requestBody);

        // Execute request
        Call<ResponseBody> call = restInterface.postPhoto(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                UIUtils.generateSnackbar(getApplicationContext(), findViewById(android.R.id.content),
                        "File Uploaded!", Snackbar.LENGTH_LONG);
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                UIUtils.generateSnackbar(getApplicationContext(), findViewById(android.R.id.content),
                        "File Upload Failed!", Snackbar.LENGTH_LONG);
                Log.d(LOG_TAG, "onFailure Response: " + t);
            }

        });
    }
}
