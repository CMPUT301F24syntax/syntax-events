
package com.example.syntaxeventlottery;

import java.util.List;

public interface DataCallback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}