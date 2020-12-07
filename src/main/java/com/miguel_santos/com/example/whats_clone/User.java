package com.miguel_santos.com.example.whats_clone;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String userID;
    private String username;
    private String profileUrl;

    public User() { }

    public User(String userID, String username, String profileUrl) {
        this.userID = userID;
        this.username = username;
        this.profileUrl = profileUrl;
    }

    protected User(Parcel in) {
        userID = in.readString();
        username = in.readString();
        profileUrl = in.readString();
    }


    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(userID);
        dest.writeString(username);
        dest.writeString(profileUrl);
    }


}
