package net.wetfish.wetfish.ui;

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
import net.wetfish.wetfish.utilities.FileUtils;
import net.wetfish.wetfish.utilities.RESTInterface;
import net.wetfish.wetfish.utilities.RetrofitClient;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class GalleryDetailActivity extends AppCompatActivity{

    // Logging Tag
    private static final String LOG_TAG = GalleryDetailActivity.class.getSimpleName();

    // Bundle Variables
    Uri dataUri;

    // View Variables
    ImageView dataImageView;
    ImageView dataImageView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_detail);

        //TODO: Definitely fix up the UI. Get rid of this button and implement the FAB
        // Gather and set views
        dataImageView = (ImageView) findViewById(R.id.iv_gallery_detail);
        dataImageView2 = (ImageView) findViewById(R.id.iv_gallery_detail_2);

        // Gather Intent data
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            // Gather Intent String Data & Parse to URI
            String myUri = bundle.getString(getString(R.string.gallery_detail_uri_key));
            dataUri = Uri.parse(myUri);

            // Set view data
            if (dataUri != null) {
                Log.d(LOG_TAG, "Image Data URI: " + dataUri.toString());
                Glide.with(this)
                        .load(dataUri)
                        .into(dataImageView);
            } else {
                //TODO: Probably remove Snackbar use of this manner and keep to logs.
                Log.d(LOG_TAG, "dataUri returned null");
                Snackbar.make(findViewById(android.R.id.content), "dataUri returned Null", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        } else {
            //TODO: Probably remove Snackbar use of this manner and keep to logs.
            Log.d(LOG_TAG, "Bundle returned null");
            Snackbar.make(findViewById(android.R.id.content), "Bundle was Null", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        //Setup Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //TODO: Turn into a radial expanding bottom action button bar!
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "uploadFile FAB");
                uploadFile(dataUri);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void uploadFile(Uri fileUri) {

        // Create Retrofit Instance
        Retrofit retrofit =  RetrofitClient.getClient(getString(R.string.wetfish_base_url));

        // Create REST Interface
        RESTInterface restInterface = retrofit.create(RESTInterface.class);

        // Create RequestBody instance from our chosen file
        File file = new File(FileUtils.getRealPathFromUri(this, fileUri));

        Log.d(LOG_TAG, "NAME OF THING: " + FileUtils.getRealPathFromUri(this, fileUri)
                + "\n" + file.getName());
        Glide.with(this)
                .load(file)
                .into(dataImageView2);

        // Create the RequestBody & multipart
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("Image", file.getName(), requestBody);

        // Execute request
        //TODO: Update these Snackbars
        Call<ResponseBody> call = restInterface.postPhoto(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //TODO: Use some sort of text parsing to gather the passed image URL response.
                Snackbar.make(findViewById(android.R.id.content), "Image Uploaded!", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                Log.d(LOG_TAG, "onResponse Response: " + response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Snackbar.make(findViewById(android.R.id.content), "Image Upload Failed!", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                Log.d(LOG_TAG, "onFailure Response: " + t);
            }

        });
    }
}
