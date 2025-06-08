package android.example.quantummind.domain;

import java.util.List;

public interface RankingCallback {
    void onSuccess(List<RankingItem> rankingItems);  // This will pass the list of RankingItem
    void onError(String errorMessage);  // Handle errors
}
