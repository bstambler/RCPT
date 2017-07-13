package com.RobustSystems;

import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.view.LayoutInflater;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MyAdapter<T> extends ArrayAdapter<T> {

    private int mCount = 20;
    private final LayoutInflater mLayoutInflater;
    private final int mTextViewResourceId;

    MyAdapter(Context context, int textViewResourceId) {
    	super(context, textViewResourceId);
    	
	   mLayoutInflater = LayoutInflater.from(context);
       //* mPositionString = context.getString(R.string.position) + " ";
       mTextViewResourceId = textViewResourceId;
    }

    public void addMoreItems(int count) {
        mCount += count;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public T getItem(int position) {
    	return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TextView tv;
        
        if (convertView == null) {
            tv = (TextView) mLayoutInflater.inflate (mTextViewResourceId, null);
        } else {
            tv = (TextView) convertView;
        }

        tv.setText(getItem(position).toString());
        
        return tv;
    }
}
