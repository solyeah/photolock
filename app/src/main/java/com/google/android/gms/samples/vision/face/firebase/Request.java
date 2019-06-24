package com.google.android.gms.samples.vision.face.firebase;

public class Request {
    private int type;
    private String userName;
    private String imageName;

    public Request(){
        type = 0;
        userName = "test";
        imageName = "test.png";
    }

    public Request(int type, String userName, String imageName){
        this.type = type;
        this.userName = userName;
        this.imageName = imageName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
