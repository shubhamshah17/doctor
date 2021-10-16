package com.example.doctorapp;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

public class signUpPageTwo extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    TextView UserDOB, DOBPicker, name, skip;
    TextInputLayout et_allergies;
    RadioButton RadioMale, RadioFemale, RadioOther;
    RadioGroup Radiogender;
    Spinner bloodgrp;
    Button saveProfile;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore fstore;
    DocumentReference df;

    Calendar calendar = Calendar.getInstance();
    String[] allBloodGroups = {"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};
    String blood;
    String uid, username, Age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_page_two);
        changeStatusBarColor();

        firebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        name = findViewById(R.id.name);
        skip = findViewById(R.id.skip);
        UserDOB = findViewById(R.id.UserDOB);
        DOBPicker = findViewById(R.id.DOBPicker);
        et_allergies = findViewById(R.id.et_allergies);
        bloodgrp = findViewById(R.id.bloodgrp);
        saveProfile = findViewById(R.id.saveProfile);
        Radiogender = findViewById(R.id.Radiogender);
        RadioMale = findViewById(R.id.RadioMale);
        RadioFemale = findViewById(R.id.RadioFemale);
        RadioOther = findViewById(R.id.RadioOther);

        uid = firebaseAuth.getCurrentUser().getUid();

        df = fstore.collection("users").document(uid);
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                username = documentSnapshot.getString("Name");
                String result = username.replaceAll(" .+$", "");
                name.setText("Welcome " + result + "!!");
            }
        });


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
                DatePickerDialog datePickerDialog = new DatePickerDialog(signUpPageTwo.this, date, calendar.get(Calendar.YEAR)
                        , calendar.get(Calendar.MONTH)
                        , calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
                datePickerDialog.show();
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(signUpPageTwo.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog pd = new ProgressDialog(signUpPageTwo.this, R.style.MyAlertDialogStyle);
                pd.setMessage("Saving Profile...");
                pd.setCancelable(false);
                pd.show();
                UpdateProfile();
                pd.dismiss();
                Toasty.success(signUpPageTwo.this, "Saved", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(signUpPageTwo.this, MainActivity.class);
                startActivity(intent);
                finish();

            }
        });
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        blood = bloodgrp.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void UpdateProfile() {
        String DOB = UserDOB.getText().toString();
        String Allergy = et_allergies.getEditText().getText().toString();

        Map<String, Object> updates = new HashMap<>();
        if (!(DOB.equals(""))) {
            updates.put("DOB", DOB);
        }

        if (Radiogender.getCheckedRadioButtonId() == R.id.RadioMale) {
            updates.put("Gender", "Male");
        } else if (Radiogender.getCheckedRadioButtonId() == R.id.RadioFemale) {
            updates.put("Gender", "Female");
        } else if (Radiogender.getCheckedRadioButtonId() == R.id.RadioOther) {
            updates.put("Gender", "Other");
        }
        if (Allergy.equals("")) {
            updates.put("Allergy", "None");
        } else {
            updates.put("Allergy", Allergy);
        }
        updates.put("BloodGroup", blood);
        if (Age != null) {
            updates.put("Age", Age);
        }

        df.set(updates, SetOptions.merge());
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

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }
}