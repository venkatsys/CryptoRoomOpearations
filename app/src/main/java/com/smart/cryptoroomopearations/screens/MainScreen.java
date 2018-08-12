package com.smart.cryptoroomopearations.screens;




import java.util.List;

import models.CoinModel;

public interface MainScreen {
    void updateData(List<CoinModel> data);
    void setError(String msg);
}
