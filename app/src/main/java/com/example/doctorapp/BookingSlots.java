package com.example.doctorapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class BookingSlots extends AppCompatActivity implements View.OnClickListener {

    TextView selectedDate,Noslot;
    Button confirm_appointment;
    CardView c1, c2, c3, c4, c5;
    String time = "";
    int flagChecked = 0;
    Boolean timeclicked = false;
    String name, phone;

    ProgressDialog pd;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore fstore;
    DocumentReference df;

    Calendar calendar = Calendar.getInstance();
    String Mobiletime = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
    int currentTime = Integer.parseInt(Mobiletime);
    String currentDate = new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(new Date());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_slots);
        changeStatusBarColor();
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.register_bk_color)));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Slot Booking");
        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_backbutton);
        upArrow.setColorFilter(getResources().getColor(R.color.whiteTextColor), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        firebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        c1 = findViewById(R.id.slot1);
        c2 = findViewById(R.id.slot2);
        c3 = findViewById(R.id.slot3);
        c4 = findViewById(R.id.slot4);
        c5 = findViewById(R.id.slot5);
        selectedDate = findViewById(R.id.selectedDate);
        Noslot = findViewById(R.id.Noslot);
        confirm_appointment = findViewById(R.id.confirm_appointment);

        checktime();

        getUserData();
        checkBooking();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "dd/MM/yy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                selectedDate.setText(sdf.format(calendar.getTime()));
            }
        };
        selectedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(BookingSlots.this, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)
                        , calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                datePickerDialog.show();
            }
        });

        confirm_appointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateDate() | !validateTime()) {
                    return;
                } else if (!isconnected(getApplicationContext())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BookingSlots.this);
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
                    putdata();
                    pd = new ProgressDialog(BookingSlots.this, R.style.MyAlertDialogStyle);
                    pd.setMessage("Booking appointment");
                    pd.setCancelable(false);
                    pd.show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pd.dismiss();
                            Toasty.success(BookingSlots.this, "Appointment Booked", Toast.LENGTH_SHORT).show();
                            BookingSlots.super.onBackPressed();
                        }
                    }, 2000);
                }
            }
        });
    }

    private void checktime() {
        if (currentTime > 8 && currentTime <= 11) {
            c1.setEnabled(false);
            c1.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        } else if (currentTime > 8 && currentTime <= 12) {
            c1.setEnabled(false);
            c1.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            c2.setEnabled(false);
            c2.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        } else if (currentTime > 8 && currentTime <= 16) {
            c1.setEnabled(false);
            c1.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            c2.setEnabled(false);
            c2.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            c3.setEnabled(false);
            c3.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        } else if (currentTime > 8 && currentTime <= 19) {
            c1.setEnabled(false);
            c1.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            c2.setEnabled(false);
            c2.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            c3.setEnabled(false);
            c3.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            c4.setEnabled(false);
            c4.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        } else if (currentTime > 8 && currentTime <= 24) {
            c1.setEnabled(false);
            c1.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            c2.setEnabled(false);
            c2.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            c3.setEnabled(false);
            c3.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            c4.setEnabled(false);
            c4.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            c5.setEnabled(false);
            c5.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            confirm_appointment.setEnabled(false);
            confirm_appointment.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            Noslot.setVisibility(View.VISIBLE);
        } else {
            c1.setEnabled(true);
            c1.setCardBackgroundColor(getResources().getColor(R.color.skyBlue));
            c2.setEnabled(true);
            c2.setCardBackgroundColor(getResources().getColor(R.color.skyBlue));
            c3.setEnabled(true);
            c3.setCardBackgroundColor(getResources().getColor(R.color.skyBlue));
            c4.setEnabled(true);
            c4.setCardBackgroundColor(getResources().getColor(R.color.skyBlue));
            c5.setEnabled(true);
            c5.setCardBackgroundColor(getResources().getColor(R.color.skyBlue));
        }
    }

    private void checkBooking() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        df = fstore.collection("Appointments").document(firebaseUser.getUid());
        df.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.contains("Time")) {
                            Toasty.error(BookingSlots.this, "Appointment already booked", Toast.LENGTH_SHORT, false).show();
                            confirm_appointment.setEnabled(false);
                            confirm_appointment.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
                        }
                    }
                });
    }

    private void getUserData() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        df = fstore.collection("users").document(firebaseUser.getUid());
        df.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        name = documentSnapshot.getString("Name");
                        phone = documentSnapshot.getString("UserPhone");
                    }
                });
    }


    private Boolean validateDate() {
        String Date = selectedDate.getText().toString();
        if (Date.equals("Select date")) {
            selectedDate.setError("Please select");
            return false;
        } else {
            selectedDate.setError(null);
            return true;
        }
    }

    private Boolean validateTime() {
        if (!timeclicked) {
            Toasty.error(BookingSlots.this, "Fields Empty", Toast.LENGTH_SHORT, false).show();
            return false;
        } else {
            setTime(flagChecked);
            return true;
        }
    }

    public void putdata() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String Date = selectedDate.getText().toString();
        df = fstore.collection("Appointments").document(firebaseUser.getUid());
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("Date", Date);
        setTime(flagChecked);
        userInfo.put("Time", time);
        userInfo.put("Name", name);
        userInfo.put("Phone", phone);
        df.set(userInfo);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.slot1:
                checkIsBooked(1);
                break;
            case R.id.slot2:
                checkIsBooked(2);
                break;
            case R.id.slot3:
                checkIsBooked(3);
                break;
            case R.id.slot4:
                checkIsBooked(4);
                break;
            case R.id.slot5:
                checkIsBooked(5);
                break;
            default:
                break;
        }
    }

    private void checkIsBooked(int i) {
        if (flagChecked != 0) {
            setDefaultColor(flagChecked);
            flagChecked = i;
            setColorGreen(i);
        } else {
            flagChecked = i;
            timeclicked = true;
            setColorGreen(i);
        }
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

    private void setDefaultColor(int i) {
        switch (i) {
            case 1:
                c1.setCardBackgroundColor(getResources().getColor(R.color.skyBlue));
                c1.setEnabled(true);
                break;
            case 2:
                c2.setCardBackgroundColor(getResources().getColor(R.color.skyBlue));
                c2.setEnabled(true);
                break;
            case 3:
                c3.setCardBackgroundColor(getResources().getColor(R.color.skyBlue));
                c3.setEnabled(true);
                break;
            case 4:
                c4.setCardBackgroundColor(getResources().getColor(R.color.skyBlue));
                c4.setEnabled(true);
                break;
            case 5:
                c5.setCardBackgroundColor(getResources().getColor(R.color.skyBlue));
                c5.setEnabled(true);
                break;
            default:
                break;
        }
    }


    private void setColorGreen(int i) {

        switch (i) {
            case 1:
                c1.setCardBackgroundColor(Color.GREEN);
                break;
            case 2:
                c2.setCardBackgroundColor(Color.GREEN);
                break;
            case 3:
                c3.setCardBackgroundColor(Color.GREEN);
                break;
            case 4:
                c4.setCardBackgroundColor(Color.GREEN);
                break;
            case 5:
                c5.setCardBackgroundColor(Color.GREEN);
                break;
            default:
                break;
        }
    }

    private void setTime(int i) {
        switch (i) {
            case 1:
                time = "08:00 - 09:00 AM";
                break;
            case 2:
                time = "11:00 - 12:00 AM";
                break;
            case 3:
                time = "03:00 - 04:00 PM";
                break;
            case 4:
                time = "05:00 - 07:00 PM";
                break;
            case 5:
                time = "08:00 - 09:00 PM";
                break;
            default:
                break;
        }
    }
}