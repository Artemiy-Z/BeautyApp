package com.example.beautyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.dynamicanimation.animation.DynamicAnimation;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SignInActivity extends AppCompatActivity {

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

    private EditText phone;
    private EditText password;
    private ArrayList<EditText> array;
    private FirebaseGiver giver = new FirebaseGiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        findViewById(R.id.variant).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
                overridePendingTransition(R.anim.right_enter, R.anim.right_exit);
                finish();
            }
        });

        phone = findViewById(R.id.phone);
        password = findViewById(R.id.password);

        array = new ArrayList<>();

        array.add(phone);
        array.add(password);

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int i = -1;

                if(phone.getText().toString().equals(""))
                    i = 0;
                else if(password.getText().toString().equals(""))
                    i = 1;

                if(i != -1) {
                    int colorFrom = getColor(R.color.pink3_tr);
                    int colorTo = getColor(R.color.white);
                    ValueAnimator a = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                    a.setDuration(1000);
                    int finalI = i;
                    a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                            array.get(finalI).setHintTextColor((int) valueAnimator.getAnimatedValue());
                        }
                    });
                    a.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(@NonNull Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(@NonNull Animator animator) {
                            array.get(finalI).setHintTextColor(colorTo);
                        }

                        @Override
                        public void onAnimationCancel(@NonNull Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(@NonNull Animator animator) {

                        }
                    });
                    a.start();

                    return;
                }

                FirebaseAuth auth = giver.getAuth();

                showLoading();

                auth.signInWithEmailAndPassword(phone.getText().toString() + "@beauty.usr", password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {

                            FirebaseDatabase fd = giver.getDatabase();

                            DatabaseReference datref = fd.getReference().child("admin_content/masters/signin");
                            datref.setValue("signed").addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getApplicationContext(), "Вход выполнен, admin", Toast.LENGTH_SHORT).show();
                                }
                            });
                            DatabaseReference ref = fd.getReference("users/"+auth.getCurrentUser().getUid());
                            ref.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Toast.makeText(getApplicationContext(), "Здравствуйте, "+snapshot.getValue(String.class), Toast.LENGTH_SHORT).show();
                                    SharedPreferences sp = getSharedPreferences("profile", 0);
                                    hideLoading();
                                    sp.edit().putString("name", snapshot.getValue(String.class)).apply();
                                    startActivity(new Intent(SignInActivity.this, CatalogActivity.class));
                                    overridePendingTransition(R.anim.page_change_to_up_enter, R.anim.page_change_to_up_exit);
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideLoading();
                        Toast.makeText(getApplicationContext(), "error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}