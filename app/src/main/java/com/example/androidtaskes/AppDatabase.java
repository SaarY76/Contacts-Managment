package com.example.androidtaskes;

import androidx.room.Database;
import androidx.room.RoomDatabase;

// this is the Database class that in it there is an implementation of the user Dao
// that in in there are Queries for Database operations
@Database(entities = {User.class, Contact.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase
{
    public abstract UserDao userDao();
}
