package net.wetfish.wetfish;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class GalleryActivity extends AppCompatActivity {

    private int PICK_IMAGE = 1;

    //TODO: Either figure out ButterKnife or delete it.
    //ButterKnife Binds
//    @BindView(R.id.iv_gallery_image)
      ImageView selectedImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Setup Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup Image View for selected Gallery picture
        selectedImageView = (ImageView) findViewById(R.id.iv_gallery_image);

        // FAB for acquiring an image
        //TODO: Image View & FAB: Create pop up Image View w/ FAB expanding Toolbar
        //TODO: Generating Grid of scrollable uploaded images and then pop up with Toolbar overlay
        //TODO: Review Potential Setups
        // When FAB is pressed and image data is passed, populate an image view above the grid.
        // Clicking on grid images will start up a new activity or popup w/ FAB Toolbar

        // When FAB is pressed and image data is passed, start up a new activity with FAB toolbar.
        // Clicking on grid images will do the same.

        // When FAB is pressed and image data is passed, start up a popup with FAB toolbar.
        // Clicking on grid images will do the same.
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
     * Gather the result code from our
     *
     * @param reqCode
     * @param resultCode    Determines the result of the request
     * @param data          Gathers data from the passed intent
     */
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            try {
                //TODO: Integrate Glide
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                selectedImageView.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Snackbar.make(findViewById(android.R.id.content), "File not found! " + "\n" + e.toString(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

        }else {
            Snackbar.make(findViewById(android.R.id.content), "Result Code Error!: " + resultCode, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
