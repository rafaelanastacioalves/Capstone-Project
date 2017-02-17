package com.speko.android.data;

import java.util.HashMap;

/**
 * Created by rafaelanastacioalves on 12/26/16.
 */

public class User {

    private String id;
    private String name;
    private String age;
    private String email;
    private String fluentLanguage;
    private String learningLanguage;
    private String learningCode;
    private HashMap<String,Chat> chats;

    public User(){
    }

    public User(String name, String id){

        this.name = name;
        this.id = id;
    }

    public User(String name, String age, String email, String fluentLanguage){
        this.name = name;
        this.age = age;
        this.email = email;
        this.fluentLanguage = fluentLanguage;
    }
    public User(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getFluentLanguage() {
        return fluentLanguage;
    }

    public void setFluentLanguage(String fluentLanguage) {
        this.fluentLanguage = fluentLanguage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
