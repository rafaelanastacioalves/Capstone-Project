package com.speko.android.retrofit;

import com.speko.android.data.Chat;
import com.speko.android.data.Friend;
import com.speko.android.data.User;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by rafaelanastacioalves on 12/28/16.
 */

public interface FirebaseClient {
    @GET("/repos/{owner}/{repo}/contributors")
    Call<List<Friend>> contributors(
            @Path("owner") String owner,
            @Path("repo") String repo
    );

    @GET("/users/{userId}.json")
    Call<User> getUser(
            @Path("userId") String owner );

    @GET("/users/{userId}.json")
    Call<User> getUser(
            @Path("userId") String owner,
            @Query("auth") String token);

    @GET("/friends/{userId}.json")
    Call<HashMap<String,User>> getUserFriends(
            @Path("userId") String owner,
            @Query("auth") String token);

    @GET("/users/.json?")
    Call<HashMap<String,User>> getUsersListWith(
            @Query("auth") String token,
            @Query("orderBy") String orderByKey,
            @Query("equalTo") String equalToKey);
    @GET("chats/.json")
    Call<HashMap<String,Chat>> getChatsListWith(
            @Query("auth") String token,
            @Query("orderBy") String orderByKey,
            @Query("equalTo") Boolean equalToKey);
}
