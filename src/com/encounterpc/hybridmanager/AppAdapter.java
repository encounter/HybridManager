package com.encounterpc.hybridmanager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AppAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ViewGroup parent;
    private List<Drawable> icons;
    private List<String> labels;
    private List<Integer> densities;

    public AppAdapter(Context c, List<Drawable> icons, List<String> labels, List<Integer> densities) {
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.icons = icons;
        this.labels = labels;
        this.densities = densities;
    }

    @Override
    public int getCount() {
        return icons.size();
    }

    @Override
    public Object getItem(int position) {
        if (parent != null)
            return parent.getChildAt(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (this.parent == null) this.parent = parent;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listitem, null);
        }

        ((ImageView) convertView.findViewById(R.id.icon)).setImageDrawable(icons.get(position));
        ((TextView) convertView.findViewById(R.id.text)).setText(labels.get(position));
        ((TextView) convertView.findViewById(R.id.mode)).setText(densities.get(position) + "dpi");

        return convertView;
    }
}