package com.speko.android.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncInfo;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.speko.android.R;
import com.speko.android.Utility;
import com.speko.android.data.Chat;
import com.speko.android.data.ChatMembersColumns;
import com.speko.android.data.User;
import com.speko.android.data.UserColumns;
import com.speko.android.data.UserContract;
import com.speko.android.data.UsersProvider;
import com.speko.android.retrofit.APIException;
import com.speko.android.retrofit.AccessToken;
import com.speko.android.retrofit.ErrorUtils;
import com.speko.android.retrofit.FirebaseClient;
import com.speko.android.retrofit.ServiceGenerator;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

import static android.content.Context.ACCOUNT_SERVICE;
import static com.speko.android.data.UserColumns.FIREBASE_ID;
import static com.speko.android.data.UserContract.ACCOUNT_TYPE;
import static com.speko.android.data.UserContract.AUTHORITY;
import static com.speko.android.data.UsersProvider.ChatMembers.CHAT_URI;
import static com.speko.android.data.UsersProvider.Users.USER_URI;
import static com.speko.android.sync.SpekoAuthenticator.ACCOUNT;


/**
 * Created by rafaelalves on 14/12/16.
 */
public class SpekoSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final int SYNC_INTERVAL = 1000; //every minute
    private static final int FLEX_TIME = 1000; // every minute
    private static final String LOG = "SpekoSyncAdapter";
    private static FirebaseDatabase mFirebaseDatabase;
    private static FirebaseAuth mFirebaseAuth;
    private static final String LOG_TAG = "SpekoSyncAdapter";
    private static String userToken;
    private static User user;


    public static final String ACTION_DATA_UPDATED =
            "com.speko.android.ACTION_DATA_UPDATED";


    public SpekoSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.i(LOG_TAG, "Constructor Called");

    }

    public SpekoSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        Log.i(LOG_TAG, "Constructor Called");

    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SYNC_STATUS_OK, SYNC_STATUS_SERVER_DOWN, SYNC_STATUS_INVALID, SYNC_STATUS_SERVER_ERROR,
            SYNC_STATUS_UNKNOWN})
    public @interface LocationStatus {
    }

    public static final int SYNC_STATUS_OK = 0;
    public static final int SYNC_STATUS_SERVER_DOWN = 1;
    public static final int SYNC_STATUS_SERVER_ERROR = 2;
    public static final int SYNC_STATUS_UNKNOWN = 3;
    public static final int SYNC_STATUS_INVALID = 4;

    public static void setUserToken(String userToken) {
        SpekoSyncAdapter.userToken = userToken;
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.i(LOG_TAG, "onPerformSync");


        if (userToken != null) {
            User user = null;
            try {

                Log.i("SpekoSyncAdapter", "getUser... ");
                user = getUser(userToken);
                //in case of user null - logout - just stop
                if (user == null) {
                    return;
                }

                Log.i("SpekoSyncAdapter", "getOtherUSerPhotofrom... ");
                getOtherUsersPhotofrom(user);

                HashMap<String, User> userFriends = null;
                Log.i("SpekoSyncAdapter", "getFriends... ");
                userFriends = getFriends(userToken);

                if (userFriends != null) {
                    // persisting everything in database
                    persistUser(user);
                    persistChatListFrom(user);
                    persistFriends(userFriends.values().toArray(new User[userFriends.size()]));
                    Log.i("SpekoSyncAdapter", "Deu certo!: \n" + user.toString());

                }


            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                setSyncStatus(getContext(), SYNC_STATUS_SERVER_DOWN);
            } catch (APIException e) {
                int responseCode = e.getAPIStatusCodeMessage();
                if (responseCode >= 400 || responseCode < 500) {
                    setSyncStatus(getContext(), SYNC_STATUS_SERVER_DOWN);

                }
                if (responseCode >= 500 || responseCode < 600) {
                    setSyncStatus(getContext(), SYNC_STATUS_SERVER_ERROR);

                }
            } catch (Exception e){
                Log.e(LOG_TAG, e.getMessage());
                setSyncStatus(getContext(), SYNC_STATUS_UNKNOWN);
            }
        } else {
            Log.w(LOG_TAG, "userToken not setted!");
        }

        updateWidgets();

    }

    private void updateWidgets() {
        Log.i(LOG, "updateWidgets");

        Context context = getContext();
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    /**
     * Other users chat members pictures need another call for each one, as we don't work with
     * relational database in firebase.
     **/
    private void getOtherUsersPhotofrom(User user) throws IOException, APIException {
        HashMap<String, Chat> chats = user.getChats();
        if (chats != null) {
            for (String chatKey : chats.keySet()) {
                HashMap<String, User> membersHashMap = chats.get(chatKey).getMembers();
                for (String otherUserId : membersHashMap.keySet()) {
                    if (!otherUserId.equals(user.getId())) {
                        String profilePictureUrl = getProfilePictureForUserId(otherUserId, userToken);
                        String fluentLanguage = geFluentLanguageForUserId(otherUserId, userToken);

                        User otherUser = membersHashMap.get(otherUserId);

                        // updating members hashmap with updated User java object
                        otherUser.setProfilePicture(profilePictureUrl);
                        otherUser.setFluentLanguage(fluentLanguage);
                        membersHashMap.put(otherUserId, otherUser);
                    }
                }

                // updating chat hashmap with updated chat java object
                Chat chat = chats.get(chatKey);
                chat.setMembers(membersHashMap);
                chats.put(chatKey, chat);
            }
        }

    }

    private String geFluentLanguageForUserId(String otherUserId, String userToken) throws IOException, APIException {
        FirebaseClient client = ServiceGenerator.createService(FirebaseClient.class, new AccessToken(
                "Bearer",
                userToken)
        );

        if (mFirebaseAuth.getCurrentUser().getUid() == null) {
            Log.w(LOG_TAG, "method with null User variable!");
            return null;
        }
        Call<String> call = client.getUserFluentLanguage(otherUserId, userToken);

        Log.i("SpekoSyncAdapter", "getUser: \n");
        Response<String> response = call.execute();
        if (response.isSuccessful()) {
            String userPictureUrl = response.body();
            if (userPictureUrl != null) {
                Log.i("SpekoSyncAdapter", "Deu certo!: \n" + user.toString());

                return userPictureUrl;
            }
        } else {
            Log.e(LOG_TAG, "Response not successfull");
            throw new APIException(String.valueOf(
                    ErrorUtils.parseError(response).status())
            );
        }

        return null;

    }

    private void persistChatListFrom(User user) {
        Log.i(LOG_TAG, "persistChatListFrom");
        int count = 0;
        if (user.getChats() == null) {
            return;
        }
        Chat[] chastList = user.getChats().values().toArray(new Chat[user.getChats().size()]);
        for (Chat chat :
                chastList) {
            ContentValues chatCV = new ContentValues();
            chatCV.put(ChatMembersColumns.FIREBASE_CHAT_ID, chat.getChatId());
            User[] chatMembersList = chat.getMembers().values().toArray(
                    new User[chat.getMembers().values().size()]
            );
            for (User other_user : chatMembersList) {
                if (!other_user.getId().equals(user.getId())) {
                    Log.i(LOG_TAG, "other user to be setted: \n" +
                            "id: " + other_user.getId() +
                            "name: " + other_user.getName());
                    chatCV.put(ChatMembersColumns.OTHER_MEMBER_ID, other_user.getId());
                    chatCV.put(ChatMembersColumns.OTHER_MEMBER_NAME, other_user.getName());
                    //TODO remove this part to a different one, where http requests are separated from database work
                    //better put inside the object in other code part
                    String profilePictureUrl = other_user.getProfilePicture();
                    String fluentLanguage = other_user.getFluentLanguage();
                    chatCV.put(ChatMembersColumns.OTHER_MEMBER_PHOTO_URL, profilePictureUrl);
                    chatCV.put(ChatMembersColumns.OTHER_MEMBER_FLUENT_LANGUAGE, fluentLanguage);

                }
            }

            try {

                Log.d(LOG_TAG, "trying to insert a row...");
                //noinspection UnusedAssignment
                Uri rowNumber = getContext().getContentResolver().insert(CHAT_URI, chatCV);
                Log.d(LOG_TAG, "inserted ok! Count: " + (count + 1));

            } catch (Exception e) {
                Log.e(LOG_TAG, "Insert not possible:" + e.getCause());
                Log.d(LOG_TAG, "Trying to update");
                Log.d(LOG_TAG, "Values: " + chat.getChatId() + " "
                        + chat.getMembers());
                int rows = getContext().getContentResolver().update(CHAT_URI, chatCV,
                        ChatMembersColumns.FIREBASE_CHAT_ID + " = ?", new String[]{chat.getChatId()});
                if (rows > 0) {
                    Log.i(LOG_TAG, "updated successfuly. Count: " + (count + 1));
                }
            }

        }


    }

    private String getProfilePictureForUserId(String id, String userToken) throws IOException, APIException {
        FirebaseClient client = ServiceGenerator.createService(FirebaseClient.class, new AccessToken(
                "Bearer",
                userToken)
        );

        if (mFirebaseAuth.getCurrentUser().getUid() == null) {
            Log.w(LOG_TAG, "method with null User variable!");
            return null;
        }
        Call<String> call = client.getUserPictureUrl(id, userToken);

        Log.i("SpekoSyncAdapter", "getUser: \n");
        Response<String> response = call.execute();
        if (response.isSuccessful()) {
            String userPictureUrl = response.body();
            if (userPictureUrl != null) {
                Log.i("SpekoSyncAdapter", "Deu certo!: \n" + user.toString());

                return userPictureUrl;
            }
        } else {
            Log.e(LOG_TAG, "Response not successfull");
            throw new APIException(
                    String.valueOf(
                            ErrorUtils.parseError(response).message())
            );
        }

        return null;

    }


    private void persistFriends(User[] userFriends) {

        // verification to solve a crash when getCurrent user returns null
        if (mFirebaseAuth.getCurrentUser() == null) {
            Log.w(LOG_TAG, "User Signed Out!");
            mFirebaseAuth.signOut();
            return;
        }
        User mainUser = Utility.getUser(getContext());
        ContentValues[] cvArray = new ContentValues[userFriends.length];

        for (int count = 0; count < userFriends.length; count++) {
            User userFriend = userFriends[count];
            ContentValues userCV = new ContentValues();
            Log.i(LOG_TAG, "Persist User Friends: Inserting user friend with id: " + userFriend.getId());
            userCV.put(UserColumns.FIREBASE_ID, userFriend.getId());
            userCV.put(UserColumns.NAME, userFriend.getName());
            userCV.put(UserColumns.AGE, userFriend.getAge());
            userCV.put(UserColumns.EMAIL, userFriend.getEmail());
            userCV.put(UserColumns.FLUENT_LANGUAGE, userFriend.getFluentLanguage());
            userCV.put(UserColumns.LEARNING_CODE, userFriend.getLearningCode());
            userCV.put(UserColumns.LEARNING_LANGUAGE, userFriend.getLearningLanguage());
            userCV.put(UserColumns.USER_PHOTO_URL, userFriend.getProfilePicture());
            Log.i(LOG_TAG, "Profile URL: " + userFriend.getProfilePicture());
            userCV.put(UserColumns.FRIEND_OF, mFirebaseAuth.getCurrentUser().getUid());
            cvArray[count] = userCV;


        }

        getContext().getContentResolver().delete(UsersProvider.Users.USER_URI,
                UserColumns.FRIEND_OF + " = ?"
                , new String[]{mainUser.getId()});

        getContext().getContentResolver().bulkInsert(UsersProvider.Users.USER_URI, cvArray);

    }

    private HashMap<String, User> getFriends(String idToken) throws IOException, APIException {

        // Fetch and print a list of the contributors to this library.
        FirebaseClient client = ServiceGenerator.createService(FirebaseClient.class, new AccessToken(
                "Bearer",
                idToken)
        );

        String parameterValue = user.getLearningLanguage() + "|" + user.getFluentLanguage();
        String parameterKey = "learningCode";
        Call<HashMap<String, User>> call = client.getUsersListWith(userToken
                , "\"" + parameterKey + "\""
                , "\"" + parameterValue + "\"");

//        try {
        Log.i("SpekoSyncAdapter", "getFriends: \n");

        Response<HashMap<String, User>> response = call.execute();

        if (response.isSuccessful()) {
            HashMap<String, User> friends = response.body();
            return friends;
        } else {
            Log.e(LOG_TAG, "Response not successfull");
            throw new APIException(
                    String.valueOf(
                            ErrorUtils.parseError(response).status())
            );
        }

    }

    private void persistUser(@NonNull User user) {
        if (user == null) {
            Log.w(LOG_TAG, "method with null User variable!");
            return;
        }
        ContentValues userCV = new ContentValues();
        userCV.put(FIREBASE_ID, user.getId());
        userCV.put(UserColumns.NAME, user.getName());
        userCV.put(UserColumns.EMAIL, user.getEmail());
        userCV.put(UserColumns.AGE, user.getAge());
        userCV.put(UserColumns.FLUENT_LANGUAGE, user.getFluentLanguage());
        userCV.put(UserColumns.LEARNING_LANGUAGE, user.getLearningLanguage());
        userCV.put(UserColumns.USER_DESCRIPTION, user.getUserDescription());
        userCV.put(UserColumns.LEARNING_CODE, user.getLearningCode());
        userCV.put(UserColumns.USER_PHOTO_URL, user.getProfilePicture());

        // deleting any row first
        getContext().getContentResolver().delete(USER_URI,
                FIREBASE_ID + " = ?",
                new String[]{user.getId()});

        // insterting
        getContext().getContentResolver().insert(USER_URI, userCV);
    }

    private User getUser(String idToken) throws IOException, APIException {
        // Fetch and print a list of the contributors to this library.
        FirebaseClient client = ServiceGenerator.createService(FirebaseClient.class,
                new AccessToken(
                        "Bearer",
                        idToken)
        );
        if (mFirebaseAuth.getCurrentUser().getUid() == null) {
            Log.w(LOG_TAG, "method with null User variable!");
            return null;
        }
        Call<User> call = client.getUser(mFirebaseAuth.getCurrentUser().getUid(), idToken);

//        try {
        Log.i("SpekoSyncAdapter", "getUser: \n");
        Response<User> response = call.execute();
        if (response.isSuccessful()) {
            user = response.body();
            if (user != null) {
                Log.i("SpekoSyncAdapter", "Deu certo!: \n" + user.toString());
            }
            return user;

        } else {
            //TODO Handle API error responses
            Log.e(LOG_TAG, "Response not successfull");
            throw new APIException(
                    String.valueOf(
                            ErrorUtils.parseError(response).message())
            );

        }


    }

    public static void initializeSyncAdapter(Context context) {

        Log.d("SpekoSyncAdapter", "initializeSyncAdapter");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        getSyncAccount(context);


    }

    /**
     * Create a new dummy account for the onClickSync adapter
     *
     * @param context The application context
     */
    public static Account getSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, CONTENT_AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);


        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */

            Log.w("HomeActivity", "Deu ruim com o Account: " + newAccount);
