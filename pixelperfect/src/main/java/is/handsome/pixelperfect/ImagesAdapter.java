package is.handsome.pixelperfect;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import is.handsome.pixelperfect.ui.SettingsView;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {

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

    private List<PixelPerfectImage> images = Collections.EMPTY_LIST;
    private SettingsView.AdapterListener listener;
    private final LayoutInflater layoutInflater;
    private int selectedPosition = -1;

    public ImagesAdapter(Context context, List<PixelPerfectImage> images, SettingsView.AdapterListener listener) {
        this.images = images;
        this.layoutInflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @Override
    public ImagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.layout_recycler_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        String name = images.get(position).name;
        holder.textView.setText(name.contains(".") ? name.substring(0, name.indexOf(".")) : name);
        if (images.get(position).bitmap != null) {
            holder.imageView.setImageBitmap(images.get(position).bitmap);
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
        return images.size();
    }

    public void setSelectedPosition(int newSelectedPosition) {
        notifyItemChanged(selectedPosition);
        selectedPosition = newSelectedPosition;
        notifyItemChanged(selectedPosition);
    }
}