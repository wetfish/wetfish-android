package net.wetfish.wetfish.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
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
            Log.d(LOG_TAG, "What is this");
            if (fileCursor != null) {

                // Storage paths for all files saved to the database
                String originalFileStorageLink = fileCursor.getString(fileCursor.getColumnIndex(FileContract.FileColumns.COLUMN_FILE_DEVICE_STORAGE_LINK));
                String editedFileStorageLink = fileCursor.getString(fileCursor.getColumnIndex(FileContract.FileColumns.COLUMN_FILE_EDITED_DEVICE_STORAGE_LINK));
                String fileWetfishPath = fileCursor.getString(fileCursor.getColumnIndex(FileContract.FileColumns.COLUMN_FILE_WETFISH_STORAGE_LINK));
                String fileType = fileCursor.getString(fileCursor.getColumnIndex(FileContract.FileColumns.COLUMN_FILE_TYPE_EXTENSION));

                // Boolean values to determine if a file exists or not
                boolean originalFilePresent = FileUtils.checkIfFileExists(originalFileStorageLink);
                boolean editedFilePresent = FileUtils.checkIfFileExists(editedFileStorageLink);

                // Network information
                ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();

                // Check to see if the network is connected and available
                if (networkInfo != null && networkInfo.isConnected()) {
                    // Check to see if the file is representable by glide
                    if (FileUtils.representableByGlide(fileType)) {
                            // Load the edited file from the local storage if possible
                            Glide.with(mContext)
                                    .load(editedFileStorageLink)
                                    .error(Glide.with(mContext)
                                            .load(originalFileStorageLink)
                                            .apply(RequestOptions.centerCropTransform()))
                                    .error(Glide.with(mContext)
                                            .load(fileWetfishPath)
                                            .apply(RequestOptions.centerCropTransform()))
                                    .error(Glide.with(mContext)
                                            .load(ContextCompat.getDrawable(mContext, R.drawable.glide_file_not_found_anywhere))
                                            .apply(RequestOptions.fitCenterTransform()))
                                    .apply(RequestOptions.placeholderOf(new ColorDrawable(Color.DKGRAY)))
                                    .apply(RequestOptions.centerCropTransform())
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(fileView);
                    } else { // FileUtils.representableByGlide(mFileType) else
                        Log.d(LOG_TAG, "File is not representable by glide");
                        // If the file is not representable by glide depict this to the user
                        Glide.with(mContext)
                                .load(ContextCompat.getDrawable(mContext, R.drawable.glide_not_representable))
                                .apply(RequestOptions.placeholderOf(new ColorDrawable(Color.DKGRAY)))
                                .apply(RequestOptions.fitCenterTransform())
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(fileView);
                    }
                } else { // mNetworkInfo != null && mNetworkInfo.isConnected() else
                    if (FileUtils.representableByGlide(fileType)) {
                            Log.d(LOG_TAG, "No network, edited file present");
                            // Load the desired file storage link first, then
                            Glide.with(mContext)
                                    .load(editedFileStorageLink)
                                    .error(Glide.with(mContext)
                                            .load(originalFileStorageLink)
                                            .apply(RequestOptions.centerCropTransform()))
                                    .error(Glide.with(mContext)
                                            .load(ContextCompat.getDrawable(mContext, R.drawable.glide_file_not_found_no_network))
                                            .apply(RequestOptions.fitCenterTransform()))
                                    .apply(RequestOptions.placeholderOf(new ColorDrawable(Color.DKGRAY)))
                                    .apply(RequestOptions.centerCropTransform())
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(fileView);
                    } else { // FileUtils.representableByGlide(mFileType) else
                        Log.d(LOG_TAG, "File is not representable by glide");
                        // If the file is not representable by glide depict this to the user
                        Glide.with(mContext)
                                .load(ContextCompat.getDrawable(mContext, R.drawable.glide_not_representable))
                                .apply(RequestOptions.placeholderOf(new ColorDrawable(Color.DKGRAY)))
                                .apply(RequestOptions.fitCenterTransform())
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(fileView);
                    }
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