//            return null;
        }

        return newAccount;

    }

    private static void onAccountCreated(Account newAccount, Context context) {

        configurePeriodicSync(context, SYNC_INTERVAL, FLEX_TIME);
        // Inform the system that this account is eligible for auto onClickSync when the network is up
        ContentResolver.setSyncAutomatically(newAccount, AUTHORITY, true);

//        TODO Uncomment this:
//        syncImmediatly(context);
    }

    public static void syncImmediatly(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                AUTHORITY, bundle);
    }

    public static boolean isSyncActive(Context context) {

        Account account = SpekoSyncAdapter.getSyncAccount(context);
        String authority = UserContract.AUTHORITY;
        Log.i("isSyncActive", "start with values: \n " +
                "account: " + account.toString() + "\n" +
                "authority: " + authority.toString());
        for (SyncInfo syncInfo : ContentResolver.getCurrentSyncs()) {
            Log.i("isSyncActive", "syncInfo: \n" +
                    "account: " + syncInfo.account.toString() + "\n" +
                    "authority: " + syncInfo.authority.toString() + "\n");

            // just checked authority, as account seems to be cryptographed
            if (syncInfo.authority.toString().equals(authority.toString())) {
                return true;
            }
        }
        return false;

    }


    /**
     * Helper method to schedule the onClickSync adapter periodic execution
     */
    private static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (account != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // we can enable inexact timers in our periodic onClickSync
                SyncRequest request = new SyncRequest.Builder().
                        syncPeriodic(syncInterval, flexTime).
                        setSyncAdapter(account, authority).
                        setExtras(new Bundle()).build();
                Log.i(LOG_TAG, "request: " + request.toString());

                ContentResolver.requestSync(request);
            } else {
                ContentResolver.addPeriodicSync(account,
                        authority, new Bundle(), syncInterval);
            }
        } else {
            Log.i(LOG_TAG, "Account retornando null!");

        }

    }

    public static void setSyncStatus(Context c, @LocationStatus int syncStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.shared_preference_sync_status_key), syncStatus);
        spe.commit();
    }

}
