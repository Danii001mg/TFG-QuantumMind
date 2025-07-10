package android.example.quantummind.domain.callbacks;

import android.example.quantummind.domain.RankingItem;

import java.util.List;

public interface RankingCallback {
    void onSuccess(List<RankingItem> rankingItems);
    void onError(String errorMessage);
}
