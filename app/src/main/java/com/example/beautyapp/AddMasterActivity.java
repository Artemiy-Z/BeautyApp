package com.example.beautyapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public class AddMasterActivity extends AppCompatActivity implements PlanDialog.PlanListener {

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

    private EditText name;
    private ImageView avatar;
    private int PICK_IMAGE = 2034;
    public static final int STORAGE_REQUEST = 101;
    String storagePermission[];
    private ByteArrayOutputStream baos;
    private Spinner prof;
    private TextView plan_text;
    private ImageButton plan;

    private HashMap<String, Object> master = new HashMap<>();
    private ArrayList<String> portfolio = new ArrayList<>();
    private ArrayList<String> paths = new ArrayList<>();

    private String[] ProfRefferences = {
            "none",
            "haircut",
            "massage",
            "maniqure",
            "face",
            "spa",
            "epilation",
            "makiash",
            "paint"
    };

    private FirebaseGiver giver = new FirebaseGiver();

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AddMasterActivity.this, AdminActivity.class));
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_master);

        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        name = findViewById(R.id.name);
        prof = findViewById(R.id.prof);
        plan_text = findViewById(R.id.plan);
        plan = findViewById(R.id.pencil);

        String[] ProfVariants = {
                "Специализация...",
                "Парикмахер",
                "Массажист",
                "Маникюр",
                "Уход за лицом",
                "Спа",
                "Эпиляция",
                "Макияж",
                "Покраска волос"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, ProfVariants);
        prof.setAdapter(adapter);

        plan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlanDialog dialog = new PlanDialog();
                dialog.show(getSupportFragmentManager(), "Plan setting");
            }
        });

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name.getText().toString().equals(""))
                    return;
                else if (prof.getSelectedItemPosition() == 0)
                    return;
                else if (master.get("plan") == "" || master.get("plan") == "[]")
                    return;
                else if (baos == null)
                    return;

                showLoading();

                master.put("name", name.getText().toString());
                master.put("photos", "");

                FirebaseDatabase fd = giver.getDatabase();
                DatabaseReference dat = fd.getReference("/admin_content/masters/").child(ProfRefferences[prof.getSelectedItemPosition()]);
                DatabaseReference n = dat.push();
                n.setValue(master);

                if(portfolio.size() > 0)
                    loadF(0, n.getKey());
                else {
                    n.child("photos").setValue("").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            new FirebaseGiver().getStorage().getReference("masters/"+ProfRefferences[prof.getSelectedItemPosition()]).child(n.getKey()).child("avatar.png").putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    hideLoading();
                                    Toast.makeText(AddMasterActivity.this, "Загрузка завершена", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(AddMasterActivity.this, AdminActivity.class));
                                    finish();
                                }
                            });
                        }
                    });
                }
            }
        });

        findViewById(R.id.portfolio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(AddMasterActivity.this, EditPortfolioActivity.class).putExtra("portfolio", portfolio), 113);
            }
        });

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
    }

    public void loadF(int index, String k) {

        FirebaseStorage fs = giver.getStorage();
        StorageReference ref = fs.getReference("/masters/").child(ProfRefferences[prof.getSelectedItemPosition()]).child(k);

        if(index == portfolio.size()) {
            FirebaseDatabase fd = giver.getDatabase();
            DatabaseReference dat = fd.getReference("/admin_content/masters/").child(ProfRefferences[prof.getSelectedItemPosition()]);
            DatabaseReference n = dat.child(k);
            n.child("photos").setValue(paths).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    ref.child("avatar.png").putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            hideLoading();
                            Toast.makeText(AddMasterActivity.this, "Загрузка завершена", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AddMasterActivity.this, AdminActivity.class));
                            finish();
                        }
                    });
                }
            });
            return;
        }

        ref.child("image"+String.valueOf(index)+".png").putFile(Uri.fromFile(new File(getApplicationContext().getCacheDir(), portfolio.get(index))))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        paths.add(taskSnapshot.getStorage().getPath());
                        loadF(index+1, k);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("ERROR", e.getMessage());
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 113) {
            portfolio = new ArrayList<>();
            String[] paths = data.getStringExtra("portfolio").replace("[", "").replace("]", "").replace(" ", "").split(",");
            for (String path : paths) {
                portfolio.add(path);
            }
        }
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
                avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);

                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            }
        }
    }

    @Override
    public void sendData(ArrayList<Integer> checked_days) {
        master.put("plan", checked_days);
        String s = "";
        for (int k : checked_days) {
            s += new String[] { "Пн ",  "Вт ",  "Ср ",  "Чт ",  "Пт ",  "Сб ",  "Вс " }[k];
        }
        plan_text.setText("Расписание: " + s);
        Log.println(Log.ASSERT, "Days work", checked_days.toString());
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
}