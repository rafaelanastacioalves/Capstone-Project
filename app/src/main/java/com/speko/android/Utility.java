package com.speko.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import com.speko.android.data.UserColumns;
import com.speko.android.data.UserComplete;
import com.speko.android.data.UserPublic;
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

@SuppressWarnings("ALL")
public class Utility {

    private final String LOG_TAG = getClass().getSimpleName();
    private static FirebaseDatabase firebaseDatabase;
    private static FirebaseUser authUser;

    private static final Map<String, String> fluentLanguageToIconNameHash;
    static {
        Hashtable<String,String> tmp =
                new Hashtable<String, String>();
        tmp.put("PT-BR","ic_pt_br");
        tmp.put("EN-US","ic_en_us");
        tmp.put("SP","ic_sp");
        fluentLanguageToIconNameHash = Collections.unmodifiableMap(tmp);
    }


    private static final Map<String, String> fluentLanguageBiggerPictureToIconNameHash;
    static {
        Hashtable<String,String> tmp =
                new Hashtable<String, String>();
        tmp.put("PT-BR","bigger_picture_language_pt_br");
        tmp.put("EN-US","bigger_picture_language_en_us");
        tmp.put("SP","bigger_picture_language_sp");
        fluentLanguageBiggerPictureToIconNameHash = Collections.unmodifiableMap(tmp);
    }


    private static final Map<String, String> completeLanguageToCompleteLanguageHash;
    static {
        Hashtable<String,String> tmp =
                new Hashtable<String, String>();
        tmp.put("PT-BR","complete_language_pt_br");
        tmp.put("EN-US","complete_language_en_us");
        tmp.put("SP","complete_language_sp");
        completeLanguageToCompleteLanguageHash = Collections.unmodifiableMap(tmp);
    }

    public static final int RC_PHOTO_PICKER = 3;


    private static final String[] USER_COLUMNS = {
            UserColumns.FIREBASE_ID,
            UserColumns.NAME,
            UserColumns.EMAIL
    };
    private static final String DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss Z";


    public static @Nullable
    UserComplete getUser(Context context){
        //TODO should verify if it' null. In case positive should try to retrieve from DB.

        return getUserFromDB(context);

    }



    /**
     * Persists current into Firebase
     * @param userComplete
     * @param c
     */
    public static void setUserIntoFirebase(UserComplete userComplete, Context c, OnCompleteListener onCompleteListener){
        authUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseDatabase==null){
            firebaseDatabase = FirebaseDatabase.getInstance();
        }

