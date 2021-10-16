package com.example.doctorapp;

import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyProfilePage extends AppCompatActivity {

    TextView fullname, nameOfUser, useremail, userphone;
    TextView gender, UserAge, userDOB, userBlood;
    ImageView profile_image;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore fstore;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile_page);
        changeStatusBarColor();

        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.register_bk_color)));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Profile");
        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_backbutton);
        upArrow.setColorFilter(getResources().getColor(R.color.whiteTextColor), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        firebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        nameOfUser = findViewById(R.id.nameOfUser);
        useremail = findViewById(R.id.useremail);
        userphone = findViewById(R.id.userphone);
        fullname = findViewById(R.id.fullname);
        gender = findViewById(R.id.gender);
        UserAge = findViewById(R.id.UserAge);
        userDOB = findViewById(R.id.userDOB);
        userBlood = findViewById(R.id.userBlood);
        profile_image = findViewById(R.id.profile_image);


        showuserData(firebaseAuth.getCurrentUser().getUid());

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }
        return true;
    }

    private void showuserData(String uid) {
        DocumentReference df = fstore.collection("users").document(uid);
        df.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String name = documentSnapshot.getString("Name");
                        String email = documentSnapshot.getString("UserEmail");
                        String phone = documentSnapshot.getString("UserPhone");
                        String age = documentSnapshot.getString("Age");
                        String Gender = documentSnapshot.getString("Gender");
                        String DateOfBirth = documentSnapshot.getString("DOB");
                        String bloodGroup = documentSnapshot.getString("BloodGroup");
                        nameOfUser.setText(name);
                        fullname.setText(name);
                        useremail.setText(email);
                        userphone.setText("+91 " + phone);
                        if (documentSnapshot.contains("Gender")) {
                            if (Gender.equals("Male")) {
                                gender.setText(Gender);
                                profile_image.setImageResource(R.drawable.ic_profile_male);
                            } else if (Gender.equals("Female")) {
                                gender.setText(Gender);
                                profile_image.setImageResource(R.drawable.ic_profile_female);
                            } else if (Gender.equals("Other")) {
                                gender.setText(Gender);
                            }
                        } else {
                            gender.setText("Not Set");
                        }
                        if (documentSnapshot.contains("Age")) {
                            UserAge.setText(age);
                        } else {
                            UserAge.setText("Not Set");
                        }
                        if (documentSnapshot.contains("DOB")) {
                            userDOB.setText(DateOfBirth);
                        } else {
                            userDOB.setText("Not Set");
                        }
                        if (documentSnapshot.contains("BloodGroup")) {
                            userBlood.setText(bloodGroup);
                        } else {
                            userBlood.setText("Not Set");
                        }
                    }
                });
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