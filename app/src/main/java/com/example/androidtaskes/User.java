package com.example.androidtaskes;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User
{
    @PrimaryKey
    @NonNull
    private String userName;// the user's username
    private String password;// the user's password

    public User() {}

    public User(String userName, String password)
    {
        this.userName = userName;
        this.password = password;
    }

    // Getters and Setters for userName
    @NonNull
    public String getUserName() {return userName;}

    public void setUserName(@NonNull String userName) {
        this.userName = userName;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
