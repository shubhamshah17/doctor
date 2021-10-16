package com.example.doctorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

public class BMICalcActivity extends AppCompatActivity {

    TextView result,resultTv,BmiCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_calc);
        changeStatusBarColor();

        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.register_bk_color)));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("BMI Calculator");
        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_backbutton);
        upArrow.setColorFilter(getResources().getColor(R.color.whiteTextColor), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        final EditText e1 = (EditText) findViewById(R.id.weight);
        final EditText e2 = (EditText) findViewById(R.id.height);
        result=(TextView)findViewById(R.id.result);
        resultTv=(TextView)findViewById(R.id.resultTv);
        BmiCategory=(TextView)findViewById(R.id.category);

        findViewById(R.id.calculate).setOnClickListener(new View.OnClickListener() {
            // Logic for validation, input can't be empty
            @Override
            public void onClick(View v) {

                String str1 = e1.getText().toString();
                String str2 = e2.getText().toString();

                if (TextUtils.isEmpty(str1)) {
                    e1.setError("Please enter your weight");
                    e1.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(str2)) {
                    e2.setError("Please enter your height");
                    e2.requestFocus();
                    return;
                }

//Get the user values from the widget reference
                float weight = Float.parseFloat(str1);
                float height = Float.parseFloat(str2) / 100;

//Calculate BMI value
                float bmiValue = calculateBMI(weight, height);

//Define the meaning of the bmi value
                String bmiInterpretation = interpretBMI(bmiValue);

                resultTv.setVisibility(View.VISIBLE);
                String mytext=Float.toString(bmiValue);
                result.setText(mytext);
                BmiCategory.setText(bmiInterpretation);

            }
        });

    }

    //Calculate BMI
    private float calculateBMI(float weight, float height) {
        return (float) (weight / (height * height));
    }

    // Interpret what BMI means
    private String interpretBMI(float bmiValue) {

        if (bmiValue < 16) {
            return "Severely underweight";
        } else if (bmiValue < 18.5) {

            return "Underweight";
        } else if (bmiValue < 25) {

            return "Normal";
        } else if (bmiValue < 30) {

            return "Overweight";
        } else {
            return "Obese";
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

