package com.example.beautyapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SignUpActivity extends AppCompatActivity {

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
    private EditText name;
    private EditText password;
    private EditText password_rep;
    private ArrayList<EditText> array;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        findViewById(R.id.variant).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                overridePendingTransition(R.anim.left_enter, R.anim.left_exit);
                finish();
            }
        });

        phone = findViewById(R.id.phone);
        name = findViewById(R.id.name);
        password = findViewById(R.id.password);
        password_rep = findViewById(R.id.password_rep);

        array = new ArrayList<>();

        array.add(name);
        array.add(phone);
        array.add(password);
        array.add(password_rep);

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int i = -1;
                int j = -1;

                if (name.getText().toString().equals(""))
                    i = 0;
                else if (phone.getText().toString().equals(""))
                    i = 1;
                else if (password.getText().toString().equals(""))
                    i = 2;
                else if(password.getText().toString().length() < 6) {
                    i = 2;
                    Toast.makeText(getApplicationContext(), "Пароль должен быть не менее 6 символов в длину", Toast.LENGTH_SHORT).show();
                }
                else if (password_rep.getText().toString().equals(""))
                    i = 3;
                else if(!password.getText().toString().equals(password_rep.getText().toString())) {
                    i = 2;
                    j = 3;
                    Toast.makeText(getApplicationContext(), "Пароли не совпадают!", Toast.LENGTH_SHORT).show();
                }

                if (i != -1) {
                    int colorFrom = getColor(R.color.pink3_tr);
                    int colorTo = getColor(R.color.white);
                    ValueAnimator a = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                    a.setDuration(1000);
                    int finalI = i;
                    int finalI2 = j;
                    a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                            array.get(finalI).setHintTextColor((int) valueAnimator.getAnimatedValue());
                            if(finalI2 != -1)
                                array.get(finalI2).setHintTextColor((int) valueAnimator.getAnimatedValue());
                        }
                    });
                    a.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(@NonNull Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(@NonNull Animator animator) {
                            array.get(finalI).setHintTextColor(colorTo);
                            if(finalI2 != -1)
                                array.get(finalI2).setHintTextColor(colorTo);
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
                //Получаю ссылку на объект FB, позволяющий взаимодействовать с сервером
                FirebaseAuth auth = FirebaseAuth.getInstance();
                //Отображение процесса загрузки
                showLoading();
                //Регистрация пользователя в системе
                auth.createUserWithEmailAndPassword(phone.getText().toString() + "@beauty.usr", password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            hideLoading();
                            Toast.makeText(getApplicationContext(), "Регистрация успешна", Toast.LENGTH_SHORT).show();
                            //Создание записи данных в базу данных
                            FirebaseDatabase fd = FirebaseDatabase.getInstance("https://beauty-d4ba8-default-rtdb.europe-west1.firebasedatabase.app");
                            DatabaseReference ref = fd.getReference("/users");
                            ref = ref.child(auth.getCurrentUser().getUid());
                            HashMap<String, String> usrData = new HashMap<String, String>();
                            usrData.put("name", name.getText().toString());
                            usrData.put("orders", "");
                            ref.setValue(usrData);
                            SharedPreferences sp = getSharedPreferences("profile", 0);
                            sp.edit().putString("name", name.getText().toString()).apply();
                            //Переход на главную страницу
                            startActivity(new Intent(SignUpActivity.this, CatalogActivity.class));
                            overridePendingTransition(R.anim.page_change_to_up_enter, R.anim.page_change_to_up_exit);
                            finish();
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