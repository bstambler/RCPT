package com.RobustSystems;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;
import android.annotation.SuppressLint;
import android.app.ListFragment;
import android.os.Bundle;
import android.os.Handler;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

@SuppressLint("NewApi")
public class AutoLoadingListFragment extends ListFragment implements OnScrollListener {

    private final int AUTOLOAD_THRESHOLD = 4;
    private final int MAXIMUM_ITEMS = 52;
    private SimpleAdapter mAdapter;
    private View mFooterView;
    private Handler mHandler;
    private boolean mIsLoading = false;
    private boolean mMoreDataAvailable = true;
    private boolean mWasLoading = false;

    private Runnable mAddItemsRunnable = new Runnable() {
        @Override
        public void run() {
            //* mAdapter.addMoreItems(10);
            mIsLoading = false;
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Context context = getActivity();
        mHandler = new Handler();
        //* mAdapter = new SimpleAdapter(context, android.R.layout.simple_list_item_1);
        //* mFooterView = LayoutInflater.from(context).inflate(R.layout.loading_view, null);
        getListView().addFooterView(mFooterView, null, false);
        setListAdapter(mAdapter);
        getListView().setOnScrollListener(this);
    }

    @SuppressLint("NewApi")
	@Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        if (!mIsLoading && mMoreDataAvailable) {
            if (totalItemCount >= MAXIMUM_ITEMS) {
                mMoreDataAvailable = false;
                getListView().removeFooterView(mFooterView);
            } else if (totalItemCount - AUTOLOAD_THRESHOLD <= firstVisibleItem + visibleItemCount) {
                mIsLoading = true;
                mHandler.postDelayed(mAddItemsRunnable, 1000);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Ignore
    }

    @SuppressLint("NewApi")
	@Override
    public void onStart() {
        super.onStart();
        if (mWasLoading) {
            mWasLoading = false;
            mIsLoading = true;
            mHandler.postDelayed(mAddItemsRunnable, 1000);
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mAddItemsRunnable);
        mWasLoading = mIsLoading;
        mIsLoading = false;
    }
}