        HashMap<String, Object> userHashMap = new HashMap<>();
        userHashMap.put(authUser.getUid(), userComplete);
        firebaseDatabase.getReference()
                .child(c.getString(R.string.firebase_database_node_users))
                .updateChildren(userHashMap).addOnCompleteListener(onCompleteListener);
    }



    private static UserComplete getUserFromDB(Context context){
        FirebaseUser fireBaseUser = getFirebaseAuthUser();
        if(fireBaseUser == null){
            Log.w("Utility", "There is no firebase user!");
            return null;
        }
        Cursor c = context.getContentResolver().query(UsersProvider.Users.USER_URI
        , null, UserColumns.FIREBASE_ID + " = ? ", new String[]{fireBaseUser.getUid()}, null);
        UserComplete userComplete = null;

        //noinspection ConstantConditions
        if( c.moveToFirst()){
            userComplete = new UserComplete();
            userComplete.setLearningLanguage(c.getString(c.getColumnIndex(UserColumns.LEARNING_LANGUAGE)));
            userComplete.setLearningCode(c.getString(c.getColumnIndex(UserColumns.LEARNING_CODE)));
            userComplete.setFluentLanguage(c.getString(c.getColumnIndex(UserColumns.FLUENT_LANGUAGE)));
            userComplete.setId(c.getString(c.getColumnIndex(UserColumns.FIREBASE_ID)));
            userComplete.setName(c.getString(c.getColumnIndex(UserColumns.NAME)));
            userComplete.setEmail(c.getString(c.getColumnIndex(UserColumns.EMAIL)));
            userComplete.setAge(c.getString(c.getColumnIndex(UserColumns.AGE)));
            userComplete.setUserDescription(c.getString(c.getColumnIndex(UserColumns.USER_DESCRIPTION)));
            userComplete.setProfilePicture(c.getString(c.getColumnIndex(UserColumns.USER_PHOTO_URL)));
            userComplete.setChats(getUserConversationsFromDB(context, userComplete));

        }

        c.close();
        return userComplete;
    }

    /**
     * Same as {@link #getOtherUserWithId(Context, String)}
     * @param context
     * @param id
     * @return
     */
    private static UserComplete getUserFriendFromDB(Context context, String id){
        Log.i("getUserFriendFromDB", "Retrieving user friend with id: " + id);

        Cursor c = context.getContentResolver().query(UsersProvider.Users.userWith(id)
                , null, null, null, null);
        UserComplete userComplete = null ;
        //noinspection ConstantConditions
        if( c != null && c.moveToFirst()){
            userComplete = new UserComplete();
            userComplete.setLearningLanguage(c.getString(c.getColumnIndex(UserColumns.LEARNING_LANGUAGE)));
            userComplete.setLearningCode(c.getString(c.getColumnIndex(UserColumns.LEARNING_CODE)));
            userComplete.setFluentLanguage(c.getString(c.getColumnIndex(UserColumns.FLUENT_LANGUAGE)));
            userComplete.setId(c.getString(c.getColumnIndex(UserColumns.FIREBASE_ID)));
            userComplete.setName(c.getString(c.getColumnIndex(UserColumns.NAME)));
            userComplete.setEmail(c.getString(c.getColumnIndex(UserColumns.EMAIL)));
            userComplete.setAge(c.getString(c.getColumnIndex(UserColumns.AGE)));

            Log.i("getUserFriendFromDB", "Retrieved user friend with id: " + userComplete.getId());

        }


        c.close();
        return userComplete;
    }

    /**
     *
     * @param context
     * @param mainUserComplete
     * @return Chats hashmap or null if there is none
     */
    private static HashMap<String, Chat> getUserConversationsFromDB(Context context, UserComplete mainUserComplete){
        Log.i("getUserConvFromDB", "Retrieving user friend with id: " + mainUserComplete.getId());
        Cursor c = context.getContentResolver().query(UsersProvider.ChatMembers.CHAT_URI
                , null, null, null, null);
        HashMap<String, Chat> chatsHashMap = null;
        Chat chat;
        //noinspection ConstantConditions
        if (c!= null && c.moveToFirst()){
            //noinspection ConstantConditions
            if (chatsHashMap == null) {
                chatsHashMap = new HashMap<>();
            }

            do {
                chat = new Chat();
                HashMap<String, UserPublic> members = new HashMap<>();

                UserPublic userCompleteFriend = new UserPublic(
                        c.getString(c.getColumnIndex(ChatMembersColumns.OTHER_MEMBER_ID)));



                members.put(userCompleteFriend.getId(), userCompleteFriend);



                members.put(mainUserComplete.getId(), new UserComplete( mainUserComplete.getId()));

                chat.setMembers(members);
                chat.setChatId(c.getString(c.getColumnIndex(ChatMembersColumns.FIREBASE_CHAT_ID)));
                chatsHashMap.put(chat.getChatId(), chat);
            } while (c.moveToNext());


        }

        c.close();



        return chatsHashMap;
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
        //noinspection ConstantConditions
        if (c != null && c.moveToFirst()){
            chatIdWithOtherUser = c.getString(
                    c.getColumnIndex(ChatMembersColumns.FIREBASE_CHAT_ID));
        }
        c.close();
        return chatIdWithOtherUser;
    }

    public static Loader<Cursor> getUserConversationsCursorLoader(Context context) {
        return new CursorLoader(context, UsersProvider.ChatMembers.CHAT_URI
                ,
                null,
                null,
                null,
                null);    }

    @SuppressWarnings("UnusedParameters")
    public static String createRoomForUsers(Context context, String friendId, String userID, OnCompleteListener onCompleteListener ) {
        //TODO
        Chat chat = new Chat();
        HashMap<String, UserPublic> members = new HashMap<>();
        UserPublic userPublicFriend = getUserFriendFromDB(context, friendId);
        Log.i("createRoomForUSers", "Creating chat with \n" + userPublicFriend +
                "\n id: " + userPublicFriend.getId());

        members.put(userPublicFriend.getId(), new UserPublic(userPublicFriend.getId()));
        UserPublic userPublic = getUser(context);

        //noinspection ConstantConditions
        Log.i("createRoomForUSers", "Creating chat with \n" + userPublic +
                "\n id: " + userPublic.getId());


        members.put(userPublic.getId(), new UserPublic( userPublic.getId()));

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
        //noinspection unchecked
        firebaseDatabase.getReference()
                .child("chats")
                .updateChildren((Map) chatHashMap);

        //noinspection unchecked
        firebaseDatabase.getReference()
                .child("users")
                .child(userPublic.getId())
                .child("chats")
                .updateChildren((Map) chatHashMap).addOnCompleteListener(onCompleteListener);

        //noinspection unchecked
        firebaseDatabase.getReference()
                .child("users")
                .child(userPublicFriend.getId())
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
    public static UserComplete getOtherUserWithId(Context context, String friendId) {
        Cursor c = context.getContentResolver().query(UsersProvider.Users.USER_URI
                , null, UserColumns.FIREBASE_ID + " = ? ", new String[]{friendId}, null);
        UserComplete userComplete = new UserComplete();
        //noinspection ConstantConditions
        if(c.moveToFirst()){
            userComplete.setLearningLanguage(c.getString(c.getColumnIndex(UserColumns.LEARNING_LANGUAGE)));
            userComplete.setLearningCode(c.getString(c.getColumnIndex(UserColumns.LEARNING_CODE)));
            userComplete.setFluentLanguage(c.getString(c.getColumnIndex(UserColumns.FLUENT_LANGUAGE)));
            userComplete.setId(c.getString(c.getColumnIndex(UserColumns.FIREBASE_ID)));
            userComplete.setName(c.getString(c.getColumnIndex(UserColumns.NAME)));
            userComplete.setEmail(c.getString(c.getColumnIndex(UserColumns.EMAIL)));
            userComplete.setAge(c.getString(c.getColumnIndex(UserColumns.AGE)));
            userComplete.setUserDescription(c.getString(c.getColumnIndex(UserColumns.USER_DESCRIPTION)));
            userComplete.setProfilePicture(c.getString(c.getColumnIndex(UserColumns.USER_PHOTO_URL)));

        }

        c.close();
        return userComplete;
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
        return new Message.Builder()
                .setUser(user)
                .setRightMessage(isRighMessage)
                .setMessageStatusType(firebaseModel.getmStatus())
                .setMessageText(firebaseModel.getmMessageText())
                .setCreatedAt(calendarInstance)
                .build();
    }

    @SuppressWarnings("SameParameterValue")
    private static Date parseDate(String date, String format) throws ParseException
    {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.parse(date);
    }

    @SuppressWarnings("SameParameterValue")
    private static String fromCalendarToString(Calendar calendar, String format){
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

    public static String getCompleteLanguageNameString(String language, Context mContext){
        Log.i("getCompleteLangUriF...", "the fluent language is: " + language);
        String uri = "@string/" + completeLanguageToCompleteLanguageHash.
                get(
                language);
        Log.i("getCompleteLangUriF...", "the image URI is: " + uri);

        int languageResource = mContext.getResources().getIdentifier(
                uri, null, mContext.getPackageName()
        );

        return mContext.getString(languageResource);

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
        if (authUser == null) {
            authUser = getFirebaseAuthUser();
        }
        //noinspection ConstantConditions
        context.getContentResolver().delete(UsersProvider.Users
                .usersFriendsFrom(authUser.getUid()),null,null);

        context.getContentResolver().delete(UsersProvider.Users.USER_URI,null,null);

        context.getContentResolver().delete(UsersProvider.ChatMembers.CHAT_URI,null,null);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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

    public static int getFluentLangagueBiggerPictureUri(Context mContext, @NonNull String fluentLanguage) {
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

    // A method to find height of the status bar
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
