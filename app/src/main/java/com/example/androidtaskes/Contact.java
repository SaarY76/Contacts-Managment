package com.example.androidtaskes;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Calendar;

// With primaryKeys = {"userName", "phone"}, we ensure that within the contacts table,
// the combination of userName and phone must be unique, the each user can have a contact with
// the same phone number, but in the same contacts list of a specific user there will be no
// contacts that will have the same phone number (primary key) as well as the username
@Entity(tableName = "contacts",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "userName",
                childColumns = "userName",
                onDelete = ForeignKey.CASCADE),
        primaryKeys = {"userName", "phone"})
public class Contact
{

    @NonNull
    @ColumnInfo(name = "userName")
    private String userName; // This is a foreign key referencing to a User

    @NonNull
    private String phone;// the contact's phone number, this is another primary key

    private String name;// the contact's name
    private String email;// the contact's email
    private String gender;// the contact's gender
    private String birthdate;// the contact's birthdate

    public Contact () {}

    public Contact(@NonNull String userName, String phone, String name, String email, String gender, String birthdate)
    {
        this.userName = userName;
        this.phone = phone;
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.birthdate = birthdate; // You might want to convert from Calendar to String here
    }

    // Setters and Getters
    @NonNull
    public String getUserName() {
        return userName;
    }

    public void setUserName(@NonNull String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }
}
