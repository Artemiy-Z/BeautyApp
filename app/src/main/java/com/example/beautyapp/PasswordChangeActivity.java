package com.example.beautyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PasswordChangeActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((EditText)findViewById(R.id.last_pass)).getText().toString().equals(""))
                    Toast.makeText(getApplicationContext(), "Введите старый пароль", Toast.LENGTH_SHORT).show();
                else if (((EditText)findViewById(R.id.new_pass)).getText().toString().equals(""))
                    Toast.makeText(getApplicationContext(), "Введите новый пароль", Toast.LENGTH_SHORT).show();
                else if(((EditText)findViewById(R.id.new_pass)).getText().toString().length() < 6) {
                    Toast.makeText(getApplicationContext(), "Пароль должен быть не менее 6 символов в длину", Toast.LENGTH_SHORT).show();
                }
                else if (((EditText)findViewById(R.id.new_pass_rep)).getText().toString().equals(""))
                    Toast.makeText(getApplicationContext(), "Повторите новый пароль", Toast.LENGTH_SHORT).show();
                else if(!((EditText)findViewById(R.id.new_pass)).getText().toString().equals(((EditText)findViewById(R.id.new_pass_rep)).getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Пароли не совпадают!", Toast.LENGTH_SHORT).show();
                }
                else {
                    showLoading();

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    AuthCredential credential = EmailAuthProvider
                            .getCredential(user.getEmail(), ((EditText)findViewById(R.id.last_pass)).getText().toString());

                    user.reauthenticate(credential)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    user.updatePassword(((EditText)findViewById(R.id.new_pass)).getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(PasswordChangeActivity.this, "Пароль изменен успешно", Toast.LENGTH_SHORT).show();
                                            finish();
                                            hideLoading();
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(PasswordChangeActivity.this, "Неверный старый пароль!", Toast.LENGTH_SHORT).show();
                                    hideLoading();
                                }
                            });
                }
            }
        });
    }
}