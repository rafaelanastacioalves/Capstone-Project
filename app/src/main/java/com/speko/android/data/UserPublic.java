package com.speko.android.data;

/**
 * Created by rafaelalves on 20/04/17.
 */

public class UserPublic {
    protected String id;
    protected String name;
    protected String fluentLanguage;
    protected String profilePicture;


    public UserPublic(){

    }

    public UserPublic(String name){
        this.name = name;
    }

    public UserPublic(String name, String id){
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
