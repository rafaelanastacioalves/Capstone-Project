package com.speko.android.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.IfNotExists;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by rafaelalves on 14/01/17.
 */

public interface UserColumns {
    @DataType(INTEGER) @PrimaryKey @AutoIncrement
    String _ID = "_id";

    @DataType(TEXT) @NotNull @IfNotExists
    String FIREBASE_ID = "firebase_id";

    @DataType(TEXT) @NotNull @IfNotExists
    String NAME = "name";

    @DataType(TEXT) @NotNull
    String EMAIL = "email";

    @DataType(TEXT) @NotNull
    String FLUENT_LANGUAGE = "fluent_language";

    @DataType(TEXT)
    String FRIEND_OF = "friend_of";

}
