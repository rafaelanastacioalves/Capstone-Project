package com.speko.android.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
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
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.speko.android.R;
import com.speko.android.Utility;
import com.speko.android.data.Chat;
import com.speko.android.data.ChatMembersColumns;
import com.speko.android.data.UserColumns;
import com.speko.android.data.UserComplete;
import com.speko.android.data.UserContract;
import com.speko.android.data.UserPublic;
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
@SuppressWarnings("ALL")
public class SpekoSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final int SYNC_INTERVAL = 1000; //every minute
    private static final int FLEX_TIME = 1000; // every minute
    private static final String LOG = "SpekoSyncAdapter";
    private static FirebaseAuth mFirebaseAuth;
    private static final String LOG_TAG = "SpekoSyncAdapter";
    private static String userToken;
    private static UserComplete userComplete;


    public static final String ACTION_DATA_UPDATED =
            "com.speko.android.ACTION_DATA_UPDATED";


    @SuppressWarnings("SameParameterValue")
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

    private static final int SYNC_STATUS_OK = 0;
    public static final int SYNC_STATUS_SERVER_DOWN = 1;
    public static final int SYNC_STATUS_SERVER_ERROR = 2;
    public static final int SYNC_STATUS_UNKNOWN = 3;
    private static final int SYNC_STATUS_INVALID = 4;

    public static void setUserToken(String userToken) {
        SpekoSyncAdapter.userToken = userToken;
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.i(LOG_TAG, "onPerformSync");


        if (userToken != null) {
            UserComplete userComplete = null;
            try {

                Log.i("SpekoSyncAdapter", "getUserComplete... ");
                userComplete = getUserComplete(userToken);
                //in case of user null - logout - just stop
                if (userComplete == null) {
                    return;
                }

                Log.i("SpekoSyncAdapter", "getOtherUSerPhotofrom... ");
                getOtherUsersPhotofrom(userComplete, userToken);

                HashMap<String, UserComplete> userFriends = null;
                Log.i("SpekoSyncAdapter", "getFriends... ");
                userFriends = getFriends(userToken);

                if (userFriends != null) {
                    // persisting everything in database
                    persistUser(userComplete);
                    persistChatListFrom(userComplete);
                    persistFriends(userFriends.values().toArray(new UserComplete[userFriends.size()]));
                    Log.i("SpekoSyncAdapter", "Deu certo!: \n" + userComplete.toString());

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
        synchronized (getContext().getContentResolver()){
            getContext().getContentResolver().notify();
        }


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
    private void getOtherUsersPhotofrom(UserComplete userComplete, String userToken) throws IOException, APIException {
        HashMap<String, Chat> chats = userComplete.getChats();
        UserPublic userPublicTemp;
        if (chats != null) {
            Log.i(LOG_TAG, "chats: " + chats);
            for (String chatKey : chats.keySet()) {
                HashMap<String, UserPublic> membersHashMap = chats.get(chatKey).getMembers();
                for (String otherUserId : membersHashMap.keySet()) {
                    if (!otherUserId.equals(userComplete.getId())) {
                        Log.i(LOG_TAG, "otherMemberId: " + otherUserId);
                        userPublicTemp = getUserPublicWithId(otherUserId,userToken);
                        membersHashMap.put(otherUserId, userPublicTemp);
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

        //noinspection ConstantConditions,ConstantConditions
        if (mFirebaseAuth.getCurrentUser().getUid() == null) {
            Log.w(LOG_TAG, "method with null User variable!");
            return null;
        }
        Call<String> call = client.getUserFluentLanguage(otherUserId, userToken);

        Log.i("SpekoSyncAdapter", "getUserComplete: \n");
        Response<String> response = call.execute();
        if (response.isSuccessful()) {
            String userPictureUrl = response.body();
            if (userPictureUrl != null) {
                Log.i("SpekoSyncAdapter", "Deu certo!: \n" + userComplete.toString());

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

    private void persistChatListFrom(UserComplete userComplete) {
        Log.i(LOG_TAG, "persistChatListFrom");
        int count = 0;
        if (userComplete.getChats() == null) {
            return;
        }
        Chat[] chastList = userComplete.getChats().values().toArray(new Chat[userComplete.getChats().size()]);
        for (Chat chat :
                chastList) {
            ContentValues chatCV = new ContentValues();
            chatCV.put(ChatMembersColumns.FIREBASE_CHAT_ID, chat.getChatId());
            UserPublic[] chatMembersList = chat.getMembers().values().toArray(
                    new UserPublic[chat.getMembers().values().size()]
            );
            for (UserPublic other_userComplete : chatMembersList) {
                if (!other_userComplete.getId().equals(userComplete.getId())) {
                    Log.i(LOG_TAG, "other user to be setted: \n" +
                            "id: " + other_userComplete.getId() +
                            "name: " + other_userComplete.getName());
                    chatCV.put(ChatMembersColumns.OTHER_MEMBER_ID, other_userComplete.getId());
                    chatCV.put(ChatMembersColumns.OTHER_MEMBER_NAME, other_userComplete.getName());
                    //TODO remove this part to a different one, where http requests are separated from database work
                    //better put inside the object in other code part
                    String profilePictureUrl = other_userComplete.getProfilePicture();
                    String fluentLanguage = other_userComplete.getFluentLanguage();
                    chatCV.put(ChatMembersColumns.OTHER_MEMBER_PHOTO_URL, profilePictureUrl);
                    chatCV.put(ChatMembersColumns.OTHER_MEMBER_FLUENT_LANGUAGE, fluentLanguage);

                }
            }

            try {

                Log.d(LOG_TAG, "trying to insert a row...");
                //noinspection UnusedAssignment
                int rowNumber = getContext().getContentResolver().delete(CHAT_URI, null,null);
                if (rowNumber > 0) {
                    Log.i(LOG_TAG, "deleted successfuly. Count: " + (count + 1));
                }
                getContext().getContentResolver().insert(CHAT_URI, chatCV);
                Log.d(LOG_TAG, "inserted ok! Count: " + (count + 1));

            } catch (Exception e) {
                Log.e(LOG_TAG, "Insert or deleting not possible:" + e.getCause());

            }

        }


    }

    private String getProfilePictureForUserId(String id, String userToken) throws IOException, APIException {
        FirebaseClient client = ServiceGenerator.createService(FirebaseClient.class, new AccessToken(
                "Bearer",
                userToken)
        );

        //noinspection ConstantConditions,ConstantConditions
        if (mFirebaseAuth.getCurrentUser().getUid() == null) {
            Log.w(LOG_TAG, "method with null User variable!");
            return null;
        }
        Call<String> call = client.getUserPictureUrl(id, userToken);

        Log.i("SpekoSyncAdapter", "getUserComplete: \n");
        Response<String> response = call.execute();
        if (response.isSuccessful()) {
            String userPictureUrl = response.body();
            if (userPictureUrl != null) {
                Log.i("SpekoSyncAdapter", "Deu certo!: \n" + userComplete.toString());

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


    private void persistFriends(UserComplete[] userCompleteFriends) {

        // verification to solve a crash when getCurrent user returns null
        if (mFirebaseAuth.getCurrentUser() == null) {
            Log.w(LOG_TAG, "User Signed Out!");
            mFirebaseAuth.signOut();
            return;
        }
        UserComplete mainUserComplete = Utility.getUser(getContext());
        ContentValues[] cvArray = new ContentValues[userCompleteFriends.length];

        for (int count = 0; count < userCompleteFriends.length; count++) {
            UserComplete userCompleteFriend = userCompleteFriends[count];
            ContentValues userCV = new ContentValues();
            Log.i(LOG_TAG, "Persist User Friends: Inserting user friend with id: " + userCompleteFriend.getId());
            userCV.put(UserColumns.FIREBASE_ID, userCompleteFriend.getId());
            userCV.put(UserColumns.NAME, userCompleteFriend.getName());
            userCV.put(UserColumns.AGE, userCompleteFriend.getAge());
            userCV.put(UserColumns.EMAIL, userCompleteFriend.getEmail());
            userCV.put(UserColumns.FLUENT_LANGUAGE, userCompleteFriend.getFluentLanguage());
            userCV.put(UserColumns.LEARNING_CODE, userCompleteFriend.getLearningCode());
            userCV.put(UserColumns.LEARNING_LANGUAGE, userCompleteFriend.getLearningLanguage());
            userCV.put(UserColumns.USER_PHOTO_URL, userCompleteFriend.getProfilePicture());
            Log.i(LOG_TAG, "Profile URL: " + userCompleteFriend.getProfilePicture());
            userCV.put(UserColumns.FRIEND_OF, mFirebaseAuth.getCurrentUser().getUid());
            cvArray[count] = userCV;


        }

        //noinspection ConstantConditions
        getContext().getContentResolver().delete(UsersProvider.Users.USER_URI,
                UserColumns.FRIEND_OF + " = ?"
                , new String[]{mainUserComplete.getId()});

        getContext().getContentResolver().bulkInsert(UsersProvider.Users.USER_URI, cvArray);

    }

    private HashMap<String, UserComplete> getFriends(String idToken) throws IOException, APIException {

        // Fetch and print a list of the contributors to this library.
        FirebaseClient client = ServiceGenerator.createService(FirebaseClient.class, new AccessToken(
                "Bearer",
                idToken)
        );

        String parameterValue = userComplete.getLearningLanguage() + "|" + userComplete.getFluentLanguage();
        String parameterKey = "learningCode";
        Call<HashMap<String, UserComplete>> call = client.getUsersListWith(userToken
                , "\"" + parameterKey + "\""
                , "\"" + parameterValue + "\"");

//        try {
        Log.i("SpekoSyncAdapter", "getFriends: \n");

        Response<HashMap<String, UserComplete>> response = call.execute();

        if (response.isSuccessful()) {
            return response.body();
        } else {
            Log.e(LOG_TAG, "Response not successfull");
            throw new APIException(
                    String.valueOf(
                            ErrorUtils.parseError(response).status())
            );
        }

    }

    /**
     * Persists user. For internal usage.
     * @param userComplete The user object to be persisted. It is supposed to be the main user of
     *                     the app
     */

    private void persistUser(@NonNull UserComplete userComplete) {
        //noinspection ConstantConditions
        if (userComplete == null) {
            Log.w(LOG_TAG, "method with null User variable!");
            return;
        }
        ContentValues userCV = mountContentValuesFromUser(userComplete);

        // deleting any row first
        getContext().getContentResolver().delete(USER_URI,
                FIREBASE_ID + " = ?",
                new String[]{userComplete.getId()});

        // insterting
        getContext().getContentResolver().insert(USER_URI, userCV);
    }

    public static void persistUser(@NonNull UserComplete userComplete, Context context){
        //noinspection ConstantConditions
        if (userComplete == null) {
            Log.w(LOG_TAG, "method with null User variable!");
            return;
        }
        ContentValues userCV = mountContentValuesFromUser(userComplete);
        // deleting any row first
        context.getContentResolver().delete(USER_URI,
                FIREBASE_ID + " = ?",
                new String[]{userComplete.getId()});

        // insterting
        context.getContentResolver().insert(USER_URI, userCV);

    }


    @NonNull
    private static ContentValues mountContentValuesFromUser(@NonNull UserComplete userComplete) {
        ContentValues userCV = new ContentValues();
        userCV.put(FIREBASE_ID, userComplete.getId());
        userCV.put(UserColumns.NAME, userComplete.getName());
        userCV.put(UserColumns.EMAIL, userComplete.getEmail());
        userCV.put(UserColumns.AGE, userComplete.getAge());
        userCV.put(UserColumns.FLUENT_LANGUAGE, userComplete.getFluentLanguage());
        userCV.put(UserColumns.LEARNING_LANGUAGE, userComplete.getLearningLanguage());
        userCV.put(UserColumns.USER_DESCRIPTION, userComplete.getUserDescription());
        userCV.put(UserColumns.LEARNING_CODE, userComplete.getLearningCode());
        userCV.put(UserColumns.USER_PHOTO_URL, userComplete.getProfilePicture());
        return userCV;
    }



    private UserComplete getUserComplete(String idToken) throws IOException, APIException {
        // Fetch and print a list of the contributors to this library.
        FirebaseClient client = ServiceGenerator.createService(FirebaseClient.class,
                new AccessToken(
                        "Bearer",
                        idToken)
        );
        //noinspection ConstantConditions,ConstantConditions
        if (mFirebaseAuth.getCurrentUser().getUid() == null) {
            Log.w(LOG_TAG, "method with null User variable!");
            return null;
        }
        Call<UserComplete> call = client.getUser(mFirebaseAuth.getCurrentUser().getUid(), idToken);

//        try {
        Log.i("SpekoSyncAdapter", "getUserComplete: \n");
        Response<UserComplete> response = call.execute();
        if (response.isSuccessful()) {
            userComplete = response.body();
            if (userComplete != null) {
                Log.i("SpekoSyncAdapter", "Deu certo!: \n" + userComplete.toString());
            }
            return userComplete;

        } else {
            //TODO Handle API error responses
            Log.e(LOG_TAG, "Response not successfull");
            throw new APIException(
                    String.valueOf(
                            ErrorUtils.parseError(response).message())
            );

        }


    }

    private UserPublic getUserPublicWithId(String userId, String idToken) throws IOException, APIException {
        UserPublic userPublic;
        // Fetch and print a list of the contributors to this library.
        FirebaseClient client = ServiceGenerator.createService(FirebaseClient.class,
                new AccessToken(
                        "Bearer",
                        idToken)
        );
        //noinspection ConstantConditions,ConstantConditions
        if (mFirebaseAuth.getCurrentUser().getUid() == null) {
            Log.w(LOG_TAG, "method with null User variable!");
            return null;
        }
        Call<UserComplete> call = client.getUser(userId, idToken);

//        try {
        Log.i("SpekoSyncAdapter", "getUserPublicWithId: \n");
        Response<UserComplete> response = call.execute();
        if (response.isSuccessful()) {
            userPublic = response.body();
            if (userPublic != null) {
                Log.i("SpekoSyncAdapter", "Deu certo!: \n" + userComplete.toString());
            }
            return userPublic;

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
        mFirebaseAuth = FirebaseAuth.getInstance();
        getSyncAccount(context);


    }

    /**
     * Create a new dummy account for the onClickSync adapter
     *
     * @param context The application context
     */
    private static Account getSyncAccount(Context context) {
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
                "authority: " + authority);

        for (SyncInfo syncInfo : ContentResolver.getCurrentSyncs()) {
            Log.i("isSyncActive", "syncInfo: \n" +
                    "account type: " + syncInfo.account + "\n" +
                    "authority: " + syncInfo.authority + "\n");

            // just checked authority, as account seems to be cryptographed
            if (syncInfo.authority.equals(authority) ) {
                return true;
            }
        }
        return false;

    }


    /**
     * Helper method to schedule the onClickSync adapter periodic execution
     */
    @SuppressWarnings("SameParameterValue")
    @SuppressLint("ObsoleteSdkInt")
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

    private static void setSyncStatus(Context c, @LocationStatus int syncStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.shared_preference_sync_status_key), syncStatus);
        spe.commit();
    }

}
