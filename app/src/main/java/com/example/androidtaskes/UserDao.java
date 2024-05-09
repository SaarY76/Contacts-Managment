package com.example.androidtaskes;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

//
@Dao
public interface UserDao
{
    // a Query for inserting a user into the Database
    @Insert
    long insertUser(User user);

    // a Query for inserting a contact into the Database
    @Insert
    long insertContact(Contact contact);

    // a Query for getting the count of the users with the username String parameter
    @Query("SELECT COUNT(*) FROM users WHERE userName = :userName")
    int getUserCountByUserName(String userName);

    // a Query for getting the specific user with the username and password parameters
    @Query("SELECT * FROM users WHERE userName = :userName AND password = :password")
    User getUserByUserNameAndPassword(String userName, String password);

    // a Query for getting all of the contacts that related to the current user
    @Query("SELECT * FROM contacts WHERE userName = :userName")
    LiveData<List<Contact>> getContactsForUser(String userName);

    // a Query for getting the count of the contacts with the phone number parameter
    @Query("SELECT COUNT(*) FROM contacts WHERE userName = :userName AND phone = :phone")
    int countContactsByPhoneForUser(String userName, String phone);

    // a Query for deleting a contact, and doing that by the username related to this conatct
    // and the contact's phone number parameters
    @Query("DELETE FROM contacts WHERE userName = :userName AND phone = :phoneNumber")
    void deleteContactByPhoneNumber(String userName, String phoneNumber);

    // a Query for updating a contact by calling the delete contact and then calling the insert contact
    @Transaction
    default void updateContact(String userName, String phoneNumber, Contact newContact)
    {
        deleteContactByPhoneNumber(userName, phoneNumber);
        insertContact(newContact);
    }

}

