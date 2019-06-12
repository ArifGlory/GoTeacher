package com.goteacher;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;

import com.goteacher.main.MainActivity;


public class SplashScreens extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        setTimer(); // set timer 2.5 detik
    }

    private void setTimer() {
        new CountDownTimer(2500, 500) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() { // setelah timer selesai
                startActivity(new Intent(SplashScreens.this, MainActivity.class));
                finish();
            }
        }.start();
    }
}
