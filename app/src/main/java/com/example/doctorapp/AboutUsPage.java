package com.example.doctorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Calendar;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutUsPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        changeStatusBarColor();

        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.register_bk_color)));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("About");
        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_backbutton);
        upArrow.setColorFilter(getResources().getColor(R.color.whiteTextColor), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        Element versionElement = new Element();
        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .enableDarkMode(false)
                .setDescription("For millions of people, Remedium is a trusted home for health. It connects them with" +
                        "everything they need to take good care of themselves and their loved ones - finding the right doctor" +
                        "and learning new ways to live healthier")
//                .setCustomFont(String) // or Typeface
                .setImage(R.drawable.ic_aboutus)
                .addGroup("Connect with us")
                .addEmail("shahshubham172@gmail.com")
                .addInstagram("shubham_.shahh","Instagram")
                .addFacebook("","Facebook")
                .addPlayStore("")
                .addWebsite("https://google.com/")
                .addItem(versionElement.setTitle("Version 1.0.0").setGravity(Gravity.CENTER))
//                .addItem(getCopyRightsElement())
                .create();

        setContentView(aboutPage);
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
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }
}