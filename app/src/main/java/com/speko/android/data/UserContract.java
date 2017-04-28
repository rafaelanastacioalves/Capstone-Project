package com.speko.android.data;

import android.net.Uri;

/**
 * Created by rafaelalves on 14/01/17.
 */

@SuppressWarnings("ALL")
public class UserContract {
    public static final String CONTENT_AUTHORITY = "com.speko.android.data";

    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.speko.android.data";

    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "android.speko.com";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


}
