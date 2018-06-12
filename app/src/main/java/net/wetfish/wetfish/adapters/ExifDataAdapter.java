package net.wetfish.wetfish.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import net.wetfish.wetfish.R;
import net.wetfish.wetfish.data.FileExifData;
import net.wetfish.wetfish.data.FileExifDataBlank;
import net.wetfish.wetfish.data.FileExifDataHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ${Michael} on 12/12/2017.
 */

public class ExifDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Logging Tag
    private static final String LOG_TAG = ExifDataAdapter.class.getSimpleName();

    // ViewType tags
    private static final int VIEW_TYPE_EXIF_DATA = 0;
    private static final int VIEW_TYPE_EXIF_DATA_BLANK = 1;
    private static final int VIEW_TYPE_EXIF_DATA_HEADER = 2;

    // Click handler?
    private final ExifDataAdapterOnClickHandler mClickHandler;
    // List of EXIF data objects
    private List<Object> mExifDataList = new ArrayList<>();
    // Activity context
    private Context mContext;


    public ExifDataAdapter(Context mContext, ExifDataAdapterOnClickHandler mClickHandler) {
        this.mClickHandler = mClickHandler;
        this.mContext = mContext;
    }

    /**
     * Populate the gallery with current cursor data
     *
     * @param newDataList is file data stored within the mExifDataList
     */
    public void swapEXIFData(List<Object> newDataList) {
        mExifDataList = newDataList;
        notifyDataSetChanged();
    }


    /**
     * Called when RecyclerView needs a new {@link FileExifDataHeader} or {@link FileExifData} of the given type to represent
     * an item.
     *
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(RecyclerView.ViewHolder, int)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(RecyclerView.ViewHolder, int)
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //Gather context from parent to inflate the layout of the provided XML into layoutView
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Viewholder instance
        RecyclerView.ViewHolder viewHolder;

        // View to populate the ViewHolder with
        View layoutView;

        //TODO: Can be simplified
        // Setup a switch to decide on the correct view for this layout
        switch (viewType) {

            case VIEW_TYPE_EXIF_DATA:
                // Populate the view with the EXIF data layout
                layoutView = inflater.inflate(R.layout.exif_item, parent, false);
                viewHolder = new ExifDataViewHolder(layoutView);
                break;
            case VIEW_TYPE_EXIF_DATA_BLANK:
                // Populate the view with the EXIF blank data layout
                layoutView = inflater.inflate(R.layout.exif_item_blank, parent, false);
                viewHolder = new ExifDataBlankViewHolder(layoutView);
                break;
            case VIEW_TYPE_EXIF_DATA_HEADER:
                // Populate the view with the sensitive EXIF data header
                layoutView = inflater.inflate(R.layout.exif_item_header, parent, false);
                viewHolder = new ExifHeaderViewHolder(layoutView);
                break;
            default:
                viewHolder = null;
        }

        return viewHolder;
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link RecyclerView.ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link RecyclerView.ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override {@link #onBindViewHolder(RecyclerView.ViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {

            case VIEW_TYPE_EXIF_DATA:
                ExifDataViewHolder exifDataViewHolder = (ExifDataViewHolder) holder;
                exifDataViewHolder.bind((FileExifData) mExifDataList.get(position));
                break;
            case VIEW_TYPE_EXIF_DATA_BLANK:
                ExifDataBlankViewHolder exifDataBlankViewHolder = (ExifDataBlankViewHolder) holder;
                exifDataBlankViewHolder.bind((FileExifDataBlank) mExifDataList.get(position));
                break;
            case VIEW_TYPE_EXIF_DATA_HEADER:
                ExifHeaderViewHolder exifHeaderViewHolder = (ExifHeaderViewHolder) holder;
                exifHeaderViewHolder.bind((FileExifDataHeader) mExifDataList.get(position));
                break;

        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        if (mExifDataList == null) {
            return 0;
        } else {
            return mExifDataList.size();
        }
    }

    /**
     * Return the view type of the item at <code>position</code> for the purposes
     * of view recycling.
     * <p>
     * <p>The default implementation of this method returns 0, making the assumption of
     * a single view type for the adapter. Unlike ListView adapters, types need not
     * be contiguous. Consider using id resources to uniquely identify item view types.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at
     * <code>position</code>. Type codes need not be contiguous.
     */
    @Override
    public int getItemViewType(int position) {
        Object object = mExifDataList.get(position);

        // If a header check to deliver the appropriate header, otherwise return EXIF data
        if (object instanceof FileExifData) {
            return VIEW_TYPE_EXIF_DATA;
        } else if (object instanceof FileExifDataBlank){
            return VIEW_TYPE_EXIF_DATA_BLANK;
        } else if (object instanceof FileExifDataHeader){
            return VIEW_TYPE_EXIF_DATA_HEADER;
        } else {
            return 2;
        }
    }

    // ExifDataAdapterOnClickHandler interface
    public interface ExifDataAdapterOnClickHandler {
        void onListItemClick(int file);
    }

    public class ExifDataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // EXIF data TextViews
        public TextView mExifDataTag;
        public TextView mExifDataValue;

        /**
         * Constructor for FileViewHolder. Obtain a reference to the layout.
         *
         * @param itemView The view for a specific item
         */
        public ExifDataViewHolder(View itemView) {
            super(itemView);

            // EXIF data text view references
            mExifDataTag = (TextView) itemView.findViewById(R.id.tv_exif_data_tag);
            mExifDataValue = (TextView) itemView.findViewById(R.id.tv_exif_data_value);
            itemView.setOnClickListener(this);
        }

        public void bind(FileExifData exifData) {
            if (exifData != null) {
                mExifDataTag.setText(exifData.getExifDataTag());
                mExifDataValue.setText(exifData.getExifDataValue());
            }
        }

        @Override
        public void onClick(View view) {
            //TODO: Potentially remove
//            int adapterPosition = getAdapterPosition();
//            mClickHandler.onListItemClick(adapterPosition);
        }
    }

    public class ExifDataBlankViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // EXIF header TextView
        public TextView mExifDataBlank;

        /**
         * Constructor for FileViewHolder. Obtain a reference to the layout.
         *
         * @param itemView The view for a specific item
         */
        public ExifDataBlankViewHolder(View itemView) {
            super(itemView);

            // EXIF header text view reference
            mExifDataBlank = (TextView) itemView.findViewById(R.id.tv_exif_data_blank);
            itemView.setOnClickListener(this);
        }

        public void bind(FileExifDataBlank exifDataBlank) {
            if (mExifDataBlank != null) {
                mExifDataBlank.setText(exifDataBlank.getNoExifDataFoundString());
            }
        }

        @Override
        public void onClick(View view) {
            //TODO: Potentially remove
//            int adapterPosition = getAdapterPosition();
//            mClickHandler.onListItemClick(adapterPosition);
        }
    }

    public class ExifHeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // EXIF header TextView
        public TextView mExifHeader;

        /**
         * Constructor for FileViewHolder. Obtain a reference to the layout.
         *
         * @param itemView The view for a specific item
         */
        public ExifHeaderViewHolder(View itemView) {
            super(itemView);

            // EXIF header text view reference
            mExifHeader = (TextView) itemView.findViewById(R.id.tv_exif_data_header);
            itemView.setOnClickListener(this);
        }

        public void bind(FileExifDataHeader exifHeader) {
            if (mExifHeader != null) {
                mExifHeader.setText(exifHeader.getExifDataStringHeader());
            }
        }

        @Override
        public void onClick(View view) {
            //TODO: Potentially remove
//            int adapterPosition = getAdapterPosition();
//            mClickHandler.onListItemClick(adapterPosition);
        }
    }

}