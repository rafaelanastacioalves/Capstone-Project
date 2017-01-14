package com.speko.android.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by rafaelalves on 14/01/17.
 */

public interface FriendshipColumns {

    @DataType(TEXT) @PrimaryKey @AutoIncrement
    String _ID = "id";

    @DataType(TEXT) @NotNull
    String _USER = "user";

    @DataType(TEXT) @NotNull
    String _FRIEND = "friend";

}
