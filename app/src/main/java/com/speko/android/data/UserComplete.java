package com.speko.android.data;

import com.google.firebase.database.Exclude;

import java.util.HashMap;

/**
 * Created by rafaelanastacioalves on 12/26/16.
 */

public class UserComplete extends UserPublic {




    private String learningLanguage;
    private String learningCode;
    private String age;
    private String email;
    private String userDescription;

    @Exclude
    private HashMap<String,Chat> chats;

    public UserComplete(){
    }

    public UserComplete(String name, String id){
        super(name, id);

    }

    public UserComplete(String name, String age, String email, String fluentLanguage){
        this.name = name;
        this.age = age;
        this.email = email;
        this.fluentLanguage = fluentLanguage;
    }
    public UserComplete(String name){
        super(name);
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    // TODO this still works if I change from email to email?
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }





    public String getLearningLanguage() {
        return learningLanguage;
    }

    public void setLearningLanguage(String learningLanguage) {
        this.learningLanguage = learningLanguage;
    }

    public String getLearningCode() {
        return learningCode;
    }

    public void setLearningCode(String learningCode) {
        this.learningCode = learningCode;
    }

    public HashMap<String, Chat> getChats() {
        return chats;
    }

    public void setChats(HashMap<String, Chat> chats) {
        this.chats = chats;
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

    public String getUserDescription() {
        return userDescription;
    }

    public void setUserDescription(String userDescription) {
        this.userDescription = userDescription;
    }


}
