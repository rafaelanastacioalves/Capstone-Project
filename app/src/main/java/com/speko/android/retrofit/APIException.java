package com.speko.android.retrofit;

/**
 * Created by rafaelalves on 06/04/17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class APIException extends Exception {

    public APIException(String message) {
        super(message);
    }

    public int getAPIStatusCodeMessage() {

        try {
            if (super.getMessage() != null) {
                return Integer.valueOf(
                        super.getMessage());
            }

        } catch (NumberFormatException e) {
        }
        return -1;

    }
}
