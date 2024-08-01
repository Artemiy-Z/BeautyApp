package com.example.beautyapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.widget.Toast;

public class AccountEditActivity extends AppCompatActivity {

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

    private ImageView avatar;
    private int PICK_IMAGE = 2034;

    public static final int STORAGE_REQUEST = 101;
    String storagePermission[];

    private ByteArrayOutputStream baos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_edit);

        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        FirebaseAuth auth = FirebaseAuth.getInstance();

        FirebaseStorage fs = FirebaseStorage.getInstance("gs://beauty-d4ba8.appspot.com/");
        StorageReference ref = fs.getReference("/users/" + auth.getCurrentUser().getUid());

        showLoading();
        ref.listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
            @Override
            public void onComplete(@NonNull Task<ListResult> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getItems().size() >= 1) {
                        task.getResult().getItems().get(0).getBytes(1000000).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                            @Override
                            public void onComplete(@NonNull Task<byte[]> task) {
                                if (task.isSuccessful()) {
                                    hideLoading();
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

        SharedPreferences sp = getSharedPreferences("profile", MODE_PRIVATE);

        ((EditText) findViewById(R.id.name)).setText(sp.getString("name", "missing"));

        avatar = findViewById(R.id.avatar_image);

        findViewById(R.id.loadimage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!checkStoragePermission()) {
                    requestStoragePermission();
                } else {
                    pickFromGallery();
                }
            }
        });

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sp.edit().putString("name", ((EditText) findViewById(R.id.name)).getText().toString()).apply();
                FirebaseDatabase fd = FirebaseDatabase.getInstance("https://beauty-d4ba8-default-rtdb.europe-west1.firebasedatabase.app");
                DatabaseReference fdref = fd.getReference("/users/" + auth.getCurrentUser().getUid()).child("name");
                fdref.setValue(((EditText) findViewById(R.id.name)).getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if (baos == null) {
                            startActivity(new Intent(AccountEditActivity.this, AccountActivity.class));
                            finish();
                            return;
                        }
                        ref.listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
                            @Override
                            public void onComplete(@NonNull Task<ListResult> task) {
                                if (task.isSuccessful()) {
                                    ref.child("avatar.png").putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            Toast.makeText(AccountEditActivity.this, "Аватар загружен успешно", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(AccountEditActivity.this, AccountActivity.class));
                                            finish();
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
            }
        });

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AccountEditActivity.this, AccountActivity.class));
                finish();
            }
        });

        findViewById(R.id.change_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AccountEditActivity.this, PasswordChangeActivity.class));
            }
        });
    }

    private void pickFromGallery() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission() {
        requestPermissions(storagePermission, STORAGE_REQUEST);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_REQUEST: {
                if (grantResults.length > 0) {
                    boolean storage_accepted = grantResults[0] == (PackageManager.PERMISSION_GRANTED);
                    if (storage_accepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            if (data == null) {
                return;
            }

            InputStream inputStream;

            try {
                inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                avatar.setImageBitmap(bitmap);

                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 20, baos);
            }
        }
    }
}