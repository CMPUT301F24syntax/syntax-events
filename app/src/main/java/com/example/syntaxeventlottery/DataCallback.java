
package com.example.syntaxeventlottery;

import java.util.List;

/**
 * The {@code DataCallback} interface defines a callback mechanism for asynchronous operations.
 * It is used to handle the success and error states of an operation.
 *
 * @param <T> The type of the result expected on success.
 */
public interface DataCallback<T> {

    /**
     * Called when the operation completes successfully.
     *
     * @param result The result of the operation.
     */
    void onSuccess(T result);

    /**
     * Called when the operation encounters an error.
     *
     * @param e The exception representing the error.
     */
    void onError(Exception e);
}
