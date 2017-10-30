package net.wetfish.wetfish.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import net.wetfish.wetfish.R;

public class GalleryActivity extends AppCompatActivity {

    // Logging Tag
    private static final String LOG_TAG = GalleryActivity.class.getSimpleName();

    // Intent Constant
    private int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Setup Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // FAB to start intent to select an image then pass the user to another activity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImageFromGallery(view);
            }
        });
    }

    /**
     * TODO: Potentially remove document intent addition.
     * This method will be invoked when the FAB is activated.
     *
     * @param view
     */
    private void getImageFromGallery(View view) {
        // Attempt to open gallery
        try {
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");

            Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");

            Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

            startActivityForResult(chooserIntent, PICK_IMAGE);
        } catch (Exception e) {
            Snackbar.make(view, "An Exception Occurred! " + "\n" + e.toString(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

    }
    // Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURE);

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Gather the result code from our intent result and start GalleryDetailActivity.class
     *
     * @param reqCode
     * @param resultCode    Determines the result of the request
     * @param data          Gathers data from the passed intent
     */
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
                Uri contentUri = data.getData();
                startActivity(new Intent(this, GalleryDetailActivity.class)
                        .putExtra(getString(R.string.gallery_detail_uri_key), contentUri.toString()));
        } else {
            //TODO: Probably should remove snackbar later
            Log.d(LOG_TAG, "Result Code Returned: " + resultCode);
            Snackbar.make(findViewById(android.R.id.content), "Result Code Error!: " + resultCode, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
