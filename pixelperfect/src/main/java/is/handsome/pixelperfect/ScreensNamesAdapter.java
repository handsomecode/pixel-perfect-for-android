package is.handsome.pixelperfect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.IOException;

public class ScreensNamesAdapter extends BaseAdapter {

    private static final String IMAGES_DIRECTORY = "pixelperfect";

    private String[] filenames;
    private LayoutInflater layoutInflater;

    public ScreensNamesAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
        try {
            filenames = context.getAssets().list(IMAGES_DIRECTORY);
        } catch (IOException e) {
            filenames = new String[0];
        }
    }

    @Override
    public int getCount() {
        return filenames.length + 1;
    }

    @Override
    public Object getItem(int position) {
        return position == 0 ? null : IMAGES_DIRECTORY + "/" + filenames[position - 1];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        } else {
            view = convertView;
        }
        ((TextView) view).setText(getTitle(position));
        return view;
    }

    private String getTitle(int position) {
        return "none";
        /*if (position == 0) {
            return "none";
        } else {
            return filenames[position - 1].substring(0, filenames[position - 1].indexOf("."));
        }*/
    }
}
