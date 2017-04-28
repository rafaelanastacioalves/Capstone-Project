package com.speko.android.retrofit;

/**
 * Created by rafaelalves on 05/01/17.
 */
@SuppressWarnings("DefaultFileTemplate")
public class AccessToken {
    @SuppressWarnings("CanBeFinal")
    private String accessToken;
    private String tokenType;

    @SuppressWarnings("SameParameterValue")
    public AccessToken(String tokenType, String accessToken){
        this.tokenType = tokenType;
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        // OAuth requires uppercase Authorization HTTP header value for token type
        if ( ! Character.isUpperCase(tokenType.charAt(0))) {
            tokenType =
                    Character
                            .toString(tokenType.charAt(0))
                            .toUpperCase() + tokenType.substring(1);
        }

        return tokenType;
    }
}
