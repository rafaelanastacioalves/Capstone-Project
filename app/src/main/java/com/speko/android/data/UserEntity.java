package com.speko.android.data;

/**
 * Created by rafaelanastacioalves on 12/26/16.
 */

@SuppressWarnings("ALL")
class UserEntity {
    private String name;
    public UserEntity(){

    }
    public UserEntity(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
