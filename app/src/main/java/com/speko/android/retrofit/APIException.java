package com.speko.android.retrofit;

/**
 * Created by rafaelalves on 06/04/17.
 */

public class APIException extends Exception {

    public APIException(String message) {
        super(message);
    }

    public int getAPIStatusCodeMessage() {
        return Integer.valueOf(
                super.getMessage());
    }
}
