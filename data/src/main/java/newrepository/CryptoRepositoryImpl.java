package newrepository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Transformations;
import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import datasource.LocalRoomDataSource;
import datasource.RemoteDataSource;
import entities.CryptoCoinEntity;
import mappers.CryptoMapper;
import models.CoinModel;

public class CryptoRepositoryImpl implements CryptoRepository {

    private static final String TAG = CryptoRepositoryImpl.class.getSimpleName();
    private ExecutorService mExecutor = Executors.newFixedThreadPool(5);
    private final RemoteDataSource mRemoteDataSource;
    private final LocalRoomDataSource mLocalDataSource;
    private CryptoMapper mMapper;
    MediatorLiveData<List<CoinModel>> mDataMerger = new MediatorLiveData<>();
    MediatorLiveData<String> mErrorMerger = new MediatorLiveData<>();


    private CryptoRepositoryImpl(RemoteDataSource mRemoteDataSource, LocalRoomDataSource mLocalDataSource, CryptoMapper mapper) {
        this.mRemoteDataSource = mRemoteDataSource;
        this.mLocalDataSource = mLocalDataSource;
        this.mMapper = mapper;
        mDataMerger.addSource(this.mRemoteDataSource.getDataStream(),entities -> {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    //mLocalDataSource.writeData(mMapper.mapEntitiesToString(entities));
                    mLocalDataSource.writeData(entities);
                    List<CoinModel> list = mMapper.mapEntityToModel(entities);
                    mDataMerger.postValue(list);
                }
            });
        });

/*        mDataMerger.addSource(this.mLocalDataSource.getDataStream(),json -> {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    List<CryptoCoinEntity> entities = mMapper.mapJSONToEntity(json.toString());
                    List<CoinModel> models = mMapper.mapEntityToModel(entities);
                    mDataMerger.postValue(models);
                }
            });
        });*/

/*        mDataMerger.addSource(this.mRemoteDataSource.getErrorStream() , errorStr -> {
            mErrorMerger.setValue(errorStr);
            Log.d(TAG, "Network error -> fetching from LocalDataSource");
            mLocalDataSource.fetch();
        });*/

        mErrorMerger.addSource(mRemoteDataSource.getErrorStream(), errorStr -> {
                    mErrorMerger.setValue(errorStr);
                    Log.d(TAG, "Network error -> fetching from LocalDataSource");
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            List<CryptoCoinEntity> entities = (mLocalDataSource.getALlCoins());
                            mDataMerger.postValue(mMapper.mapEntityToModel(entities));
                        }
                    });

                }
        );
        mErrorMerger.addSource(mLocalDataSource.getErrorStream(), errorStr -> mErrorMerger.setValue(errorStr));
    }

    public static CryptoRepository create(Context mAppContext){
        final CryptoMapper mapper = new CryptoMapper();
        final RemoteDataSource remoteDataSource = new RemoteDataSource(mAppContext, mapper);
        final LocalRoomDataSource localDataSource = new LocalRoomDataSource(mAppContext);
        return new CryptoRepositoryImpl(remoteDataSource,localDataSource,mapper);
    }

    @Override
    public LiveData<List<CoinModel>> getCryptoCoinsData() {
        return mDataMerger;
    }

    @Override
    public LiveData<String> getErrorStream() {
        return mErrorMerger;
    }

    @Override
    public LiveData<Double> getTotalMarketCapStream() {
        return Transformations.map(mDataMerger,input -> {
            double totalMarketCap = 0;
            for (int i = 0; i < input.size(); i++) {
                totalMarketCap += input.get(i).marketCap;
            }
            return totalMarketCap;
        });
    }

    @Override
    public void fetchData() {
        mRemoteDataSource.fetch();
    }
}
