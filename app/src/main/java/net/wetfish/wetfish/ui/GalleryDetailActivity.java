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

public class GalleryDetailActivity extends AppCompatActivity {

    // Logging Tag
    private static final String LOG_TAG = GalleryDetailActivity.class.getSimpleName();


    Uri dataUri;

    ImageView dataImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_detail);

        // Gather and set views
        dataImageView = (ImageView) findViewById(R.id.iv_gallery_detail);

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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
