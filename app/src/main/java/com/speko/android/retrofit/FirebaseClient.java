package com.speko.android.retrofit;

import com.speko.android.data.Chat;
import com.speko.android.data.UserComplete;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by rafaelanastacioalves on 12/28/16.
 */

public interface FirebaseClient {


    @GET("/users/{userId}.json")
    Call<UserComplete> getUser(
            @Path("userId") String owner );

    @GET("/users/{userId}.json")
    Call<UserComplete> getUser(
            @Path("userId") String owner,
            @Query("auth") String token);



    @GET("/friends/{userId}.json")
    Call<HashMap<String,UserComplete>> getUserFriends(
            @Path("userId") String owner,
            @Query("auth") String token);

    @GET("/users/.json?")
    Call<HashMap<String,UserComplete>> getUsersListWith(
            @Query("auth") String token,
            @Query("orderBy") String orderByKey,
            @Query("equalTo") String equalToKey);
    @GET("chats/.json")
    Call<HashMap<String,Chat>> getChatsListWith(
            @Query("auth") String token,
            @Query("orderBy") String orderByKey,
            @Query("equalTo") Boolean equalToKey);

    @GET("/users/{userId}/profilePicture.json")
    Call<String> getUserPictureUrl(
            @Path("userId") String userId,
            @Query("auth") String auth
    );

    @GET("/users/{userId}/fluentLanguage.json")
    Call<String> getUserFluentLanguage(
            @Path("userId") String userId,
            @Query("auth") String auth
    );

}
