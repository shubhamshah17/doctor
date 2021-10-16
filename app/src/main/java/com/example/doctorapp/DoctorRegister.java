package com.example.doctorapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class DoctorRegister extends AppCompatActivity {

    TextInputLayout et_Docname,et_Docemail,et_Docphone;
    Button upload,DocRegister;
    TextView tyMsg,camera;
    ImageView photo;

    Boolean clicked=false;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore fstore;
    StorageReference mStorageRef;
    CardView cardview;
    ScrollView scrollview;


    Uri contentUri;
    ProgressDialog pd;
    public static final int GALLERY_REQUEST_CODE = 105;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_register);
        changeStatusBarColor();

        et_Docname = findViewById(R.id.et_Docname);
        et_Docemail = findViewById(R.id.et_Docemail);
        et_Docphone = findViewById(R.id.et_Docphone);
        DocRegister = findViewById(R.id.DocRegister);
        cardview = findViewById(R.id.cardview);
        tyMsg = findViewById(R.id.tyMsg);
        scrollview = findViewById(R.id.scrollview);

        camera = findViewById(R.id.camera);
        upload = findViewById(R.id.upload);
        photo = findViewById(R.id.image);

        firebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                askCameraPermissions();
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery, GALLERY_REQUEST_CODE);
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isconnected(getApplicationContext())){
                    AlertDialog.Builder builder = new AlertDialog.Builder(DoctorRegister.this);
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
                    uploadimage();
                }
            }
        });

        DocRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = et_Docname.getEditText().getText().toString();
                final String email = et_Docemail.getEditText().getText().toString();
                final String phone = et_Docphone.getEditText().getText().toString();

                if(!validateName() | !validatePhoneNo() | !validateEmail()| !validateimage()){
                    return;
                } else if (!isconnected(getApplicationContext())){
                    AlertDialog.Builder builder = new AlertDialog.Builder(DoctorRegister.this);
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
                    pd = new ProgressDialog(DoctorRegister.this, R.style.MyAlertDialogStyle);
                    pd.setMessage("Processing your request...");
                    pd.setCancelable(false);
                    pd.show();



                    DocumentReference df = fstore.collection("Doctors").document();
                    Map<String,Object> userInfo = new HashMap<>();
                    userInfo.put("Name",name);
                    userInfo.put("DoctorEmail",email);
                    userInfo.put("DoctorPhone",phone);

                    df.set(userInfo);

                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        cardview.setVisibility(View.INVISIBLE);
                        tyMsg.setVisibility(View.VISIBLE);
                        scrollview.scrollTo(0,0);
                        Toasty.success(DoctorRegister.this,"Details Sent to the admin", Toast.LENGTH_SHORT).show();
                    }
                },2000);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            photo.setVisibility(View.VISIBLE);
            contentUri = data.getData();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "." + getFileExt(contentUri);
            Log.d("tag", "onActivityResult: Gallery Image Uri:  " + imageFileName);
            photo.setImageURI(contentUri);
            upload.setVisibility(View.VISIBLE);
        }
    }
    private String getFileExt(Uri contentUri) {
        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }

    private void uploadimage() {
        final ProgressDialog p = new ProgressDialog(DoctorRegister.this, R.style.MyAlertDialogStyle);
        p.setMessage("Uploading Image...");
        p.setCancelable(false);
        p.show();
        final String random = UUID.randomUUID().toString();
        final StorageReference imageRef = mStorageRef.child("DoctorImages/" + random);
        imageRef.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("tag", "onSuccess: Uploaded Image URl is " + uri.toString());
//                        Picasso.get().load(uri).into(selectedImage);
                    }
                });
                upload.setVisibility(View.GONE);
                camera.setVisibility(View.INVISIBLE);
                p.dismiss();
                clicked = true;
                Toasty.success(DoctorRegister.this, "Image Uploaded.", Toast.LENGTH_SHORT,false).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                p.dismiss();
                Toasty.error(DoctorRegister.this, "Upload Failed.", Toast.LENGTH_SHORT,false).show();
            }
        });
    }

    private boolean isconnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(connectivityManager.TYPE_WIFI);
        NetworkInfo Mobile = connectivityManager.getNetworkInfo(connectivityManager.TYPE_MOBILE);

        if ((wifi != null && wifi.isConnected() || Mobile !=null && Mobile.isConnected())){
            return true;
        } else{
            return false;
        }
    }
    private Boolean validateName() {
        String val = et_Docname.getEditText().getText().toString();

        if (val.isEmpty()) {
            et_Docname.setError("Field cannot be empty");
            YoYo.with(Techniques.Shake)
                    .duration(200)
                    .repeat(1)
                    .playOn(et_Docname);
            return false;
        }
        else {
            et_Docname.setError(null);
            et_Docname.setErrorEnabled(false);
            return true;
        }
    }
    private Boolean validateEmail() {
        String val = et_Docemail.getEditText().getText().toString();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (val.isEmpty()) {
            et_Docemail.setError("Field cannot be empty");
            YoYo.with(Techniques.Shake)
                    .duration(200)
                    .repeat(1)
                    .playOn(et_Docemail);
            return false;
        } else if (!val.matches(emailPattern)) {
            et_Docemail.setError("Invalid email address");
            YoYo.with(Techniques.Shake)
                    .duration(200)
                    .repeat(1)
                    .playOn(et_Docemail);
            return false;
        } else {
            et_Docemail.setError(null);
            et_Docemail.setErrorEnabled(false);
            return true;
        }
    }
    private Boolean validatePhoneNo() {
        String val = et_Docphone.getEditText().getText().toString();

        if (val.isEmpty()) {
            et_Docphone.setError("Field cannot be empty");
            YoYo.with(Techniques.Shake)
                    .duration(200)
                    .repeat(1)
                    .playOn(et_Docphone);
            return false;
        } else if (val.length() < 10 | val.length() > 10) {
            et_Docphone.setError("Please enter Valid phone number");
            YoYo.with(Techniques.Shake)
                    .duration(200)
                    .repeat(1)
                    .playOn(et_Docphone);
            return false;
        } else {
            et_Docphone.setError(null);
            et_Docphone.setErrorEnabled(false);
            return true;
        }
    }
    private Boolean validateimage(){
        if (photo.getDrawable() == null){
            camera.setError("Upload Image");
            return false;
        } else if(!clicked) {
            camera.setError("Upload Image");
            Toasty.error(DoctorRegister.this, "Upload image to proceed.", Toast.LENGTH_SHORT,false).show();
            return false;
        } else {
            camera.setError(null);
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
}