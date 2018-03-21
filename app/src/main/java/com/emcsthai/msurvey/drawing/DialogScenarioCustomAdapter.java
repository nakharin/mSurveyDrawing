package com.emcsthai.msurvey.drawing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class DialogScenarioCustomAdapter extends BaseAdapter {

    private Context mContext;
    private int[] mThumbIds;

    public DialogScenarioCustomAdapter(Context c, int[] thumbIds) {
        LayoutInflater.from(c);
        mContext = c;
        mThumbIds = thumbIds;
    }

    @Override
    public int getCount() {
        return mThumbIds.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView;

        if (convertView == null) {

            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(180, 180));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setPadding(1, 1, 1, 1);
        } else {
            imageView = (ImageView) convertView;
        }

        try {
            Glide.with(mContext)
                    .load(mThumbIds[position])
                    .fitCenter()
                    .dontAnimate()
                    .into(imageView);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        return imageView;
    }
}
