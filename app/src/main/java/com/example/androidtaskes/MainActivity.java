package com.example.androidtaskes;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends NoActionBarAndNotTurnAroundClass {
    private User user;// the current user that has signed in that we collect from the intent
    private ListView contactsListView;// the ListView that will be filled with Contacts
    private TextView textViewNoContacts;// the TextView that will show to the user that there are no Contacts
    private TextView textViewUserNameTitle;// the TextView that will show the user's username
    private String chosenBirthDate = null;// the chosen birthdate from the create or update contact
    private final int MIN_PHONE_NUMBER_LENGTH = 10;// the minimum phone number's length

    private AppDatabase db;// a variable that is the instance of the Database
    private UserDao userDao;// a variable that is the instance of the interface that have function of Database Queries

    private String gender;// contact gender class variable that we will put from the API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewNoContacts = findViewById(R.id.textViewNoContacts);
        //thisLinearLayout = findViewById(R.id.thisLinearLayout);
        contactsListView = findViewById(R.id.contactsListView);
        textViewUserNameTitle = findViewById(R.id.mainActivity_userName_Title);

        // initializing the Database objects
        db = SignupLogin_Activity.getDb();
        userDao = db.userDao();

        // Get the Intent that started this activity
        Intent intent = getIntent();
        String userJson = intent.getStringExtra("userJson");

        // Check if the userJson is not null
        if (userJson != null && !userJson.isEmpty()) {
            // Convert the user JSON string back to User object using Gson
            Gson gson = new Gson();
            user = gson.fromJson(userJson, User.class);
            // setting the title with the user's username
            textViewUserNameTitle.setText(textViewUserNameTitle.getText().toString() + " " + user.getUserName());
            userDao.getContactsForUser(user.getUserName()).observe(this, contacts ->
            {
                if (contacts != null && !contacts.isEmpty())// if there are Contacts in the Database related to this user
                {
                    textViewNoContacts.setVisibility(View.GONE);
                    contactsListView.setVisibility(View.VISIBLE);
                    populateContactsInListView(contacts);
                } else // there are no Contacts related to this user
                {
                    contactsListView.setVisibility(View.GONE);
                    textViewNoContacts.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    /**
     * the function is populating the contacts ListView by getting a List of Contacts
     * and connect them with an ArrayAdapter
     *
     * @param contacts- a List of Contacts
     */
    private void populateContactsInListView(List<Contact> contacts) {
        if (contactsListView.getAdapter() == null) {
            // Creating an instance of ArrayAdapter with the context, layout file and list of items.
            ArrayAdapter<Contact> adapter = new ArrayAdapter<Contact>(this, R.layout.contact_layout, contacts) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    // Getting the User object at the current position
                    Contact contact = getItem(position);
                    // Inflating the layout if it is not already inflated
                    if (convertView == null) {
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_layout, parent, false);
                    }

                    // Finding the TextView and setting the name of the user
                    TextView textView = convertView.findViewById(R.id.textViewContactName);
                    textView.setText(contact.getName());

                    ImageButton btnDelete = convertView.findViewById(R.id.buttonDelete);

                    contactsListView.setOnItemClickListener((parent1, view, position1, id) ->
                    {
                        Contact clickedContact = getItem(position1);
                        showDialogCreateOrUpdateContact(clickedContact);
                    });

                    // Set OnClickListener for the delete Contact button
                    btnDelete.setOnClickListener(v ->
                    {
                        // Handle the click event for the ImageView
                        AppDatabase db = SignupLogin_Activity.getDb();
                        UserDao userDao = db.userDao();

                        new Thread(() -> userDao.deleteContactByPhoneNumber(user.getUserName(), contact.getPhone())).start();
                        Toast.makeText(MainActivity.this, "Contact deleted successfully!", Toast.LENGTH_LONG).show();
                    });

                    return convertView;
                }
            };
            // Setting the adapter to the ListView
            contactsListView.setAdapter(adapter);
        } else {
            ArrayAdapter<Contact> adapter = (ArrayAdapter<Contact>) contactsListView.getAdapter();
            adapter.clear();
            adapter.addAll(contacts);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * the function is getting a String name of a Contact and a GenderCallback.
     * it uses Retrofit for the API call to the Gender API and after it getting the gender,
     * it puts it in a local variable and putting the gender received in the callback so only
     * after it, all of the other code that using this callback will move on
     *
     * @param name - a String of a Contact name
     * @param callback- a Gender callback that will be done after putting in the gender received
     */
    private void gettingGenderByName(String name, GenderCallback callback) {
        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.genderize.io/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create an instance of the API interface
        GenderApi genderApi = retrofit.create(GenderApi.class);

        // Make the network request
        Call<GenderResponse> call = genderApi.getGender(name);
        call.enqueue(new Callback<GenderResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenderResponse> call, @NonNull Response<GenderResponse> response) {
                if (response.isSuccessful()) {
                    // Get the gender from the response and use it
                    GenderResponse genderResponse = response.body();
                    gender = genderResponse.getGender();
                    callback.onGenderReceived(gender);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenderResponse> call, @NonNull Throwable t) {
                callback.onGenderReceived(null); // or you might want to handle this differently
            }
        });
    }

    /**
     * the function is showing an AlertDialog of create a Contact or Updating a Contact based on the
     * Contact parameter that if it's null it will be create a Contact and if not it will be update.
     * in the AlertDialog there will Views that the user can create or update the Contact.
     *
     * @param clickedContact- a Contact object
     */
    public void showDialogCreateOrUpdateContact(Contact clickedContact) {
        // Creating an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate the custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.create_contact_alert_dialog, null);


        TextView textViewTitle = dialogView.findViewById(R.id.textViewTitleCreateOrUpdate);
        // getting the views from the xml
        Button birthdateBtn = dialogView.findViewById(R.id.birthdateBtn);
        birthdateBtn.setOnClickListener(v ->
        {
            getBirthdateFromUser(dialogView, clickedContact); // get into a class variable the birthdate chosen
        });

        EditText contactNameEditText = dialogView.findViewById(R.id.editTextNameContact);
        EditText contactPhoneEditText = dialogView.findViewById(R.id.editTextPhoneNumberContact);
        EditText contactEmailEditText = dialogView.findViewById(R.id.editTextEmailContact);
        TextView contactNameError = dialogView.findViewById(R.id.textViewContactNameError);
        TextView contactPhoneError = dialogView.findViewById(R.id.textViewContactPhoneNumberError);
        TextView contactEmailError = dialogView.findViewById(R.id.textViewContactEmailError);
        TextView contactBirthDateError = dialogView.findViewById(R.id.errorMessageBirthday);
        TextView textViewGender = dialogView.findViewById(R.id.textViewGender);


        // Setting the custom layout to the AlertDialog builder
        builder.setView(dialogView);

        // Creating and showing the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

        Button submitButton = dialogView.findViewById(R.id.btnSubmit);
        Button cancelButton = dialogView.findViewById(R.id.btnCancel);

        if (clickedContact == null)// it's the create contact dialog
        {
            textViewTitle.setText("Create Contact");
            // Set the actions for your buttons (submit, cancel, etc.)

            submitButton.setOnClickListener(v ->
            {
                String contactName = contactNameEditText.getText().toString();
                String contactPhone = contactPhoneEditText.getText().toString();
                String contactEmail = contactEmailEditText.getText().toString();

                // a boolean result that will indicate if there were errors in the inputs or not
                boolean inputsErrorResult = thereAreErrors(contactName, contactNameError,
                        contactPhone, contactPhoneError,
                        contactEmail, contactEmailError, contactBirthDateError);
                if (!inputsErrorResult)// no errors, can create contact
                {
                    gettingGenderByName(contactName, genderReceived ->
                    {
                        if (genderReceived != null && !genderReceived.trim().isEmpty()) {
                            gender = genderReceived;
                            Contact contact = new Contact(user.getUserName(), contactPhone, contactName, contactEmail, gender, chosenBirthDate);

                            new Thread(() ->
                            {
                                UserDao userDao = db.userDao();
                                int result = userDao.countContactsByPhoneForUser(user.getUserName(), contactPhone);
                                if (result == 0) {
                                    userDao.insertContact(contact);
                                    runOnUiThread(() ->
                                            Toast.makeText(MainActivity.this, "Contact created successfully!", Toast.LENGTH_LONG).show());
                                    chosenBirthDate = null;
                                    gender = null;
                                    dialog.dismiss();
                                } else {
                                    runOnUiThread(() ->
                                            Toast.makeText(MainActivity.this, "Contact's Phone number already exists!", Toast.LENGTH_LONG).show());
                                }
                            }).start();
                        }
                    });
                }
            });


            cancelButton.setOnClickListener(v ->
            {
                // Handle cancel button click
                chosenBirthDate = null;
                gender = null;
                dialog.dismiss();
            });
        } else // it's the update contact dialog
        {
            textViewTitle.setText("Update Contact");
            contactNameEditText.setText(clickedContact.getName());
            String oldPhoneNumber = clickedContact.getPhone();
            contactPhoneEditText.setText(clickedContact.getPhone());
            contactEmailEditText.setText(clickedContact.getEmail());
            calculateAge(clickedContact.getBirthdate(), dialogView);
            textViewGender.setText(clickedContact.getGender());

            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String contactName = contactNameEditText.getText().toString();
                    String contactPhone = contactPhoneEditText.getText().toString();
                    String contactEmail = contactEmailEditText.getText().toString();
                    if (chosenBirthDate == null)
                        chosenBirthDate = clickedContact.getBirthdate();
                    // a boolean result that will indicate if there were errors in the inputs or not
                    boolean inputsErrorResult = thereAreErrors(contactName, contactNameError,
                            contactPhone, contactPhoneError,
                            contactEmail, contactEmailError, contactBirthDateError);
                    if (!inputsErrorResult)// no errors, can create contact
                    {
                        gettingGenderByName(contactName, genderReceived ->
                        {
                            boolean needToUpdate = false;
                            if (!contactName.equals(clickedContact.getName()) || !contactPhone.equals(clickedContact.getPhone()) ||
                                    !contactEmail.equals(clickedContact.getEmail()) ||
                                    (chosenBirthDate != null && !chosenBirthDate.equals(clickedContact.getBirthdate()))
                                    || !gender.equals(clickedContact.getGender()))
                                needToUpdate = true;
                            gender = genderReceived;
                            if (needToUpdate) {

                                new Thread(() -> {
                                    Contact contact = new Contact(user.getUserName(), contactPhone, contactName, contactEmail, gender, chosenBirthDate);
                                    int result = 0;

                                    if (!contactPhone.equals(oldPhoneNumber)) {
                                        result = userDao.countContactsByPhoneForUser(user.getUserName(), contactPhone);
                                    }
                                    if (result == 0)// we can update with contact with new or the same phone number
                                    {
                                        userDao.updateContact(user.getUserName(), oldPhoneNumber, contact);
                                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Contact Updated successfully!", Toast.LENGTH_LONG).show());
                                        chosenBirthDate = null;
                                        gender = null;
                                        dialog.dismiss();
                                    } else {
                                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Contact's Phone number already exists!", Toast.LENGTH_LONG).show());
                                    }
                                }).start();
                            } else {
                                chosenBirthDate = null;
                                gender = null;
                                dialog.dismiss();
                            }
                        });
                    }
                }
            });

            cancelButton.setOnClickListener(v ->
            {
                // Handle cancel button click
                chosenBirthDate = null;
                gender = null;
                dialog.dismiss();
            });
        }
    }

    /**
     * this function is called from the onClickListener of the birthdate button when creating or
     * updating a user. in the function there will be opened a DatePickerDialog that let the user
     * choose a birthdate and after choosing it will put the birthdate in a class variable and
     * after it, it will put it with the Contacts age in a TextView that in the dialogView parameter
     *
     * @param dialogView - an AlertDialog View that from it the function is calling
     * @param contact    - the Contact that is details in the dialogView if it's from a Update Contact dialog
     */
    private void getBirthdateFromUser(View dialogView, Contact contact) {
        int minAge = 5; // the minimum age that the contact can be
        TextView birthdateTextView = dialogView.findViewById(R.id.birthdateText);
        int year = 2000, month = 0, day = 1;

        // Create SimpleDateFormat object with source string format
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        TextView dialogTitle = dialogView.findViewById(R.id.textViewTitleCreateOrUpdate);
        String dialogTitleString = dialogTitle.getText().toString();
        if (dialogTitleString.contains("Update")) {
            String birthdateContact = contact.getBirthdate();
            if (birthdateContact != null) {
                try {
                    Date date = format.parse(birthdateContact);

                    // Use Calendar class to extract day, month, and year
                    if (date != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        year = calendar.get(Calendar.YEAR);
                        month = calendar.get(Calendar.MONTH);
                        day = calendar.get(Calendar.DAY_OF_MONTH);
                    }
                } catch (ParseException e) {
                    e.printStackTrace(); // handle the exception
                }
            }
        }

        // Calculate the maximum date (5 years ago from today)
        Calendar calendarMax = Calendar.getInstance();
        calendarMax.add(Calendar.YEAR, -minAge);
        long maxDate = calendarMax.getTimeInMillis();

        // Create a new instance of DatePickerDialog and set the maximum date
        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                (view, year1, month1, dayOfMonth) -> {
                    Calendar currentYear = Calendar.getInstance();
                    Calendar dateOfBirth = Calendar.getInstance();
                    dateOfBirth.set(year1, month1, dayOfMonth); // Set the date of birth here
                    int age = currentYear.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);

                    // Checking if the user hasn't had their birthday yet this year
                    if (currentYear.get(Calendar.DAY_OF_YEAR) < dateOfBirth.get(Calendar.DAY_OF_YEAR)) {
                        age--;
                    }
                    String birthdate = dayOfMonth + "/" + (month1 + 1) + "/" + year1 + ", age : " + age;
                    birthdateTextView.setText(birthdate);
                    String selectedBirthdate = year1 + "-" + (month1 + 1) + "-" + dayOfMonth;
                    chosenBirthDate = selectedBirthdate;
                }, year, month, day); // Setting the initial date to the date values
        datePickerDialog.getDatePicker().setMaxDate(maxDate);
        datePickerDialog.show();
    }

    /**
     * the function is calculating the Contact's age based on it's birthdate parameter and then
     * putting it in a TextView from the dialogView in the parameter
     *
     * @param contactBirthDate- a String that represent the Contact's birthdate
     * @param dialogView        - an AlertDialog View
     */
    public void calculateAge(String contactBirthDate, View dialogView) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date birthDate = format.parse(contactBirthDate);
            Calendar birthCalendar = Calendar.getInstance();
            birthCalendar.setTime(birthDate);

            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--; // Adjusting the age if birthday hasn't occurred yet this year
            }

            int birthDay = birthCalendar.get(Calendar.DAY_OF_MONTH);
            int birthMonth = birthCalendar.get(Calendar.MONTH) + 1; // Months are zero-based in Calendar
            int birthYear = birthCalendar.get(Calendar.YEAR);

            String ageString = birthDay + "/" + birthMonth + "/" + birthYear + ", age: " + age;
            TextView birthdateText = dialogView.findViewById(R.id.birthdateText);
            birthdateText.setText(ageString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * the function getting Strings representing the create/update Contact inputs and
     * TextViews that representing Error messages from the AlertDialog and this function checking
     * errors if there are it will return true and will fill the errors in the specific TextViews,
     * and else it will return false and will put "" in the TextViews
     *
     * @return - true if there is at least one error, and else false
     */
    private boolean thereAreErrors(String contactName, TextView contactNameError,
                                   String contactPhone, TextView contactPhoneError,
                                   String contactEmail, TextView contactEmailError,
                                   TextView contactBirthDateError) {
        boolean thereIsAnError = false;

        if (contactName.equals("")) {
            thereIsAnError = true;
            contactNameError.setText("Contact name is empty!");
        } else {
            contactNameError.setText("");
        }

        if (contactPhone.equals("")) {
            thereIsAnError = true;
            contactPhoneError.setText("Contact Phone number is empty!");
        } else if (contactPhone.length() < MIN_PHONE_NUMBER_LENGTH) {
            thereIsAnError = true;
            contactPhoneError.setText("Contact Phone number minimum length is 10 digits!");
        } else {
            contactPhoneError.setText("");
        }

        if (contactEmail.equals("")) {
            thereIsAnError = true;
            contactEmailError.setText("Contact Email is empty!");
        } else if (!isEmailValid(contactEmail)) {
            thereIsAnError = true;
            contactEmailError.setText("Contact Email address is not valid!");
        } else {
            contactEmailError.setText("");
        }

        if (chosenBirthDate == null) {
            thereIsAnError = true;
            contactBirthDateError.setText("Birthdate not chosen");
        } else {
            contactBirthDateError.setText("");
        }

        return thereIsAnError;
    }

    /**
     * a function that checking a String representing an email
     *
     * @param email - a String representing an email
     * @return - true if the email is valid and else false
     */
    private boolean isEmailValid(String email) { // returns true if the email is valid and else false
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * this function is the on click function the Add Contact button
     *
     * @param view - a Button View
     */
    public void addContact(View view) {
        showDialogCreateOrUpdateContact(null);
    }

    /**
     * this function is an Override to the onBackPressed function for
     * letting the user that he/she will be "signed out from the app" and will be returned back
     * to the Signup/Login Activity, and the user's choice will make the action of staying or leaving
     */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) ->
                {
                    Intent intent = new Intent(MainActivity.this, SignupLogin_Activity.class);
                    startActivity(intent);
                    finish(); // This closes the current activity
                })
                .setNegativeButton("No", (dialog, which) ->
                {
                    // User pressed "No", dismissing the dialog and do nothing
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert) // Set the icon for the dialog (optional)
                .show(); // Show the dialog
    }
}