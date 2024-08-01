package com.example.beautyapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.icu.util.Output;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class EditPortfolioActivity extends AppCompatActivity implements PhotoViewer.PhotoListener {

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
    private ArrayList<String> portfolio = new ArrayList<>();
    private long startTime;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(EditPortfolioActivity.this, EditMasterActivity.class));
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_portfolio);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        startTime = System.nanoTime();

        ArrayList<String> loaded = new ArrayList<>();
        loaded = getIntent().getStringArrayListExtra("portfolio");

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
                if(i == 0) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                    return;
                }
                PhotoViewer pv = new PhotoViewer(((GridAdapter) adapterView.getAdapter()).getItem(i-1), i);
                pv.show(getSupportFragmentManager(), "PhotoView");
            }
        });

        ArrayList<String> finalLoaded = loaded;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (finalLoaded != null)
                    if (finalLoaded.size() != 0) {
                        showLoading();
                        for (String s : finalLoaded) {
                            Uri fp = Uri.fromFile(new File(getApplicationContext().getCacheDir(), s));
                            try {
                                FileInputStream stream = new FileInputStream(new File(fp.getPath()));
                                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                                adapter.addItem(bitmap);
                                portfolio.add(s);
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        hideLoading();
                    }
            }
        }, 2000);
        grid.setAdapter(adapter);

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_OK, new Intent().putExtra("portfolio", portfolio.toString()));
                finish();
            }
        });
    }

    @Override
    public void DeleteImage(int position) {
        showLoading();
        adapter.sources.remove(position - 1);
        adapter.notifyDataSetChanged();
        new FirebaseGiver().getStorage().getReference(getIntent().getStringExtra("path")).child("image_"+position).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                hideLoading();
                Toast.makeText(EditPortfolioActivity.this, "Удалено", Toast.LENGTH_SHORT).show();
            }
        });
        portfolio.remove(position);
    }

    private View createImage(Bitmap source) {
        ShapeableImageView image = new ShapeableImageView(EditPortfolioActivity.this);

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
        private View addButton;

        public GridAdapter() {
            addButton = createImage(null);
            ((ShapeableImageView) addButton).setImageResource(R.drawable.add_image);

            addButton.setOnClickListener(new View.OnClickListener() {
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

        public void addItem(Bitmap bitmap) {
            sources.add(bitmap);
            GridAdapter.this.notifyDataSetChanged();
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            grid.setLayoutParams(new FrameLayout.LayoutParams(grid.getWidth(), (int) (addButton.getHeight() * (getCount() * 0.5 + 1))));
        }

        @Override
        public int getCount() {
            return sources.size() + 1;
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
            if(i>0)  {
                ((ShapeableImageView)view).setImageBitmap(sources.get(i-1));
            }
            else {
                ((ShapeableImageView)view).setImageResource(R.drawable.add_image);
            }

            return view;
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

                File localCopy = new File(getApplicationContext().getCacheDir(), "image_" + String.valueOf(new Random(System.nanoTime()-startTime).nextInt()));

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                adapter.addItem(bitmap);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);

                try {
                    FileOutputStream output = new FileOutputStream(localCopy);
                    output.write(baos.toByteArray());
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                portfolio.add(localCopy.getName());
            }
        }
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