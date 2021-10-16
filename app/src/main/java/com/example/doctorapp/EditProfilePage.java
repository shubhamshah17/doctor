package com.example.doctorapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class EditProfilePage extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {

    TextInputLayout et_name, et_phone, et_allergies;
    Button updateprofile;
    RadioButton RadioMale, RadioFemale, RadioOther;
    RadioGroup Radiogender;
    ImageView DOBPicker;
    TextView UserDOB;
    Spinner bloodgrp;

    ProgressDialog pd, updatesProgress;
    DocumentReference df;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore fstore;
    SharedPreferences sharedPreferences;

    Calendar calendar = Calendar.getInstance();
    String[] allBloodGroups = {"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};
    String blood, Age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_page);
        changeStatusBarColor();

        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.register_bk_color)));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Edit Profile");
        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_backbutton);
        upArrow.setColorFilter(getResources().getColor(R.color.whiteTextColor), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        sharedPreferences = getSharedPreferences("User", MODE_PRIVATE);

        firebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        showuserData(firebaseAuth.getCurrentUser().getUid());

        et_name = findViewById(R.id.et_name);
        et_phone = findViewById(R.id.et_phone);
        et_allergies = findViewById(R.id.et_allergies);
        DOBPicker = findViewById(R.id.DOBPicker);
        UserDOB = findViewById(R.id.UserDOB);
        updateprofile = findViewById(R.id.updateprofile);
        Radiogender = findViewById(R.id.Radiogender);
        RadioMale = findViewById(R.id.RadioMale);
        RadioFemale = findViewById(R.id.RadioFemale);
        RadioOther = findViewById(R.id.RadioOther);
        bloodgrp = findViewById(R.id.bloodgrp);

        pd = new ProgressDialog(EditProfilePage.this, R.style.MyAlertDialogStyle);
        pd.setMessage("Fetching Data...");
        pd.setCancelable(false);
        pd.show();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, allBloodGroups);
        bloodgrp.setAdapter(adapter);
        bloodgrp.setPrompt("Blood group");
        bloodgrp.setOnItemSelectedListener(this);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "dd/MM/yy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                UserDOB.setText(sdf.format(calendar.getTime()));
                Age = Integer.toString(calculateAge(calendar.getTimeInMillis()));

            }
        };
        DOBPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(EditProfilePage.this, date, calendar.get(Calendar.YEAR)
                        , calendar.get(Calendar.MONTH)
                        , calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
                datePickerDialog.show();
            }
        });

        updateprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateName() | !validatePhoneNo()) {
                    return;
                } else if (!isconnected(getApplicationContext())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditProfilePage.this);
                    builder.setTitle("Connection error")
                            .setMessage("Unable to connect with the server.Check your internet connection and try again")
                            .setCancelable(false)
                            .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.show();
                } else {
                    updatesProgress = new ProgressDialog(EditProfilePage.this, R.style.MyAlertDialogStyle);
                    updatesProgress.setMessage("Updating Profile...");
                    updatesProgress.setCancelable(false);
                    updatesProgress.show();
                    UpdateProfile();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updatesProgress.dismiss();
                            Toasty.success(EditProfilePage.this, "Profile Updated", Toasty.LENGTH_SHORT, true).show();
                            EditProfilePage.super.onBackPressed();
                        }
                    }, 2000);
                }
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        ((TextView) view).setTextColor(Color.RED);
        blood = bloodgrp.getSelectedItem().toString();

