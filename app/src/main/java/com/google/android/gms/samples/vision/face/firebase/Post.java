package com.google.android.gms.samples.vision.face.firebase;

public class Post {

    private String permission;
    private String userName;

    public Post() {}

    public Post(String permission, String userName) {
        this.permission = permission;
        this.userName = userName;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}