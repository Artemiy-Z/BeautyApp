package com.example.beautyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

public class AccountActivity extends AppCompatActivity {

    private final FirebaseGiver giver = new FirebaseGiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        findViewById(R.id.catalog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AccountActivity.this, CatalogActivity.class));
                overridePendingTransition(R.anim.alpha_enter, R.anim.alpha_exit);
                finish();
            }
        });

        findViewById(R.id.orders).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AccountActivity.this, OrdersActivity.class));
                finish();
            }
        });

        FirebaseAuth auth = giver.getAuth();

        SharedPreferences sp = getSharedPreferences("profile", MODE_PRIVATE);

        ((TextView) findViewById(R.id.name)).setText(sp.getString("name", "missing"));

        FirebaseStorage fs = giver.getStorage();
        StorageReference ref = fs.getReference("/users/" + auth.getCurrentUser().getUid());

        ref.listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
            @Override
            public void onComplete(@NonNull Task<ListResult> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getItems().size() >= 1) {
                        task.getResult().getItems().get(0).getBytes(1000000).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                            @Override
                            public void onComplete(@NonNull Task<byte[]> task) {
                                if (task.isSuccessful()) {
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(task.getResult(), 0, task.getResult().length);
                                    ((ImageView) findViewById(R.id.avatar_image)).setImageBitmap(bitmap);
                                    ((ImageView) findViewById(R.id.avatar_image)).setScaleType(ImageView.ScaleType.CENTER_CROP);
                                }
                            }
                        });
                    }
                }
            }
        });

        FirebaseDatabase fd = giver.getDatabase();
        DatabaseReference datref = fd.getReference().child("admin_content/masters/signin");
        datref.setValue("signed").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                findViewById(R.id.admin).setVisibility(View.VISIBLE);
                findViewById(R.id.admin).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(AccountActivity.this, AdminActivity.class));
                        finish();
                    }
                });
            }
        });

        findViewById(R.id.edit_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AccountActivity.this, AccountEditActivity.class));
                finish();
            }
        });
    }
}