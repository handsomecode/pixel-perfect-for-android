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

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder>  {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.recycler_item_text_view);
            imageView = (ImageView) itemView.findViewById(R.id.recycler_item_image_view);
        }
    }

    private List<MockupImage> images = Collections.EMPTY_LIST;
    private SettingsView.AdapterListener listener;
    private final LayoutInflater layoutInflater;

    public ImagesAdapter(Context context, List<MockupImage> images, SettingsView.AdapterListener listener) {
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
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.textView.setText(images.get(position).name);
        holder.imageView.setImageBitmap(images.get(position).bitmap);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemSelected(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }
}