package com.speko.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.github.bassaer.chatmessageview.models.Message;
import com.github.bassaer.chatmessageview.utils.TimeUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.speko.android.data.Chat;
import com.speko.android.data.ChatMembersColumns;
import com.speko.android.data.MessageLocal;
import com.speko.android.data.User;
import com.speko.android.data.UserColumns;
import com.speko.android.data.UsersProvider;
import com.speko.android.sync.SpekoSyncAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by rafaelalves on 31/01/17.
 */

public class Utility {

    private final String LOG_TAG = getClass().getSimpleName();
    private static User mUser;
    private static FirebaseDatabase firebaseDatabase;
    private static FirebaseUser authUser;

    private static final Map<String, String> fluentLanguageToIconNameHash;
    static {
        Hashtable<String,String> tmp =
                new Hashtable<String, String>();
        tmp.put("PT-BR","ic_pt_br");
        tmp.put("EN-US","ic_en_us");
        tmp.put("ES","ic_sp");
        fluentLanguageToIconNameHash = Collections.unmodifiableMap(tmp);
    }


    private static final Map<String, String> fluentLanguageBiggerPictureToIconNameHash;
    static {
        Hashtable<String,String> tmp =
                new Hashtable<String, String>();
        tmp.put("PT-BR","bigger_picture_language_pt_br");
        tmp.put("EN-US","bigger_picture_language_en_us");
        tmp.put("ES","bigger_picture_language_sp");
        fluentLanguageBiggerPictureToIconNameHash = Collections.unmodifiableMap(tmp);
    }



    public static final int RC_PHOTO_PICKER = 3;


    private static final String[] USER_COLUMNS = {
            UserColumns.FIREBASE_ID,
            UserColumns.NAME,
            UserColumns.EMAIL
    };
    public static final String DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss Z";


    public static @Nullable User getUser(Context context){
        //TODO should verify if it' null. In case positive should try to retrieve from DB.

        return getUserFromDB(context);

    }

