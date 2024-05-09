package com.example.androidtaskes;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class NoActionBarAndNotTurnAroundClass extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setting the Activity screen to not turn to wide screen
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // setting the Activity to not have an action bar
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().hide();
        }
    }
}
