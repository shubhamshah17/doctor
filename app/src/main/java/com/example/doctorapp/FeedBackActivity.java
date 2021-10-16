package com.example.doctorapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hsalf.smileyrating.SmileyRating;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class FeedBackActivity extends AppCompatActivity {

    TextView name, email;
    EditText feedback_text;
    Button feedback_submit;
    SmileyRating smilerating;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore fstore;
    FirebaseUser firebaseUser;
    DocumentReference df;

    String phone;
    Boolean clicked = false;
    int rating;
    String Username, Useremail;
    ProgressDialog pd;
    String currentTime = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(new Date());
    String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        changeStatusBarColor();

        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.register_bk_color)));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("FeedBack");
        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_backbutton);
        upArrow.setColorFilter(getResources().getColor(R.color.whiteTextColor), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        firebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        feedback_text = findViewById(R.id.feedback_text);
        feedback_submit = findViewById(R.id.feedback_submit);
        smilerating = (SmileyRating) findViewById(R.id.smilerating);

        showuserData(firebaseAuth.getCurrentUser().getUid());
        getUserData();

        feedback_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!clicked) {
                    if (!isconnected(getApplicationContext())) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(FeedBackActivity.this);
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
                    } else if (!validateFeedback() | !validateRating()) {
                        return;
                    } else {
                        pd = new ProgressDialog(FeedBackActivity.this, R.style.MyAlertDialogStyle);
                        pd.setMessage("Sending Feedback...");
                        pd.setCancelable(false);
                        pd.show();
                        putdata();
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                clicked = false;
                            }
                        },60000);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                pd.dismiss();
                                Toasty.success(FeedBackActivity.this, "FeedBack Sent", Toast.LENGTH_SHORT, false).show();
//                            FeedBackActivity.super.onBackPressed();
                                feedback_text.setText("");
                                clicked = true;

                            }
                        }, 1100);
                    }
                } else {
                    Toasty.warning(FeedBackActivity.this, "Please wait before sending another feedback", Toast.LENGTH_SHORT, false).show();
                }
            }
        });

        smilerating.setSmileySelectedListener(new SmileyRating.OnSmileySelectedListener() {
            @Override
            public void onSmileySelected(SmileyRating.Type type) {
//                if (SmileyRating.Type.GREAT == type) {
//                    Log.d("TAG", "Wow, the user gave high rating");
//                }
                rating = type.getRating();
            }
        });
    }

    private void showuserData(String uid) {
        DocumentReference df = fstore.collection("users").document(uid);
        df.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Username = documentSnapshot.getString("Name");
                        Useremail = documentSnapshot.getString("UserEmail");
                        name.setText(Username);
                        email.setText(Useremail);
                    }
                });
    }

    public void putdata() {
        String Feedback = feedback_text.getText().toString();
        df = fstore.collection("FeedBack").document();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("Name", Username);
        userInfo.put("UserEmail", Useremail);
        userInfo.put("FeedBack", Feedback);
        userInfo.put("Date", currentDate);
        userInfo.put("Time", currentTime);
        userInfo.put("Phone", phone);
        userInfo.put("Rating", String.valueOf(rating));
        df.set(userInfo);
    }

    private void getUserData() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        df = fstore.collection("users").document(firebaseUser.getUid());
        df.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        phone = documentSnapshot.getString("UserPhone");
                    }
                });
    }

    private Boolean validateFeedback() {
        String val = feedback_text.getText().toString();

        if (!(val.isEmpty())) {
            if (val.length() < 5) {
                feedback_text.setError("Feedback is too short");
                return false;
            } else {
                feedback_text.setError(null);
                return true;
            }
        } else {
            feedback_text.setError(null);
            return true;
        }
    }

    private Boolean validateRating() {
        if (rating == 0) {
            Toasty.error(FeedBackActivity.this, "Please select rating", Toast.LENGTH_SHORT, false).show();
            return false;
        } else {
            return true;
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

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }
        return true;
    }
}