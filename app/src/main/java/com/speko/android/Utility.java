package com.speko.android;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.speko.android.data.Chat;
import com.speko.android.data.ChatMembersColumns;
import com.speko.android.data.User;
import com.speko.android.data.UserColumns;
import com.speko.android.data.UsersProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rafaelalves on 31/01/17.
 */

public class Utility {

    private final String LOG_TAG = getClass().getSimpleName();
    private static User mUser;
    private static FirebaseDatabase firebaseDatabase;
    private static FirebaseUser authUser;

    private static final String[] USER_COLUMNS = {
            UserColumns.FIREBASE_ID,
            UserColumns.NAME,
            UserColumns.EMAIL
    };




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
}
