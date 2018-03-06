package net.wetfish.wetfish.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import net.wetfish.wetfish.R;
import net.wetfish.wetfish.data.FileContract;
import net.wetfish.wetfish.utils.FileUtils;

/**
 * Created by ${Michael} on 12/12/2017.
 */

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {

    // Logging Tag
    private static final String LOG_TAG = FilesAdapter.class.getSimpleName();

    // ViewType tags
    private static final int VIEW_TYPE_IMAGE_FILE = 0;
    private static final int VIEW_TYPE_VIDEO_FILE = 1;

    // Click handler?
    private final FileAdapterOnClickHandler mClickHandler;

    // Activity context
    private Context mContext;

    // Activity cursor
    private Cursor mCursor;


    public FilesAdapter(Context mContext, FileAdapterOnClickHandler mClickHandler) {
        this.mClickHandler = mClickHandler;
        this.mContext = mContext;
    }

    /**
     * Populate the gallery with current cursor data
     *
     * @param newCursor is file data stored within the files SQLite DB
     */
    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }


    /**
     * //TODO: read me later
     * Called when RecyclerView needs a new {@link FileViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(FileViewHolder, int)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(FileViewHolder, int)
     */
    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //Gather context from parent to inflate the layout of the provided XML into layoutView
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item, parent, false);

        // Utilize layoutView as an argument to FileViewHolder to pass in all relevant view IDs
        FileViewHolder viewHolder = new FileViewHolder(layoutView);

        return viewHolder;
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link FileViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link FileViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override {@link #onBindViewHolder(FileViewHolder, int)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        // Move cursor to the current position
        mCursor.moveToPosition(position);

        // Pass cursor at relevant position to view holder
        holder.bind(mCursor);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        } else {
            return mCursor.getCount();
        }
    }

    // FileAdapterOnClickHandler interface
    public interface FileAdapterOnClickHandler {
        void onListItemClick(int file);
    }

    public class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // File image view
        //TODO: Impelemnt exoplayer later if video playback is desired
        public ImageView fileView;

        /**
         * Constructor for FileViewHolder. Obtain a reference to the layout.
         *
         * @param itemView The view for a specific item
         */
        public FileViewHolder(View itemView) {
            super(itemView);

            // Gallery item view
            fileView = (ImageView) itemView.findViewById(R.id.iv_gallery_item);
            itemView.setOnClickListener(this);
        }

        public void bind(Cursor fileCursor) {
            if (fileCursor != null) {

                // Storage paths for all files saved to the database
                String fileDeviceStoragePath = fileCursor.getString(fileCursor.getColumnIndex(FileContract.FileColumns.COLUMN_FILE_DEVICE_STORAGE_LINK));
                String editedFileDeviceStoragePath = fileCursor.getString(fileCursor.getColumnIndex(FileContract.FileColumns.COLUMN_FILE_WETFISH_EDITED_FILE_STORAGE_LINK));
                String fileWetfishPath = fileCursor.getString(fileCursor.getColumnIndex(FileContract.FileColumns.COLUMN_FILE_WETFISH_STORAGE_LINK));

                Log.d(LOG_TAG, "Original: " + fileDeviceStoragePath);
                Log.d(LOG_TAG, "Edited: " + editedFileDeviceStoragePath);
                Log.d(LOG_TAG, "Online: " + fileWetfishPath);

                // Variable to store the appropriate storage path to use
                String fileDevicePath;

                // Determine whether to use the original file or an edited file should it exist
                if (editedFileDeviceStoragePath != null) {
                    if (!editedFileDeviceStoragePath.isEmpty() && !editedFileDeviceStoragePath.equals("")) {
                        Log.d(LOG_TAG, "editedFile exists and is not empty");
                        fileDevicePath = editedFileDeviceStoragePath;
                    } else {
                        Log.d(LOG_TAG, "file does not exist and/or is empty");
                        fileDevicePath = fileDeviceStoragePath;
                    }
                } else {
                    Log.d(LOG_TAG, "file does not exist and/or is empty");
                    fileDevicePath = fileDeviceStoragePath;
                }

                ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    // If network is connected search the device for the stored image on the device
                    // then wetfish if not found.
                    if (FileUtils.representableByGlide(fileCursor.getString(fileCursor.getColumnIndex(FileContract.FileColumns.COLUMN_FILE_TYPE_EXTENSION)))) {
                        Glide.with(mContext)
                                .load(fileDevicePath)
                                .error(Glide.with(mContext)
                                        .load(fileWetfishPath)
                                        .apply(RequestOptions.centerCropTransform()))
                                .apply(RequestOptions.placeholderOf(new ColorDrawable(Color.DKGRAY)))
                                .apply(RequestOptions.centerCropTransform())
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(fileView);
                    } else {
                        Glide.with(mContext)
                                .load(null)
                                .apply(RequestOptions.placeholderOf(new ColorDrawable(Color.CYAN)))
                                .apply(RequestOptions.centerCropTransform())
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(fileView);
                    }

                } else {
                    // If network is not connected search the device for the stored file on the
                    // device then show a black image if not found.
                    //TODO: Figure out a good method for this later. In the meantime, storage or black image.
                    Glide.with(mContext)
                            .load(fileDevicePath)
                            .error(Glide.with(mContext)
                                    .load(new ColorDrawable(Color.BLACK))
                                    .apply(RequestOptions.fitCenterTransform()))
                            .apply(RequestOptions.placeholderOf(new ColorDrawable(Color.DKGRAY)))
                            .apply(RequestOptions.fitCenterTransform())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(fileView);
                }
            }

        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mClickHandler.onListItemClick(adapterPosition);
        }
    }
}
