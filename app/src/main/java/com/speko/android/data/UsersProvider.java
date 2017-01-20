package com.speko.android.data;


import android.content.ContentResolver;
import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

import static com.speko.android.data.UserContract.CONTENT_AUTHORITY;

/**
 * Created by rafaelalves on 19/12/16.
 */

@ContentProvider(authority = CONTENT_AUTHORITY, database = UsersDatabase.class)
public final class UsersProvider {

    public static final Uri URI
            = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(CONTENT_AUTHORITY).build();

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    interface Path{
        String USER = "user";
        String FRIENDS = "friends";

    }

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table = UsersDatabase.USERS_TABLE) public static class Users {
        @ContentUri(
                path = Path.USER,
                type = "vnd.android.cursor.dir/user")
        public static final Uri USER_URI = buildUri(Path.USER);
        @InexactContentUri(
                name = "FRIENDS_LIST",
                path = Path.USER + "/" + Path.FRIENDS + "/*",
                type = "vnd.android.cursor.item/friends",
                whereColumn = UserColumns.FRIEND_OF,
                pathSegment = 2
        )
        public static final Uri usersFrom(String firebaseUserId){
            return buildUri(Path.USER,Path.FRIENDS, firebaseUserId);
        }
    }


}
