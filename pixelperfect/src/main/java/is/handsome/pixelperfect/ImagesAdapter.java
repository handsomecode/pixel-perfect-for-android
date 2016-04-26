package is.handsome.pixelperfect;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView imageView;
        public View borderView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.recycler_item_text_view);
            borderView = itemView.findViewById(R.id.recycler_item_image_container_frame_layout);
            imageView = (ImageView) itemView.findViewById(R.id.recycler_item_image_view);
        }
    }

    private Context context;
    private int widthDp;
    private int heightDp;
    private String overlayImagesAssetsPath;
    private List<String> imageNames = Collections.EMPTY_LIST;
    private SettingsView.AdapterListener listener;
    private final LayoutInflater layoutInflater;
    private int selectedPosition = -1;

    public ImagesAdapter(Context context, List<String> imageNames, String overlayImagesAssetsPath, SettingsView.AdapterListener listener) {
        this.context = context;
        this.imageNames = imageNames;
        this.overlayImagesAssetsPath = overlayImagesAssetsPath;
        this.listener = listener;

        this.widthDp = (int) context.getResources().getDimension(R.dimen.settings_screen_recycler_item_width);
        this.heightDp = (int) context.getResources().getDimension(R.dimen.settings_screen_recycler_item_height);
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ImagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.layout_recycler_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        String name = imageNames.get(position);
        holder.textView.setText(name.contains(".") ? name.substring(0, name.indexOf(".")) : name);
        Bitmap bitmap = Utils.getAdoptedBitmapFromAssets(context,
                overlayImagesAssetsPath + "/" + imageNames.get(position), widthDp, heightDp);
        if (bitmap != null) {
            holder.imageView.setImageBitmap(bitmap);
        }
        holder.textView.setSelected(position == selectedPosition);
        holder.borderView.setSelected(position == selectedPosition);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyItemChanged(selectedPosition);
                selectedPosition = holder.getAdapterPosition();
                holder.textView.setSelected(true);
                holder.borderView.setSelected(true);
                listener.onItemSelected(selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageNames.size();
    }

    public void setSelectedPosition(int newSelectedPosition) {
        notifyItemChanged(selectedPosition);
        selectedPosition = newSelectedPosition;
        notifyItemChanged(selectedPosition);
    }
}