    /**
     * Persists current into Firebase
     * @param user
     * @param c
     */
    public static void setUser(User user, Context c){
        mUser = user;
        authUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseDatabase==null){
            firebaseDatabase = FirebaseDatabase.getInstance();
        }
        firebaseDatabase.getReference()
                .child(c.getString(R.string.firebase_database_node_users))
                .child(authUser.getUid())
                .setValue(user);
    }

    private static User getUserFromDB(Context context){
        Cursor c = context.getContentResolver().query(UsersProvider.Users.USER_URI
        , null, UserColumns.FIREBASE_ID + " = ? ", new String[]{getFirebaseAuthUser().getUid()}, null);
        User user = new User();
        if(c.moveToFirst()){
            user.setLearningLanguage(c.getString(c.getColumnIndex(UserColumns.LEARNING_LANGUAGE)));
            user.setLearningCode(c.getString(c.getColumnIndex(UserColumns.LEARNING_CODE)));
            user.setFluentLanguage(c.getString(c.getColumnIndex(UserColumns.FLUENT_LANGUAGE)));
            user.setId(c.getString(c.getColumnIndex(UserColumns.FIREBASE_ID)));
            user.setName(c.getString(c.getColumnIndex(UserColumns.NAME)));
            user.setEmail(c.getString(c.getColumnIndex(UserColumns.EMAIL)));
            user.setAge(c.getString(c.getColumnIndex(UserColumns.AGE)));
            user.setUserDescription(c.getString(c.getColumnIndex(UserColumns.USER_DESCRIPTION)));
            user.setProfilePicture(c.getString(c.getColumnIndex(UserColumns.USER_PHOTO_URL))); ;

        }

        c.close();
        return user;
    }

    private static User getUserFriendFromDB(Context context, String id){
        Log.i("getUserFriendFromDB", "Retrieving user friend with id: " + id);

        Cursor c = context.getContentResolver().query(UsersProvider.Users.userWith(id)
                , null, null, null, null);
        User user = new User();
        if(c.moveToFirst()){
            user.setLearningLanguage(c.getString(c.getColumnIndex(UserColumns.LEARNING_LANGUAGE)));
            user.setLearningCode(c.getString(c.getColumnIndex(UserColumns.LEARNING_CODE)));
            user.setFluentLanguage(c.getString(c.getColumnIndex(UserColumns.FLUENT_LANGUAGE)));
            user.setId(c.getString(c.getColumnIndex(UserColumns.FIREBASE_ID)));
            user.setName(c.getString(c.getColumnIndex(UserColumns.NAME)));
            user.setEmail(c.getString(c.getColumnIndex(UserColumns.EMAIL)));
            user.setAge(c.getString(c.getColumnIndex(UserColumns.AGE)));

            Log.i("getUserFriendFromDB", "Retrieved user friend with id: " + user.getId());


        }

        c.close();
        return user;
    }

    public static CursorLoader getUserFriendsCursorLoader(Context context){
        //TODO remove this part, as we already have have "getFirebaseAuthUser"
         if (authUser==null){
             authUser = getFirebaseAuthUser();
         }
         return new CursorLoader(context, UsersProvider.Users.usersFriendsFrom(authUser.getUid()),
                null,
                null,
                null,
                null);
    }

    private static FirebaseUser getFirebaseAuthUser() {
        authUser = FirebaseAuth.getInstance().getCurrentUser();
        return authUser;
    }

    public static Loader<Cursor> getUserCursorLoader(Context context) {
        if (authUser == null) {
            authUser = getFirebaseAuthUser();
        }
        return new CursorLoader(context, UsersProvider.Users.USER_URI
                ,
                null,
                null,
                null,
                null);
    }


    public static String getFirebaseRoomIdWithUserID(String otherUserId, Context context) {
        String chatIdWithOtherUser = null;
        Cursor c = context.getContentResolver().
                query(UsersProvider.ChatMembers.chatInfoWithUser(otherUserId),
                        null, null, null, null);
        if (c.moveToFirst()){
            chatIdWithOtherUser = c.getString(
                    c.getColumnIndex(ChatMembersColumns.FIREBASE_CHAT_ID));
        }
        c.close();
        return chatIdWithOtherUser;
    }

    private static void createChatRoomForUsers(String id, String otherUserId) {

    }

    public static Loader getUserConversationsCursorLoader(Context context) {
        return new CursorLoader(context, UsersProvider.ChatMembers.CHAT_URI
                ,
                null,
                null,
                null,
                null);    }

    public static String createRoomForUsers(Context context, String friendId, String userID,OnCompleteListener onCompleteListener ) {
        //TODO
        Chat chat = new Chat();
        HashMap<String, User> members = new HashMap<>();
        User userFriend = getUserFriendFromDB(context, friendId);
        Log.i("createRoomForUSers", "Creating chat with \n" + userFriend +
                "\n id: " + userFriend.getId());

        members.put(userFriend.getId(), new User(userFriend.getName(), userFriend.getId()));
        User user = getUser(context);

        Log.i("createRoomForUSers", "Creating chat with \n" + user +
                "\n id: " + user.getId());


        members.put(user.getId(), new User( user.getName(),user.getId()));

        chat.setMembers(members);
        if (firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
        }
        Log.i("createRoomForUSers", "Creating chat with \n" + chat.getMembers());
        String chatId = firebaseDatabase.getReference()
                .child("chats")
                .push()
                .getKey();

        chat.setChatId(chatId);

        HashMap<String, Chat> chatHashMap = new HashMap<String, Chat>();
        chatHashMap.put(chatId, chat);
        firebaseDatabase.getReference()
                .child("chats")
                .updateChildren((Map) chatHashMap);

        firebaseDatabase.getReference()
                .child("users")
                .child(user.getId())
                .child("chats")
                .updateChildren((Map) chatHashMap).addOnCompleteListener(onCompleteListener);
        ;

        firebaseDatabase.getReference()
                .child("users")
                .child(userFriend.getId())
                .child("chats")
                .updateChildren((Map) chatHashMap);
        return chatId;
    }

    public static void call_to_upload_picture(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        activity.startActivityForResult(Intent.createChooser(intent, "Complete action using"), Utility.RC_PHOTO_PICKER);
    }

    public static void call_to_upload_picture(Fragment activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        activity.startActivityForResult(Intent.createChooser(intent, "Complete action using"), Utility.RC_PHOTO_PICKER);
    }

    /**
     * User to friends list only.
     * @param context
     * @param friendId
     * @return
     */
    public static User getOtherUserWithId(Context context, String friendId) {
        Cursor c = context.getContentResolver().query(UsersProvider.Users.USER_URI
                , null, UserColumns.FIREBASE_ID + " = ? ", new String[]{friendId}, null);
        User user = new User();
        if(c.moveToFirst()){
            user.setLearningLanguage(c.getString(c.getColumnIndex(UserColumns.LEARNING_LANGUAGE)));
            user.setLearningCode(c.getString(c.getColumnIndex(UserColumns.LEARNING_CODE)));
            user.setFluentLanguage(c.getString(c.getColumnIndex(UserColumns.FLUENT_LANGUAGE)));
            user.setId(c.getString(c.getColumnIndex(UserColumns.FIREBASE_ID)));
            user.setName(c.getString(c.getColumnIndex(UserColumns.NAME)));
            user.setEmail(c.getString(c.getColumnIndex(UserColumns.EMAIL)));
            user.setAge(c.getString(c.getColumnIndex(UserColumns.AGE)));
            user.setUserDescription(c.getString(c.getColumnIndex(UserColumns.USER_DESCRIPTION)));
            user.setProfilePicture(c.getString(c.getColumnIndex(UserColumns.USER_PHOTO_URL))); ;

        }

        c.close();
        return user;
    }

    public static MessageLocal parseToFirebaseModel(Message m) {
        String firebaseID;

        //converting ID by comparing the ids used locally by the library
        if (m.getUser().getId() == ChatActivityFragment.ME_CHATMESSAGE_ID){
            // if ID used locally is about "me"
            firebaseID = ChatActivityFragment.mIdConvertion.get(
                    ChatActivityFragment.ME_CHATMESSAGE_ID
            );
        }else{
            // if ID used locally is about the other user ("him")
            firebaseID = ChatActivityFragment.mIdConvertion.get(
                    ChatActivityFragment.HIM_CHATMESSAGE_ID
            );
        }
        MessageLocal parsedMessage = new MessageLocal();
            parsedMessage.setName(m.getUser().getName());
            parsedMessage.setmFirebaseId(firebaseID);
            parsedMessage.setDateCell(m.isDateCell());
            parsedMessage.setmCreatedAt(fromCalendarToString(m.getCreatedAt(),DATE_TIME_FORMAT));
            parsedMessage.setmHideIcon(m.isIconHided());
            parsedMessage.setmStatus(m.getStatus());
            parsedMessage.setmUsernameVisibility(m.getUsernameVisibility());
            parsedMessage.setRightMessage(m.isRightMessage());
            parsedMessage.setmIconVisibility(m.getIconVisibility());
            parsedMessage.setmMessageText(m.getMessageText());
        return parsedMessage;

    }


    // TODO this class should have all the intelligence and variables to convert these classes
    // should take  part of the information from the ChatFragment
    public static Message parseFromFirebaseModel(MessageLocal firebaseModel) {


        Calendar calendarInstance = Calendar.getInstance();

        try {
            calendarInstance.setTime(
                    parseDate(firebaseModel.getmCreatedAt(), DATE_TIME_FORMAT)
            );
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int parsedId;
        boolean isRighMessage;

        if (!firebaseModel.getmFirebaseId().equals(getFirebaseAuthUser().getUid())){
            // if the message user Firebase ID is different from the app user Firebase ID
            parsedId = ChatActivityFragment.HIM_CHATMESSAGE_ID;
            isRighMessage = false;
            Log.i("Utility", "parseFromFirebaseModel: Its a message from HIM");
        }else {
            // if they'e equal
            parsedId = ChatActivityFragment.ME_CHATMESSAGE_ID;
            isRighMessage = true;
            Log.i("Utility", "parseFromFirebaseModel: Its a message from ME");

        }
        com.github.bassaer.chatmessageview.models.User user =
                new com.github.bassaer.chatmessageview.models.User(parsedId, firebaseModel.getName(),null);
        Message parsedMessage = new Message.Builder()
                .setUser(user)
                .setRightMessage(isRighMessage)
                .setMessageStatusType(firebaseModel.getmStatus())
                .setMessageText(firebaseModel.getmMessageText())
                .setCreatedAt(calendarInstance)
                .build();
        return parsedMessage;
    }

    public static Date parseDate(String date, String format) throws ParseException
    {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.parse(date);
    }

    public static String fromCalendarToString(Calendar calendar, String format){
        return TimeUtils.calendarToString(calendar, format);

    }

    public static int getDrawableUriForLanguage(String fluentLanguage, Context mContext) {
        Log.i("getDrawableUriFor...", "the fluent language is: " + fluentLanguage);

        String uri = "@drawable/" + fluentLanguageToIconNameHash.
                get(
                fluentLanguage);
        int imageResource = mContext.getResources().getIdentifier(
                uri, null, mContext.getPackageName()
        );

        Log.i("getDrawableUriFor...", "the image URI is: " + uri);

        return imageResource;
    }

    public static boolean isValidAge(String ageString) {
        Log.i("isValidAge", "Age put is: " + ageString );
        if (ageString !=null && !ageString.equals("") ){
            int ageNumber  = Integer.valueOf(ageString);
            if (ageNumber < 1 ){
                Log.i("isValidAge", "Returning false" );
                return false;
            }
        }
        Log.i("isValidAge", "Returning true" );
        return true;
    }

    public static void deleteEverything(Context context){
        context.getContentResolver().delete(UsersProvider.Users.USER_URI,null,null);
        context.getContentResolver().delete(UsersProvider.Users
                .usersFriendsFrom(getUser(context).getId()),null,null);
        context.getContentResolver().delete(UsersProvider.ChatMembers.CHAT_URI,null,null);
    }

    public static boolean isNetworkAvailable(Context c){
        ConnectivityManager cm = (ConnectivityManager) c.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    @SuppressWarnings("ResourceType")
    static public @SpekoSyncAdapter.LocationStatus int getSyncStatus(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.shared_preference_sync_status_key),
                SpekoSyncAdapter.SYNC_STATUS_UNKNOWN);
    }

    /**
     * If active_connectivity_status is true -> is Connected = true
     * @param c
     * @return
     */
    static boolean getIsConnectedStatus(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getBoolean(c.getString(R.string.shared_preference_active_connectivity_status_key),false);
    }

    public static int getFluentLangagueBiggerPictureUri(Context mContext, String fluentLanguage) {
        Log.i("getBiggerPictureUri...", "the fluent language is: " + fluentLanguage);

        String uri = "@drawable/" + fluentLanguageBiggerPictureToIconNameHash.
                get(
                        fluentLanguage);
        int imageResource = mContext.getResources().getIdentifier(
                uri, null, mContext.getPackageName()
        );

        Log.i("getDrawableUriFor...", "the image URI is: " + uri);

        return imageResource;

    }
}
