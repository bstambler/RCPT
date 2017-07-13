package com.RobustSystems;

import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import android.view.LayoutInflater;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public abstract class TwoLineArrayAdapter<T> extends ArrayAdapter<T> {
	
    private int mListItemLayoutResId;
    private int mTxt1ResId;
    private int mTxt2ResId;

    public TwoLineArrayAdapter(Context context, T[] ts) {
        //* this(context, android.R.layout.two_line_list_item, ts);
        this(context, android.R.layout.two_line_list_item, android.R.id.text1, android.R.id.text2, ts);
    }
    
    public TwoLineArrayAdapter(Context context, ArrayList<T> ts) {
        this(context, android.R.layout.two_line_list_item, android.R.id.text1, android.R.id.text2, ts);
    }

    public TwoLineArrayAdapter(
            Context context, 
            int listItemLayoutResourceId, int txt1ResId, int txt2ResId,
            T[] ts) {
        super(context, listItemLayoutResourceId, ts);
        mListItemLayoutResId = listItemLayoutResourceId;
        mTxt1ResId = txt1ResId;
        mTxt2ResId = txt2ResId;
    }
    
    public TwoLineArrayAdapter(
            Context context, 
            int listItemLayoutResourceId, int txt1ResId, int txt2ResId,
            ArrayList<T> ts) {
        super(context, listItemLayoutResourceId, ts);
        mListItemLayoutResId = listItemLayoutResourceId;
        mTxt1ResId = txt1ResId;
        mTxt2ResId = txt2ResId;
    }

    @Override
    public android.view.View getView(
            int position, 
            View convertView,
            ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)getContext()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View listItemView = convertView;
        if (null == convertView) { 
            listItemView = inflater.inflate(
                mListItemLayoutResId, 
                parent, 
                false);
        }

        // The ListItemLayout must use the standard text item IDs.
        TextView lineOneView = (TextView)listItemView.findViewById(mTxt1ResId);
        TextView lineTwoView = (TextView)listItemView.findViewById(mTxt2ResId);

        T t = (T)getItem(position);
        
        lineOneView.setText(lineOneText(t));
        lineTwoView.setText(lineTwoText(t));

        return listItemView;
    }   

    public abstract String lineOneText(T t);

    public abstract String lineTwoText(T t);
}