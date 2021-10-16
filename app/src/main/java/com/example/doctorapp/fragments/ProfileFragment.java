package com.example.doctorapp.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.doctorapp.AboutUsPage;
import com.example.doctorapp.BMICalcActivity;
import com.example.doctorapp.DoctorRegister;
import com.example.doctorapp.EditPassword;
import com.example.doctorapp.EditProfilePage;
import com.example.doctorapp.FeedBackActivity;
import com.example.doctorapp.MyProfilePage;
import com.example.doctorapp.R;
import com.example.doctorapp.loginActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import es.dmoral.toasty.Toasty;

public class ProfileFragment extends Fragment
        implements View.OnClickListener {

    TextView username, BMICalc, editprofile, editPass, aboutUs, areUDoctor, shareapp, feedback, logout;
    RelativeLayout myprofile;
    SwitchCompat NotifcationSwitch;
    SharedPreferences sharedPreferences;


    FirebaseAuth firebaseAuth;
    FirebaseFirestore fstore;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, null);

        firebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        showuserData(firebaseAuth.getCurrentUser().getUid());

        sharedPreferences = getActivity().getSharedPreferences("User", Context.MODE_PRIVATE);


        logout = view.findViewById(R.id.logout);
        NotifcationSwitch = view.findViewById(R.id.NotifcationSwitch);
        editprofile = view.findViewById(R.id.editprofile);
        editPass = view.findViewById(R.id.editPass);
        myprofile = view.findViewById(R.id.myprofile);
        BMICalc = view.findViewById(R.id.BMICalc);
        shareapp = view.findViewById(R.id.shareapp);
        aboutUs = view.findViewById(R.id.aboutUs);
        areUDoctor = view.findViewById(R.id.areUDoctor);
        feedback = view.findViewById(R.id.feedback);
        username = view.findViewById(R.id.username);

        NotifcationSwitch.setChecked(sharedPreferences.getBoolean("NotificationSwitch", true));

        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FeedBackActivity.class);
                startActivity(intent);
            }
        });

        areUDoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DoctorRegister.class);
                startActivity(intent);
            }
        });

        shareapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                String shareBody = "I booked my doctor’s appointment for free with the Remedium App." +
                        "\nFind the leading doctors & book your appointment today. Download the App at \n" +
                        "https://drive.google.com/file/d/1d23GmApMjeij70docobPPiQTvayaEu9a/view?usp=sharing";
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(intent, "Share Via"));
            }
        });


        aboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AboutUsPage.class);
                startActivity(intent);
            }
        });

        myprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MyProfilePage.class);
                startActivity(intent);
            }
        });
        BMICalc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BMICalcActivity.class);
                startActivity(intent);
            }
        });

        NotifcationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NotifcationSwitch.isChecked()) {
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("User", Context.MODE_PRIVATE).edit();
                    editor.putBoolean("NotificationSwitch", true);
                    NotifcationSwitch.setChecked(true);
                    editor.apply();
                } else {
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("User", Context.MODE_PRIVATE).edit();
                    editor.putBoolean("NotificationSwitch", false);
                    NotifcationSwitch.setChecked(false);
                    editor.apply();
                }
            }
        });

        editprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditProfilePage.class);
                startActivity(intent);
            }
        });
        editPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditPassword.class);
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserLogout();
            }
        });

        return view;
    }

    private void showuserData(String uid) {
        DocumentReference df = fstore.collection("users").document(uid);
        df.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String name = documentSnapshot.getString("Name");
                        username.setText(name.toUpperCase());
                    }
                });
    }

    private void UserLogout() {
        if (!isconnected(getActivity())) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            alertDialog.setTitle("Confirm Signout")
                    .setMessage("Are you sure you want to Signout?")
                    .setPositiveButton("YES",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseAuth.getInstance().signOut();
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean("login_status_shared_preferences", false);
                                    editor.apply();
                                    sharedPreferences.edit().remove("userChoiceSpinner").apply();
                                    final ProgressDialog pd = new ProgressDialog(getActivity(), R.style.MyAlertDialogStyle);
                                    pd.setMessage("Signing Out...");
                                    pd.setCancelable(false);
                                    pd.show();
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            pd.dismiss();
                                            Intent intent = new Intent(getActivity(), loginActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            Toasty.success(getActivity(), "Signed out", Toasty.LENGTH_SHORT).show();
                                        }
                                    }, 2000);
                                }
                            })
                    .setNegativeButton("NO",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).show();
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

    @Override
    public void onClick(View v) {

    }
}