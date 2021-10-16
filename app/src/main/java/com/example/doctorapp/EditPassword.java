package com.example.doctorapp;

import android.app.ProgressDialog;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class EditPassword extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseFirestore fstore;
    FirebaseUser firebaseUser;

    TextInputLayout old_password, new_password;
    Button changePass_Btn;

    String newpass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);
        changeStatusBarColor();

        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.register_bk_color)));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Change Password");
        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_backbutton);
        upArrow.setColorFilter(getResources().getColor(R.color.whiteTextColor), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        firebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        changePass_Btn = findViewById(R.id.changePass_Btn);
        old_password = findViewById(R.id.old_password);
        new_password = findViewById(R.id.new_password);

        changePass_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = old_password.getEditText().getText().toString().trim();
                final String newPassword = new_password.getEditText().getText().toString().trim();
                if (!validatePassword()) {
                    return;
                }
                if (newPassword.length() < 6) {
                    new_password.setError("Password too short...Must be greater than 6 digits");
                    return;
                } else {
                    new_password.setError(null);
                    new_password.setErrorEnabled(false);
                }
                final ProgressDialog pd = new ProgressDialog(EditPassword.this, R.style.MyAlertDialogStyle);
                pd.setMessage("Updating Password...");
                pd.setCancelable(false);
                pd.show();
                String email = firebaseUser.getEmail();

                AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);
                firebaseUser.reauthenticate(credential)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                firebaseUser.updatePassword(newPassword)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                pd.dismiss();
                                                newpass = new_password.getEditText().getText().toString();
                                                UpdateData();
                                                Toasty.success(EditPassword.this, "Password Updated", Toasty.LENGTH_SHORT, true).show();
                                                EditPassword.super.onBackPressed();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                pd.dismiss();
                                                Toasty.error(EditPassword.this, e.getMessage(), Toasty.LENGTH_SHORT, true).show();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toasty.error(EditPassword.this, e.getMessage(), Toasty.LENGTH_SHORT, true).show();
                            }
                        });
            }

        });
    }

    private void UpdateData() {
        DocumentReference df = fstore.collection("users").document(firebaseUser.getUid());
        Map<String, Object> updates = new HashMap<>();
        updates.put("UserPassword", newpass);
        df.set(updates, SetOptions.merge());
    }

    private Boolean validatePassword() {
        String val = old_password.getEditText().getText().toString();

        if (val.isEmpty()) {
            old_password.setError("Field cannot be empty");
            return false;
        } else if (val.length() < 6) {
            old_password.setError("Password too short...Must be greater than 6 digits");
            return false;
        } else {
            old_password.setError(null);
            old_password.setErrorEnabled(false);
            return true;
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

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }
}