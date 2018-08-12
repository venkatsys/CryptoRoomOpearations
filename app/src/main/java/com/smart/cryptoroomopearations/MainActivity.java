package com.smart.cryptoroomopearations;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.smart.cryptoroomopearations.fragments.UILessFragment;
import com.smart.cryptoroomopearations.recview.Divider;
import com.smart.cryptoroomopearations.recview.MyCryptoAdapter;
import com.smart.cryptoroomopearations.screens.MainScreen;
import com.smart.cryptoroomopearations.viewmodel.CryptoViewModel;

import java.util.List;

import models.CoinModel;

public class MainActivity extends AppCompatActivity implements MainScreen{
    private static final String TAG = MainActivity.class.getSimpleName();
    private final static int DATA_FETCHING_INTERVAL=5*1000; //5 seconds
    private RecyclerView mRecyclerView;
    private MyCryptoAdapter mMyCryptoAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CryptoViewModel mCryptoViewModel;
    private long mLastFetchedDataTimeStamp;
    private final Observer<List<CoinModel>> mDataObserver = coinModels -> updateData(coinModels);
    private final Observer<String> mErrorObserver = errorMsg -> setError(errorMsg);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        mCryptoViewModel = ViewModelProviders.of(this).get(CryptoViewModel.class);
        mCryptoViewModel.getCoinsMarketData().observe(this,mDataObserver);
        mCryptoViewModel.getErrorUpdates().observe(this,mErrorObserver);
        mCryptoViewModel.fetchData();
        getSupportFragmentManager().beginTransaction().add(new UILessFragment(),"UILessFragment").commit();
    }

    private void bindViews() {
        Toolbar toolbar = this.findViewById(R.id.toolbar);
        mSwipeRefreshLayout = findViewById(R.id.swipeToRefresh);
        mRecyclerView = this.findViewById(R.id.recView);
        mSwipeRefreshLayout.setOnRefreshListener(()->{
            if(System.currentTimeMillis() - mLastFetchedDataTimeStamp < DATA_FETCHING_INTERVAL){
                Log.d(TAG, "\tNot fetching from network because interval didn't reach");
                mSwipeRefreshLayout.setRefreshing(false);
                return;
            }
            mCryptoViewModel.fetchData();
        });
        mMyCryptoAdapter = new MyCryptoAdapter();
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(lm);
        mRecyclerView.setAdapter(mMyCryptoAdapter);
        mRecyclerView.addItemDecoration(new Divider(this));
        setSupportActionBar(toolbar);
    }

    @Override
    public void updateData(List<CoinModel> data) {
        mLastFetchedDataTimeStamp=System.currentTimeMillis();
        mMyCryptoAdapter.setItems(data);
        mSwipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void setError(String msg) {
        showErrorToast(msg);
    }

    private void showErrorToast(String error) {
        Toast.makeText(this, "Error:" + error, Toast.LENGTH_LONG).show();
    }
}
