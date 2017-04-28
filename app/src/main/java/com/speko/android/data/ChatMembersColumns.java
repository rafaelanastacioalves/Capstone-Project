package com.speko.android.data;

import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.IfNotExists;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.Unique;

import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by rafaelalves on 14/01/17.
 */

@SuppressWarnings("DefaultFileTemplate")
public interface ChatMembersColumns {

    @DataType(TEXT) @PrimaryKey @Unique
    String FIREBASE_CHAT_ID = "firebase_chat_id";

    @DataType(TEXT)
    String OTHER_MEMBER_ID = "other_member_id";

    @DataType(TEXT)
    String OTHER_MEMBER_NAME = "other_member_name";

    @DataType(TEXT) @IfNotExists
    String OTHER_MEMBER_PHOTO_URL = "other_member_photo_url";

    @DataType(TEXT) @IfNotExists
    String OTHER_MEMBER_FLUENT_LANGUAGE = "other_member_fluent_language";

}