//        switch (position) {
//            case 0:
//                break;
//            case 1:
//                break;
//            case 2:
//                break;
//            case 3:
//                break;
//            case 4:
//                break;
//            case 5:
//                break;
//            case 6:
//                break;
//            case 7:
//                break;
//        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void showuserData(String uid) {
        df = fstore.collection("users").document(uid);
        df.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        pd.dismiss();
                        String name = documentSnapshot.getString("Name");
                        String phone = documentSnapshot.getString("UserPhone");
                        String gender = documentSnapshot.getString("Gender");
                        String DateOfBirth = documentSnapshot.getString("DOB");
                        String Allergy = documentSnapshot.getString("Allergy");
                        et_name.getEditText().setText(name);
                        et_phone.getEditText().setText(phone);

                        if (!(documentSnapshot.contains("Gender"))) {
                            RadioMale.setError("Select");
                            RadioFemale.setError("Select");
                            RadioOther.setError("Select");
                        } else {
                            if (gender.equals("Male")) {
                                RadioMale.setChecked(true);
                            } else if (gender.equals("Female")) {
                                RadioFemale.setChecked(true);
                            } else if (gender.equals("Other")) {
                                RadioOther.setChecked(true);
                            } else {
                                RadioMale.setChecked(false);
                                RadioFemale.setChecked(false);
                                RadioOther.setChecked(false);
                            }
                        }
                        if (documentSnapshot.contains("DOB")) {
                            UserDOB.setText(DateOfBirth);
                        } else {
                            UserDOB.setError("Complete profile");
                            UserDOB.setText("Click on the calender");
                        }
                        if (documentSnapshot.contains("BloodGroup")) {
                            int spinnerValue = sharedPreferences.getInt("userChoiceSpinner", -1);
                            if (spinnerValue != -1) {
                                // set the selected value of the spinner
                                bloodgrp.setSelection(spinnerValue);
                            }
                        }
                        if (documentSnapshot.contains("Allergy")) {
                            et_allergies.getEditText().setText(Allergy);
                        }
                    }
                });
    }

    public void UpdateProfile() {
        String name = et_name.getEditText().getText().toString();
        String phone = et_phone.getEditText().getText().toString();
        String Allergy = et_allergies.getEditText().getText().toString();
        String DOB = UserDOB.getText().toString();

        Map<String, Object> updates = new HashMap<>();
        updates.put("Name", name);
        updates.put("UserPhone", phone);
        if (Radiogender.getCheckedRadioButtonId() == R.id.RadioMale) {
            updates.put("Gender", "Male");
        } else if (Radiogender.getCheckedRadioButtonId() == R.id.RadioFemale) {
            updates.put("Gender", "Female");
        } else if (Radiogender.getCheckedRadioButtonId() == R.id.RadioOther) {
            updates.put("Gender", "Other");
        }
        if (Age != null) {
            updates.put("Age", Age);
        }

        if (!DOB.equals("Click on the calender to set your DOB")) {
            updates.put("DOB", DOB);
        }
        updates.put("BloodGroup", blood);

        updates.put("Allergy", Allergy);

        int userChoice = bloodgrp.getSelectedItemPosition();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("userChoiceSpinner", userChoice);
        editor.apply();

        df.set(updates, SetOptions.merge());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    int calculateAge(long date) {
        Calendar dob = Calendar.getInstance();
        dob.setTimeInMillis(date);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH)) {
            age--;
        }
        return age;
    }

    private Boolean validateName() {
        String val = et_name.getEditText().getText().toString();

        if (val.isEmpty()) {
            et_name.setError("Field cannot be empty");
            return false;
        } else if (val.length() > 18) {
            et_name.setError("Name too long");
            return false;
        } else if (val.matches(".*[0-9].*")) {
            et_name.setError("Name cannot contain a number");
            return false;
        } else {
            et_name.setError(null);
            et_name.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePhoneNo() {
        String val = et_phone.getEditText().getText().toString();

        if (val.isEmpty()) {
            et_phone.setError("Field cannot be empty");
            return false;
        } else if (val.length() < 10 | val.length() > 10) {
            et_phone.setError("Please enter Valid phone number");
            return false;
        } else {
            et_phone.setError(null);
            et_phone.setErrorEnabled(false);
            return true;
        }
    }

    private boolean isconnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(connectivityManager.TYPE_WIFI);
        NetworkInfo Mobile = connectivityManager.getNetworkInfo(connectivityManager.TYPE_MOBILE);

        if ((wifi != null && wifi.isConnected() || Mobile != null && Mobile.isConnected())) {
            return true;
        } else {
            return false;
        }
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }
}