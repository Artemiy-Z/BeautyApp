package com.example.beautyapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class UserPortfolioActivity extends AppCompatActivity {

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

    private GridAdapter adapter;
    private GridView grid;

    private int PICK_IMAGE = 2034;

    public static final int STORAGE_REQUEST = 101;
    String storagePermission[];
    private long startTime;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        intent.putExtra("path", getIntent().getStringExtra("path"));
        setResult(RESULT_CANCELED, intent);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_portfolio);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        startTime = System.nanoTime();

        String path = getIntent().getStringExtra("path");

        Log.e("INTENT", "Is null - " + String.valueOf(getIntent() == null));

        FirebaseGiver giver = new FirebaseGiver();

        grid = new myGridView(getApplicationContext());
        ((ViewGroup) findViewById(R.id.scroll)).addView(grid);
        adapter = new GridAdapter();
        grid.setVerticalSpacing((int) (16 * getResources().getDisplayMetrics().density));
        grid.setNumColumns(2);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PhotoViewer pv = new PhotoViewer(((GridAdapter) adapterView.getAdapter()).getItem(i), i);
                pv.show(getSupportFragmentManager(), "PhotoView");
            }
        });

        showLoading();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                StorageReference reference = new FirebaseGiver().getStorage().getReference(path);
                reference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        ArrayList<String> keys = new ArrayList<>();
                        for(StorageReference st : listResult.getItems()) {
                            if(!st.getName().contains("avatar"))
                                keys.add(st.getName());
                        }
                        if(keys.size() > 0)
                            loadImg(0, reference, keys);
                        else {
                            hideLoading();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UserPortfolioActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                });
            }
        }, 2000);
        grid.setAdapter(adapter);

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("id", getIntent().getStringExtra("id"));
                intent.putExtra("path", getIntent().getStringExtra("path"));
                intent.putExtra("position", getIntent().getIntExtra("position", 0));
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void loadImg(int index, StorageReference sref, ArrayList<String> keys) {
        if(keys.size() == index) {
            hideLoading();
            return;
        }

        sref.child(keys.get(index)).getBytes(1000000).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                adapter.addItem(bitmap);
                loadImg(index+1, sref, keys);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideLoading();
                Toast.makeText(UserPortfolioActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private View createImage(Bitmap source) {
        ShapeableImageView image = new ShapeableImageView(UserPortfolioActivity.this);

        image.setShapeAppearanceModel(ShapeAppearanceModel.builder(getApplicationContext(), R.style.ShapeBox, R.style.ShapeBox).build());
        if (source != null)
            image.setImageBitmap(source);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                (int) (150 * getResources().getDisplayMetrics().density),
                (int) (150 * getResources().getDisplayMetrics().density)
        );

        image.setLayoutParams(params);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);

        return image;
    }

    private class GridAdapter extends BaseAdapter {

        private ArrayList<Bitmap> sources = new ArrayList<>();

        public void addItem(Bitmap bitmap) {
            sources.add(bitmap);
            GridAdapter.this.notifyDataSetChanged();
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            grid.setLayoutParams(new FrameLayout.LayoutParams(grid.getWidth(), (int) (150 * getResources().getDisplayMetrics().density * (getCount() * 0.5 + 1))));
        }

        @Override
        public int getCount() {
            return sources.size();
        }

        @Override
        public Bitmap getItem(int i) {
            return sources.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null) {
                view = createImage(null);
            }

            ((ShapeableImageView)view).setImageBitmap(sources.get(i));

            return view;
        }
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
                    if(!storage_accepted) {
                        Toast.makeText(this, "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }
}