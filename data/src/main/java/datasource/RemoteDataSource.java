package datasource;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

import entities.CryptoCoinEntity;
import mappers.CryptoMapper;

public class RemoteDataSource implements DataSource<List<CryptoCoinEntity>> {

    private static final String TAG = RemoteDataSource.class.getSimpleName();
    public final String ENDPOINT_FETCH_CRYPTO_DATA = "https://api.coinmarketcap.com/v1/ticker/?limit=100";
    private final RequestQueue mQueue;
    private final CryptoMapper mObjMapper;
    private MutableLiveData<List<CryptoCoinEntity>> mDataApi = new MutableLiveData<>();
    private MutableLiveData<String> mError = new MutableLiveData<>();

    public RemoteDataSource(Context appContext, CryptoMapper mObjMapper) {
        this.mQueue = Volley.newRequestQueue(appContext);
        this.mObjMapper = mObjMapper;
    }

    @Override
    public LiveData<List<CryptoCoinEntity>> getDataStream() {
        return mDataApi;
    }

    @Override
    public LiveData<String> getErrorStream() {
        return mError;
    }

    public void fetch() {
        final JsonArrayRequest jsonObjReq =
                new JsonArrayRequest(ENDPOINT_FETCH_CRYPTO_DATA ,
                        response -> {
                            Log.d(TAG, "Thread->" +
                                    Thread.currentThread().getName()+"\tGot some network response");
                            final ArrayList<CryptoCoinEntity> data = mObjMapper.mapJSONToEntity(response.toString());
                            mDataApi.setValue(data);
                        },
                        error -> {
                            Log.d(TAG, "Thread->" +
                                    Thread.currentThread().getName()+"\tGot network error");
                            mError.setValue(error.toString());
                        });
        mQueue.add(jsonObjReq);
    }
}
