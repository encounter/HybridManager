package com.encounterpc.hybridmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.encounterpc.hybridmanager.R;
import com.encounterpc.hybridmanager.util.AppInfo;

import java.util.List;

public class AppListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ViewGroup parent;
    private List<AppInfo> apps;
    private int defaultDensity;

    public AppListAdapter(Context c, List<AppInfo> apps, int defaultDensity) {
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.apps = apps;
        this.defaultDensity = defaultDensity;
    }

    @Override
    public int getCount() {
        return apps.size();
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

        AppInfo app = apps.get(position);
        ((ImageView) convertView.findViewById(R.id.icon)).setImageDrawable(app.getIcon());
        ((TextView) convertView.findViewById(R.id.text)).setText(app.getLabel());
        ((TextView) convertView.findViewById(R.id.mode)).setText(app.getDensity(defaultDensity) + "dpi");

        return convertView;
    }
}