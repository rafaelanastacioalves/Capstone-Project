package com.speko.android.data;

import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.Unique;

import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by rafaelalves on 14/01/17.
 */

public interface UserColumns {
    @DataType(TEXT) @PrimaryKey @Unique
    String _ID = "id";

    @DataType(TEXT) @NotNull
    String NAME = "name";

    @DataType(TEXT) @NotNull
    String EMAIL = "email";

    @DataType(TEXT) @NotNull
    String FLUENT_LANGUAGE = "fluent_language";

}
