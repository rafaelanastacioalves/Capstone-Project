package com.speko.android.data;

import android.net.Uri;

/**
 * Created by rafaelalves on 14/01/17.
 */

public class UserContract {
    public static final String CONTENT_AUTHORITY = "com.speko.android.data";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


}
