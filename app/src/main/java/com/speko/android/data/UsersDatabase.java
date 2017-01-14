package com.speko.android.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by rafaelalves on 14/01/17.
 */

@Database(version = UsersDatabase.VERSION)
public class UsersDatabase {
    private UsersDatabase(){

    }

    public static final int VERSION = 1;

    @Table(UserColumns.class)       public static final String USERS_TABLE = "users";
    @Table(FriendshipColumns.class) public static final String FRIENDS_TABLE = "friends";

}
