package com.speko.android.data;

/**
 * Created by rafaelalves on 20/04/17.
 */

@SuppressWarnings("ALL")
public class UserPublic {
    String id;
    String name;
    String fluentLanguage;
    String profilePicture;


    UserPublic(){

    }

    public UserPublic(String id){
        this.id = id;
    }

    UserPublic(String name, String id){
        this.name = name;
        this.id = id;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFluentLanguage() {
        return fluentLanguage;
    }

    public void setFluentLanguage(String fluentLanguage) {
        this.fluentLanguage = fluentLanguage;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
