package com.encounterpc.hybridmanager.adapters;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.viewpagerindicator.TitleProvider;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter implements TitleProvider {
    private Integer[] resourceIds;
    private String[] titles;
    private ViewGroup pager;
    private LayoutInflater mInflater;

    private ArrayList<View> views = new ArrayList<View>();

    public ViewPagerAdapter(Context context, String[] titles, Integer[] resourceIds) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.titles = titles;
        this.resourceIds = resourceIds;
    }

    @Override
    public String getTitle(int position) {
        return titles[position];
    }

    @Override
    public int getCount() {
        return resourceIds.length;
    }

    public View getView(ViewGroup pager, int position) {
        return views.size() > position ? views.get(position) :
                (View) instantiateItem(pager, position);
    }

    @Override
    public Object instantiateItem(ViewGroup pager, int position) {
        if (this.pager == null) this.pager = pager;

        if (views.size() > position) return views.get(position);

        View v = mInflater.inflate(resourceIds[position], null);
        pager.addView(v, position);
        views.add(position, v);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup pager, int position, Object view) {
        pager.removeView((View) view);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void finishUpdate(ViewGroup view) {
    }

    @Override
    public void restoreState(Parcelable p, ClassLoader c) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void startUpdate(ViewGroup view) {
    }
}
