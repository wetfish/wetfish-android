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
import net.wetfish.wetfish.utils.UIUtils;

public class GalleryActivity extends AppCompatActivity {

    // Logging Tag
    private static final String LOG_TAG = GalleryActivity.class.getSimpleName();

    // Intent Constant
    private int PICK_FILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Setup Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // FAB to start intent to select a file then pass the user to another activity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFile(view);
            }
        });
    }

    /**
     * This method will be invoked when the FAB is activated.
     *
     * @param view
     */
    private void getFile(View view) {
        // Attempt to open gallery
        try {

            //TODO: Reinsert picture intent when video uploading is supported
            Intent pickFileIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickFileIntent.setType("image/*");

//            Intent useCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            Intent chooserIntent = Intent.createChooser(pickFileIntent, getString(R.string.chooser_title));
//            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {useCameraIntent});

            startActivityForResult(chooserIntent, PICK_FILE);
        } catch (Exception e) {
            Log.d(LOG_TAG, "An exception occurred!: " + e.toString());
        }

    }

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
                        .setDataAndType(contentUri, getString(R.string.image_mime_type)));
        } else {
            //TODO: Probably should remove snackbar later
            UIUtils.generateSnackbar(this, findViewById(android.R.id.content),
                    "No file selected", Snackbar.LENGTH_SHORT);
            Log.d(LOG_TAG, "Result Code Returned: " + resultCode);

        }
    }
}
