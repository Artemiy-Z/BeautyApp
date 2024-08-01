package com.example.beautyapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MastersViewActivity extends AppCompatActivity {

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

    private String kategory;
    private GridAdapter2 adapter;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_masters_view);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        kategory = getIntent().getStringExtra("kategory");

        LinearLayout list = findViewById(R.id.linear);

        myGridView grid = new myGridView(getApplicationContext());
        grid.setNumColumns(2);
        grid.setVerticalSpacing((int) (16 * getResources().getDisplayMetrics().density));
        grid.setHorizontalSpacing((int)(16 * getResources().getDisplayMetrics().density));
        adapter = new GridAdapter2("masters/"+kategory+"/");
        grid.setAdapter(adapter);

        list.addView(grid);

        showLoading();

        new FirebaseGiver().getDatabase().getReference("admin_content/masters").child(kategory).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> ks = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren())
                    ks.add(s.getKey());
                load(adapter, new FirebaseGiver().getDatabase().getReference("admin_content/masters").child(kategory),
                        new FirebaseGiver().getStorage().getReference("masters").child(kategory), 0, ks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void view(String path, String id, int position) {
        Intent intent = new Intent(MastersViewActivity.this, UserPortfolioActivity.class);
        intent.putExtra("path", path+id);
        intent.putExtra("id", id);
        intent.putExtra("position", position);
        startActivityForResult(intent, 204);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == 204) {
            Intent intent = new Intent();
            intent.putExtra("id", data.getStringExtra("id"));
            intent.putExtra("name", adapter.masters.get(data.getIntExtra("position", 0)).name);
            intent.putExtra("days", adapter.masters.get(data.getIntExtra("position", 0)).days);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void load(GridAdapter2 adapter, DatabaseReference ref, StorageReference sref, int index, ArrayList<String> keys) {
        if (index == keys.size()) {
            hideLoading();
            return;
        }

        MasterItem item = new MasterItem();
        sref.child(keys.get(index)).child("avatar.png")
                .getBytes(2000000).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        item.image = bitmap;
                        ref.child(keys.get(index)).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                item.name = snapshot.getValue(String.class);
                                ref.child(keys.get(index)).child("plan").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        try {
                                            ArrayList<Long> plan = (ArrayList<Long>) snapshot.getValue();
                                            String s = "";
                                            item.days = new int[plan.size()];
                                            int i = 0;
                                            for (long k : plan) {
                                                s += new String[]{"Пн ", "Вт ", "Ср ", "Чт ", "Пт ", "Сб ", "Вс "}[(int) k];
                                                item.days[i] = (int) k;
                                                i++;
                                            }
                                            item.plan = s;
                                            item.id = keys.get(index);
                                            adapter.addItem(item);
                                        } catch (Exception e) {
                                            String[] plan = snapshot.getValue(String.class).replace("[", "")
                                                    .replace("]", "").replace(" ", "").split(",");
                                            String s = "";
                                            for (String k : plan) {
                                                s += new String[]{"Пн ", "Вт ", "Ср ", "Чт ", "Пт ", "Сб ", "Вс "}[Integer.parseInt(k)];
                                            }
                                            item.plan = s;
                                            item.id = keys.get(index);
                                            adapter.addItem(item);
                                        }

                                        load(adapter, ref, sref, index + 1, keys);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        hideLoading();
                                        Toast.makeText(MastersViewActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                hideLoading();
                                Toast.makeText(MastersViewActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideLoading();
                        Toast.makeText(MastersViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private View createView(MasterItem item) {
        View view = getLayoutInflater().inflate(R.layout.master_card2, null, false);
        ((ImageView) view.findViewById(R.id.image)).setImageBitmap(item.image);
        ((TextView) view.findViewById(R.id.name)).setText(item.name);
        ((TextView) view.findViewById(R.id.plan)).setText(item.plan);

        return view;
    }

    private class MasterItem {
        public String name;
        public String plan;
        public Bitmap image;
        public String id;
        public int[] days;
    }

    private class GridAdapter2 extends BaseAdapter {

        private ArrayList<MasterItem> masters = new ArrayList<>();
        private String listRef;

        public GridAdapter2(String _listRef) {
            listRef = _listRef;
        }

        public void addItem(MasterItem i) {
            masters.add(i);
            GridAdapter2.this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return masters.size();
        }

        @Override
        public MasterItem getItem(int i) {
            return masters.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View card = createView(masters.get(i));

            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    view(listRef, masters.get(i).id, i);
                }
            });

            return card;
        }
    }
}