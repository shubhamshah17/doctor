package com.example.doctorapp.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.doctorapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class AppointmentFragment extends Fragment {

    TextView noAppointment, Time, Date;
    String date, time;
    Button cancel;
    CardView AppointmentCard;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore fstore;
    FirebaseUser firebaseUser;
    DocumentReference df;

    ProgressDialog pd;
    String currentDate = new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(new Date());
    String Mobiletime = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
    int currentTime = Integer.parseInt(Mobiletime);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appoint, null);

        firebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        noAppointment = view.findViewById(R.id.noAppointment);
        AppointmentCard = view.findViewById(R.id.AppointmentCard);
        Time = view.findViewById(R.id.Time);
        Date = view.findViewById(R.id.Date);
        cancel = view.findViewById(R.id.cancel);

        pd = new ProgressDialog(getActivity(), R.style.MyAlertDialogStyle);
        pd.setMessage("Checking...");
        pd.setCancelable(false);
        pd.show();
        getData();
        pd.dismiss();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                df = fstore.collection("Appointments").document(firebaseUser.getUid());
                df.delete();
                AppointmentCard.animate().translationXBy(2000).setDuration(500);
                Toasty.success(getActivity(), "Appointment Cancelled", Toast.LENGTH_SHORT, false).show();
                pd.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        noAppointment.setVisibility(View.VISIBLE);
                        pd.dismiss();
                    }
                }, 1100);
            }
        });

        return view;
    }

    public void getData() {
        df = fstore.collection("Appointments").document(firebaseUser.getUid());
        df.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.contains("Time")) {
                            noAppointment.setVisibility(View.GONE);
                            AppointmentCard.setVisibility(View.VISIBLE);
                            time = documentSnapshot.getString("Time");
                            Time.setText(time);
                        }
                        date = documentSnapshot.getString("Date");
                        Date.setText(date);
                        if (currentDate.equals(date)) {
                            if (currentTime > 21) {
                                df = fstore.collection("Appointments").document(firebaseUser.getUid());
                                df.delete();
                                Toasty.success(getActivity(), "Your Appointment was Cancelled", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}