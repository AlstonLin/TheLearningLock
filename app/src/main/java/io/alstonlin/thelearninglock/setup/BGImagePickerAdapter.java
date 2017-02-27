package io.alstonlin.thelearninglock.setup;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import io.alstonlin.thelearninglock.R;
import io.alstonlin.thelearninglock.shared.SharedUtils;

/**
 * Adapter for the Gridview when picking a picture for the background
 */

public class BGImagePickerAdapter extends BaseAdapter {
    private static final int MAX_THUMBNAIL_SIZE = 250; // Max number of pixels for height and width
    private Context context;
    private TypedArray backgrounds;

    public BGImagePickerAdapter(Context c) {
        context = c;
        backgrounds = context.getResources().obtainTypedArray(R.array.default_bgs);
    }

    public int getCount() {
        return backgrounds.length();
    }

    public Integer getItem(int position) {
        return backgrounds.getResourceId(position, -1);
    }

    public long getItemId(int position) {
        return 0;
    }

    @Override
    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setAdjustViewBounds(true);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageBitmap(SharedUtils.getResizedDrawable(context.getResources(), getItem(position), MAX_THUMBNAIL_SIZE, MAX_THUMBNAIL_SIZE));
        return imageView;
    }

}
