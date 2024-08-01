package com.example.beautyapp;

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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class EditMasterActivity extends AppCompatActivity implements PlanDialog.PlanListener {

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
            "spa",
            "face",
            "maniqure",
            "makiash",
            "epilation",
            "paint"
    };

    private FirebaseGiver giver = new FirebaseGiver();
    private long startTime;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(EditMasterActivity.this, AdminMastersViewActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_master);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        startTime = System.nanoTime();

        String path = getIntent().getStringExtra("path");

        name = findViewById(R.id.name);
        prof = findViewById(R.id.prof);
        plan_text = findViewById(R.id.plan);
        plan = findViewById(R.id.pencil);

        String[] ProfVariants = {
                "Специализация...",
                "Парикмахер",
                "Массажист",
                "Спа",
                "Уход за лицом",
                "Маникюр",
                "Макияж",
                "Эпиляция",
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

        showLoading();

        DatabaseReference ref = new FirebaseGiver().getDatabase().getReference(path);
        ref.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name.setText(snapshot.getValue(String.class));
                master.put("name", snapshot.getValue(String.class));
                ref.child("plan").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        try {
                            ArrayList<Long> pl = (ArrayList<Long>)snapshot.getValue();
                            String s = "";
                            for (long k : pl) {
                                s += new String[] { "Пн ",  "Вт ",  "Ср ",  "Чт ",  "Пт ",  "Сб ",  "Вс " }[(int) k];
                            }
                            plan_text.setText(s);
                            master.put("plan", pl);
                        }
                        catch (Exception e) {
                            String[] pl = snapshot.getValue(String.class).replace("[", "")
                                    .replace("]", "").replace(" ", "").split(",");
                            String s = "";
                            for (String k : pl) {
                                s += new String[] { "Пн ",  "Вт ",  "Ср ",  "Чт ",  "Пт ",  "Сб ",  "Вс " }[Integer.parseInt(k)];
                            }
                            plan_text.setText(s);
                            master.put("plan", pl);
                        }

                        String[] as = path.split("/");
                        Log.e("PATH", "masters/" + as[as.length-2] + as[as.length-1] + "/avatar.png");

                        new FirebaseGiver().getStorage().getReference("masters/" + as[as.length-2] + "/" + as[as.length-1] + "/avatar.png")
                                .getBytes(2000000).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        avatar.setImageBitmap(bitmap);

                                        new FirebaseGiver().getStorage().getReference("masters/" + as[as.length-2] + "/" + as[as.length-1]).listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                            @Override
                                            public void onSuccess(ListResult listResult) {

                                                ArrayList<String> keys = new ArrayList<>();
                                                for (StorageReference sr : listResult.getItems()) {
                                                    if(sr.getName().contains("image"))
                                                        keys.add(sr.getName());
                                                }
                                                downloadImg(new FirebaseGiver().getStorage().getReference("masters/" + as[as.length-2] + "/" + as[as.length-1]),
                                                        keys, 0);
                                            }
                                        });
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
                else if(portfolio.size() == 0)
                    return;

                showLoading();

                master.put("name", name.getText().toString());
                master.put("photos", "");

                FirebaseDatabase fd = giver.getDatabase();
                DatabaseReference dat = fd.getReference(getIntent().getStringExtra("path"));
                dat.setValue(master);

                loadF(0, dat.getKey());
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

    public void downloadImg(StorageReference main, ArrayList<String> keys, int index) {
        if(index == keys.size()) {
            findViewById(R.id.portfolio).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(EditMasterActivity.this, EditPortfolioActivity.class);
                    i.putExtra("portfolio", portfolio);
                    String[] as = getIntent().getStringExtra("path").split("/");
                    i.putExtra("path", "masters/" + as[as.length-2] + "/" + as[as.length-1]);
                    startActivityForResult(i, 113);
                }
            });
            prof.setSelection(getIntent().getIntExtra("type", 0)+1);
            hideLoading();
            return;
        }

        main.child(keys.get(index)).getBytes(1000000).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                File localCopy = new File(getApplicationContext().getCacheDir(), "image_"+String.valueOf(new Random(System.nanoTime()-startTime).nextInt()));
                try {
                    FileOutputStream output = new FileOutputStream(localCopy);
                    output.write(bytes);
                    portfolio.add(localCopy.getName());
                    downloadImg(main, keys, index+1);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
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

                    if(baos == null) {
                        hideLoading();
                        Toast.makeText(EditMasterActivity.this, "Загрузка завершена", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EditMasterActivity.this, AdminActivity.class));
                        finish();
                        return;
                    }
                    ref.child("avatar.png").putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            hideLoading();
                            Toast.makeText(EditMasterActivity.this, "Загрузка завершена", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(EditMasterActivity.this, AdminActivity.class));
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