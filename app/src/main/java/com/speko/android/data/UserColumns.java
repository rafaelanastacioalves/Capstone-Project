package com.speko.android.data;

import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.IfNotExists;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.Unique;

import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by rafaelalves on 14/01/17.
 */

public interface UserColumns {

    @DataType(TEXT) @PrimaryKey @Unique
    String FIREBASE_ID = "firebase_id";

    @DataType(TEXT) @NotNull @IfNotExists
    String NAME = "name";

    @DataType(TEXT) @IfNotExists
    String AGE = "age";


    @DataType(TEXT) @NotNull
    String EMAIL = "email";

    @DataType(TEXT) @NotNull
    String FLUENT_LANGUAGE = "fluent_language";

    @DataType(TEXT) @NotNull
    String LEARNING_LANGUAGE = "learning_language";

    @DataType(TEXT)
    String FRIEND_OF = "friend_of";

    @DataType(TEXT) @NotNull
    String LEARNING_CODE = "learning_code";

    @DataType(TEXT) @IfNotExists
    String USER_DESCRIPTION = "user_description";

    @DataType(TEXT) @IfNotExists
    String USER_PHOTO_URL = "user_photo_url";



}
