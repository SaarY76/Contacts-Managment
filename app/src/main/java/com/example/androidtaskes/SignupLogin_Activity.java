package com.example.androidtaskes;

import androidx.room.Room;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupLogin_Activity extends NoActionBarAndNotTurnAroundClass
{
    private static AppDatabase db;// a variable that we can use from all of the Activities of the Database instance
    public static AppDatabase getDb()
    {
        return db;
    }// getter for the database instance

    private EditText editTextUserName;// the EditText of the userName input
    private EditText editTextPassword;// the EditText of the password input
    private TextView textViewErrorMessageFromDB;// the TextView that will show an error from the Database
    private final int MIN_USERNAME_LENGTH = 6;// a variable that indicates the minimum username length

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_login);

        createDatabaseInstance(this);

        editTextUserName = findViewById(R.id.editTextUserName);
        editTextPassword = findViewById(R.id.editTextPassword);
        textViewErrorMessageFromDB = findViewById(R.id.textViewErrorMessage);
        setOnClickListenerToEyeInEditText();
    }

    /**
     * the function is a static function that from it we create an instance of the Database
     * @param context - the Activity's context
     */
    public static void createDatabaseInstance (Context context)
    {
        SignupLogin_Activity.db = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class, "database-name")
                        .build();
    }

    /**
     * the function is a on click function of a Button, that when clicking on it
     * if all of the inputs are valid and there is no user with the same username, the user will be created
     * @param view - a Button
     */
    public void createUser(View view)
    {
        Object[] errorsResults = there_are_errors();
        boolean isError = (boolean) errorsResults[0];
        if (!isError) // if there are no errors from the create user inputs
        {
            AppDatabase db = SignupLogin_Activity.getDb();

            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    UserDao userDao = db.userDao();

                    // Get the username from the EditText
                    String userName = editTextUserName.getText().toString();

                    // Check if a user with the specified userName already exists
                    int userCount = userDao.getUserCountByUserName(userName);
                    if (userCount > 0)
                    {
                        // User with provided username already exists
                        runOnUiThread(() -> textViewErrorMessageFromDB.setText("User already exists!\nChange to a different username"));
                    }
                    else
                    {
                        // No user with provided username, proceed with user creation
                        // Creating a new User object
                        User newUser = new User(userName, editTextPassword.getText().toString());

                        // Insert the new User into the database
                        userDao.insertUser(newUser);

                        runOnUiThread(() ->
                        {
                            textViewErrorMessageFromDB.setText("");
                            Toast.makeText(SignupLogin_Activity.this, "User created successfully!", Toast.LENGTH_LONG).show();
                        });
                    }
                }
            }).start();
        }
    }

    /**
     * the function is a on click function of a Button, that when clicking on it
     * if all of the inputs are valid from the Database the user will be signed in and will be moved
     * to the MainActivity
     * @param view - a Button
     */
    public void signIn(View view)
    {
        Object[] errorsResults = there_are_errors();
        boolean isError = (boolean) errorsResults[0];
        if (!isError) // if there are no errors from the sign in inputs
        {
            AppDatabase db = SignupLogin_Activity.getDb();

            new Thread(() ->
            {
                UserDao userDao = db.userDao();

                // Get the username and password from the EditTexts
                String userName = editTextUserName.getText().toString();
                String userPassword = editTextPassword.getText().toString();

                // Retrieve the user with the given username and password
                User retrievedUser = userDao.getUserByUserNameAndPassword(userName, userPassword);

                if (retrievedUser != null)
                {
                    // User with provided username and password found
                    runOnUiThread(() -> textViewErrorMessageFromDB.setText(""));

                    // Convert User to JSON string using Gson
                    Gson gson = new Gson();
                    String userJson = gson.toJson(retrievedUser);

                    // Create an Intent to start MainActivity
                    Intent intent = new Intent(SignupLogin_Activity.this, MainActivity.class);
                    // Pass the User JSON string to MainActivity
                    intent.putExtra("userJson", userJson);
                    runOnUiThread(() -> {
                        editTextUserName.setText("");
                        editTextPassword.setText("");
                    });

                    startActivity(intent);
                }
                else
                {
                    // No user found with the provided username and password
                    runOnUiThread(() -> textViewErrorMessageFromDB.setText("Invalid username or password!"));
                }
            }).start();
        }
    }

    /**
     * the function gets an Object array with boolean value and two String values
     * and if the boolean value is true the function puts in TextViews the error messages
     * and else the function puts an empty "" in the TextViews of the error messages
     * @param errors_result - an Object [] of the result of the inputs from the user that in it :
     * [0] - boolean value of - true if there are errors, and false there are no errors
     * [1] - String of the phone number error
     * [2] - String of the password error
     */
    private void settingErrorMessages (Object [] errors_result)
    {
        TextView phoneNumberError = findViewById(R.id.errorPhoneNumber);
        TextView passwordError = findViewById(R.id.errorPassword);
        if ((boolean)errors_result[0])
        {
            phoneNumberError.setText((String)errors_result[1]);
            passwordError.setText((String)errors_result[2]);
        }
        else
        {
            phoneNumberError.setText("");
            passwordError.setText("");
        }
    }

    /**
     * the function checks the inputs of the username and the password of the user and if there
     * are errors it puts the Strings that represent the errors in Object [] and boolean true value
     * and if there are no errors, it will put boolean false value and empty Strings
     * @return - an Object [] of the result of the inputs from the user that in it :
     * [0] - boolean value of - true if there are errors, and false there are no errors
     * [1] - String of the phone number error
     * [2] - String of the password error
     */
    private Object[] there_are_errors ()
    {
        Object [] fullResult = {false, "", ""};
        String userName = editTextUserName.getText().toString();
        String password = editTextPassword.getText().toString();
        String capitalLetterRegex = "^(?=.*[A-Za-z])(?=.*[A-Z]).*$";
        String lengthRegex = "^.{6,}$";
        Pattern capitalLetterPattern = Pattern.compile(capitalLetterRegex);
        Pattern lengthPattern = Pattern.compile(lengthRegex);
        Matcher capitalLetterMatcher = capitalLetterPattern.matcher(password);
        Matcher lengthMatcher = lengthPattern.matcher(password);
        if (userName.equals(""))
        {
            fullResult[0] = true;
            fullResult[1] = "There is no username entered";
        }
        else if (userName.length() < MIN_USERNAME_LENGTH)
        {
            fullResult[0] = true;
            fullResult[1] = "The username's length is less then " + MIN_USERNAME_LENGTH +" characters";
        }

        if (password.equals(""))
        {
            fullResult[0] = true;
            fullResult[2] = "There is no password entered";
        }
        else
        {
            if (!lengthMatcher.matches())
            {
                fullResult[0] = true;
                fullResult[2] = "The password length needs to be at least 6 characters";
            }
            if (!capitalLetterMatcher.matches())
            {
                fullResult[0] = true;
                String result = "The password doesn't contain a capital letter";
                if (!fullResult[2].equals(""))
                    fullResult[2] += "\n"+result;
                else
                    fullResult[2] += result;
            }
        }
        settingErrorMessages(fullResult);
        return fullResult;
    }

    /**
     * the function is the onClickListener of an eye that is a drawableEnd view that is in the
     * end of the password EditText that in this function it changes the eye based on the user's click.
     * if the eye is regular, the password is unseen, and if there is a line on the eye the password is seen
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setOnClickListenerToEyeInEditText ()
    {
        editTextPassword.setOnTouchListener((v, event) ->
        {
            Drawable[] compoundDrawables = editTextPassword.getCompoundDrawables();
            Drawable visibilityDrawable = compoundDrawables[2];

            if (compoundDrawables.length >= 3 && visibilityDrawable != null) {
                int drawableWidth = visibilityDrawable.getIntrinsicWidth();
                int touchAreaRight = editTextPassword.getRight() - editTextPassword.getPaddingRight();
                int touchAreaLeft = touchAreaRight - drawableWidth - 32; // Adjust the value as per your desired clickable area size

                if (event.getAction() == MotionEvent.ACTION_UP && event.getRawX() >= touchAreaLeft && event.getRawX() <= touchAreaRight) {
                    // Toggle password visibility
                    if (editTextPassword.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        editTextPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_24, 0);
                    } else {
                        editTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        editTextPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_off_24, 0);
                    }

                    // Move the cursor to the end of the text
                    editTextPassword.setSelection(editTextPassword.getText().length());

                    return true; // Consume the touch event
                }
            }

            return false;
        });
    }

    /**
     * the function is the Override of the onBackPressed of this Activity,
     * and this is for when the user will press back from this Activity, he/she will go
     * only out of the app
     */
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}