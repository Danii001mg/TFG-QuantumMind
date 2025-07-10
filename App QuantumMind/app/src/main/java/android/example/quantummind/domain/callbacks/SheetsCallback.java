package android.example.quantummind.domain.callbacks;

import android.example.quantummind.domain.entities.Sheet;
import java.util.List;

public interface SheetsCallback {
    void onSuccess(List<Sheet> sheets);
    void onError(String errorMessage);
}