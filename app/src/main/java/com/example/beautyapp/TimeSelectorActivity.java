package com.example.beautyapp;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.widget.AppCompatButton;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class TimeSelectorActivity extends AppCompatActivity {

    private View loading;

    private void showLoading() {
        if(loading == null) {
            loading = getLayoutInflater().inflate(R.layout.loading, null, false);
            loading.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            ((ViewGroup) findViewById(R.id.main_cont)).addView(loading);
        }
        loading.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        if(loading != null) {
            loading.setVisibility(View.INVISIBLE);
        }
    }

    private int selected = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_selector);

        ArrayList<AppCompatButton> bs = new ArrayList<>();

        bs.add(findViewById(R.id.button_1));
        bs.add(findViewById(R.id.button_2));
        bs.add(findViewById(R.id.button_3));
        bs.add(findViewById(R.id.button_4));
        bs.add(findViewById(R.id.button_5));
        bs.add(findViewById(R.id.button_6));
        bs.add(findViewById(R.id.button_7));
        bs.add(findViewById(R.id.button_8));
        bs.add(findViewById(R.id.button_9));
        bs.add(findViewById(R.id.button_10));
        bs.add(findViewById(R.id.button_11));
        bs.add(findViewById(R.id.button_12));

        for(AppCompatButton b : bs) {
            b.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void onClick(View view) {
                    b.setBackgroundDrawable(getDrawable(R.drawable.box6_pink));
                    b.setTextColor(getResources().getColor(R.color.white, getTheme()));
                    selected = bs.indexOf(b);
                    for(AppCompatButton bo : bs) {
                        if(bs.indexOf(bo) != bs.indexOf(b)) {
                            bo.setBackgroundDrawable(getDrawable(R.drawable.box6));
                            bo.setTextColor(getResources().getColor(R.color.black, getTheme()));
                        }
                    }

                    findViewById(R.id.create_order).setAlpha(1f);

                    findViewById(R.id.create_order).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            showLoading();

                            FirebaseDatabase fd = new FirebaseGiver().getDatabase();
                            FirebaseAuth auth = new FirebaseGiver().getAuth();
                            DatabaseReference ref = fd.getReference("users/"+auth.getCurrentUser().getUid()).child("orders");
                            HashMap<String, String> order = new HashMap<>();
                            order.put("time", b.getText().toString());
                            order.put("kategory", getIntent().getStringExtra("kategory"));
                            order.put("difficulty", getIntent().getStringExtra("difficulity"));
                            order.put("master_id", getIntent().getStringExtra("masterID"));
                            order.put("master_name", getIntent().getStringExtra("master"));
                            order.put("date", getIntent().getStringExtra("date"));
                            ref.push().setValue(order).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(TimeSelectorActivity.this, "Заказ успешно оформлен", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(TimeSelectorActivity.this, CatalogActivity.class));
                                    hideLoading();
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(TimeSelectorActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                    hideLoading();
                                    finish();
                                }
                            });
                        }
                    });
                }
            });
        }
    }
}