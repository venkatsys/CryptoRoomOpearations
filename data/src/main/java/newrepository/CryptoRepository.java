package newrepository;

import android.arch.lifecycle.LiveData;

import java.util.List;


import models.CoinModel;

public interface CryptoRepository {
    LiveData<List<CoinModel>> getCryptoCoinsData();
    LiveData<String> getErrorStream();
    LiveData<Double> getTotalMarketCapStream();
    void fetchData();
}
