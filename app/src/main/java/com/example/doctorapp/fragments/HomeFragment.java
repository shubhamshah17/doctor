package com.example.doctorapp.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.doctorapp.DoctorList;
import com.example.doctorapp.EditProfilePage;
import com.example.doctorapp.MyProfilePage;
import com.example.doctorapp.R;
import com.example.doctorapp.loginActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.security.spec.ECField;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import im.delight.android.location.SimpleLocation;

public class HomeFragment extends Fragment implements View.OnClickListener {

    TextView textViewUser, userLocation;
    ImageView HomeProfile,closebtn;
    CardView completeProfile;
    SimpleLocation location;
    Button completeNowBtn;

    RelativeLayout physician, stress, eye, dental;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore fstore;
    DocumentReference df;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, null);

        firebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        checkProfileCompletion(firebaseAuth.getCurrentUser().getUid());
        showuserData(firebaseAuth.getCurrentUser().getUid());


        textViewUser = view.findViewById(R.id.textViewUser);
        completeProfile = view.findViewById(R.id.completeProfile);
        completeNowBtn = view.findViewById(R.id.completeNowBtn);
        userLocation = view.findViewById(R.id.userLocation);
        HomeProfile = view.findViewById(R.id.HomeProfile);
        closebtn = view.findViewById(R.id.closebtn);

        physician = view.findViewById(R.id.physician);
        stress = view.findViewById(R.id.stress);
        eye = view.findViewById(R.id.eye);
        dental = view.findViewById(R.id.dental);

        location = new SimpleLocation(getActivity());
        locationEnabled();


        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        } else {
            try {
                location.beginUpdates();
                getUserLocation();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        closebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeProfile.setVisibility(View.GONE);
            }
        });

        completeNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditProfilePage.class);
                startActivity(intent);
            }
        });

        userLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                boolean gps_enabled = false;
                boolean network_enabled = false;
                try {
                    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!gps_enabled && !network_enabled) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Location Services Disabled")
                            .setMessage("Remedium Search needs access to your location.Please turn on location access")
                            .setPositiveButton("Open Settings", new
                                    DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                        }
                                    })
                            .setNegativeButton("Cancel", null)
                            .show();
                } else {
                    try {
                        getUserLocation();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toasty.error(getActivity(), e.getMessage(), Toast.LENGTH_SHORT, false).show();
                    }
                }
            }
        });

        physician.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DoctorList.class);
                startActivity(intent);
            }
        });
        stress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toasty.warning(getActivity(), "Coming Soon", Toast.LENGTH_SHORT, false).show();
            }
        });
        eye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toasty.warning(getActivity(), "Coming Soon", Toast.LENGTH_SHORT, false).show();
            }
        });
        dental.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toasty.warning(getActivity(), "Coming Soon", Toast.LENGTH_SHORT, false).show();
            }
        });

        HomeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MyProfilePage.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            location.beginUpdates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        location.endUpdates();
        // stop location updates (saves battery)
    }

    private void showuserData(String uid) {
        df = fstore.collection("users").document(uid);
        df.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String name = documentSnapshot.getString("Name");
                        String result = name.replaceAll(" .+$", "");
                        textViewUser.setText("Hello " + result);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(getActivity(), "User is Banned", Toasty.LENGTH_SHORT, false).show();
                Intent intent = new Intent(getActivity(), loginActivity.class);
                startActivity(intent);
            }
        });
    }


    private boolean locationEnabled() {
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Location Services Disabled")
                    .setMessage("Remedium Search needs access to your location.Please turn on location access")
                    .setPositiveButton("Open Settings", new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                    .setNegativeButton("Cancel", null)
                    .show();
            return false;
        } else {
            return true;
        }

    }

    @Override
    public void onClick(View v) {

    }

    public void getUserLocation() {
        try {
            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            List<Address> addresses = null;
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.get(0).getSubLocality() == null && addresses.get(0).getLocality() == null) {
                userLocation.setText(addresses.get(0).getAdminArea());
            } else if (addresses.get(0).getSubLocality() == null) {
                userLocation.setText(addresses.get(0).getLocality());
            } else if (addresses.get(0).getLocality() == null) {
                userLocation.setText(addresses.get(0).getLocality());
            } else {
                userLocation.setText(addresses.get(0).getSubLocality() + ',' + addresses.get(0).getLocality());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Boolean checkProfileCompletion(String uid) {
        df = fstore.collection("users").document(uid);
        df.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!(documentSnapshot.contains("DOB"))|!(documentSnapshot.contains("Gender"))){
                            completeProfile.setVisibility(View.VISIBLE);
                        }

                    }
                });

        return null;
    }
}

