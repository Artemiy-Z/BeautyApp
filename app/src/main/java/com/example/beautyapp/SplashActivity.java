package com.example.beautyapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Half;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_meet_layout);

        findViewById(R.id.text).setAlpha(0);

        View mc = findViewById(R.id.main_container);
        ViewTreeObserver tbo = mc.getViewTreeObserver();
        tbo.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                //Do the animations

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        View logo = findViewById(R.id.logo);

                        DisplayMetrics metrics = getResources().getDisplayMetrics();
                        float scr_h = metrics.heightPixels;
                        float logo_h = logo.getMeasuredHeight();

                        float up_dp = ((scr_h - logo_h)/2)/metrics.density;

                        TranslateAnimation a =  new TranslateAnimation(0, 0, 0, -(up_dp - 80)*metrics.density);
                        a.setDuration(500);
                        a.setFillAfter(true);

                        logo.startAnimation(a);
                        findViewById(R.id.text).startAnimation(a);

                        AlphaAnimation al = new AlphaAnimation(0.0f, 1.0f);
                        al.setDuration(500);
                        al.setFillAfter(true);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.text).setAlpha(1);
                                findViewById(R.id.text).setVisibility(View.VISIBLE);
                                findViewById(R.id.text).startAnimation(al);

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        AlphaAnimation al2 = new AlphaAnimation(0.0f, 1.0f);
                                        al2.setDuration(500);
                                        al2.setFillAfter(true);

                                        findViewById(R.id.begin).setAlpha(1);
                                        findViewById(R.id.begin).setVisibility(View.VISIBLE);
                                        findViewById(R.id.begin).startAnimation(al2);

                                        findViewById(R.id.begin).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent i = new Intent(SplashActivity.this, SignInActivity.class);
                                                startActivity(i);
                                                finish();
                                            }
                                        });
                                    }
                                }, 500);
                            }
                        }, 500);
                    }
                }, 500);

                //end listener
                mc.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });


    }
}