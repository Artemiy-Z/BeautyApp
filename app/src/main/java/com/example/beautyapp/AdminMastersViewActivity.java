package com.example.beautyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class AdminMastersViewActivity extends AppCompatActivity {

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

    private ArrayList<LinearLayout> lists = new ArrayList<>();
    private ArrayList<myGridView> grids = new ArrayList<>();
    private ArrayList<GridAdapter> adapters = new ArrayList<>();
    private String[] ProfRefferences = {
            "haircut",
            "massage",
            "spa",
            "face",
            "maniqure",
            "makiash",
            "epilation",
            "paint"
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AdminMastersViewActivity.this, AdminActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_masters_view);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        lists.add(findViewById(R.id.linear_haircut));
        lists.add(findViewById(R.id.linear_massage));
        lists.add(findViewById(R.id.linear_spa));
        lists.add(findViewById(R.id.linear_face));
        lists.add(findViewById(R.id.linear_maniqure));
        lists.add(findViewById(R.id.linear_makiash));
        lists.add(findViewById(R.id.linear_epilation));
        lists.add(findViewById(R.id.linear_paint));

        for (int i = 0; i < lists.size(); i++) {
            LinearLayout list = lists.get(i);

            myGridView grid = new myGridView(getApplicationContext());
            grid.setNumColumns(2);
            grid.setVerticalSpacing((int)(16 * getResources().getDisplayMetrics().density));
            grid.setHorizontalSpacing((int)(16 * getResources().getDisplayMetrics().density));
            GridAdapter adapter = new GridAdapter("admin_content/masters/"+ProfRefferences[i]+"/", i);
            grid.setAdapter(adapter);
            grids.add(grid);
            adapters.add(adapter);

            list.addView(grid);
        }
        showLoading();
        new FirebaseGiver().getDatabase().getReference("admin_content/masters/haircut").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> ks = new ArrayList<>();
                for(DataSnapshot s : snapshot.getChildren())
                    ks.add(s.getKey());
                load(adapters, new FirebaseGiver().getDatabase().getReference("admin_content/masters"), 0, 0, ks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void startEdit(String path, int type) {
        Intent intent = new Intent(AdminMastersViewActivity.this, EditMasterActivity.class);
        intent.putExtra("path", path);
        intent.putExtra("type", type);
        startActivity(intent);
        finish();
    }

    private void load(ArrayList<GridAdapter> adapters, DatabaseReference ref, int index, int type, ArrayList<String> keys) {
        showLoading();
        if(index == keys.size()) {
            if(type + 1 == adapters.size()) {
                hideLoading();
                return;
            }

            ref.child(ProfRefferences[type+1]).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<String> ks = new ArrayList<>();
                    for(DataSnapshot s : snapshot.getChildren())
                        ks.add(s.getKey());
                    load(adapters, ref, 0, type+1, ks);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                    error.toException().printStackTrace();
                }
            });
            return;
        }

        MasterItem item = new MasterItem();
        new FirebaseGiver().getStorage().getReference("masters/" + ProfRefferences[type] + "/" + keys.get(index) + "/avatar.png")
                .getBytes(2000000).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        item.image = bitmap;

                        ref.child( ProfRefferences[type] + "/" + keys.get(index)).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.getValue(String.class).equals("")) {
                                    load(adapters, ref, index+1, type, keys);
                                }
                                item.name = snapshot.getValue(String.class);
                                ref.child(ProfRefferences[type] + "/" + keys.get(index)).child("plan").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        try {
                                            ArrayList<Long> plan = (ArrayList<Long>)snapshot.getValue();
                                            String s = "";
                                            for (long k : plan) {
                                                s += new String[] { "Пн ",  "Вт ",  "Ср ",  "Чт ",  "Пт ",  "Сб ",  "Вс " }[(int) k];
                                            }
                                            item.plan = s;
                                            item.id = keys.get(index);
                                            adapters.get(type).addItem(item);
                                        }
                                        catch (Exception e) {
                                            String[] plan = snapshot.getValue(String.class).replace("[", "")
                                                    .replace("]", "").replace(" ", "").split(",");
                                            String s = "";
                                            for (String k : plan) {
                                                s += new String[] { "Пн ",  "Вт ",  "Ср ",  "Чт ",  "Пт ",  "Сб ",  "Вс " }[Integer.parseInt(k)];
                                            }
                                            item.plan = s;
                                            item.id = keys.get(index);
                                            adapters.get(type).addItem(item);
                                        }

                                        load(adapters, ref, index+1, type, keys);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(AdminMastersViewActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                        hideLoading();
                                        error.toException().printStackTrace();
                                        load(adapters, ref, index+1, type, keys);
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(AdminMastersViewActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                hideLoading();
                                error.toException().printStackTrace();
                                load(adapters, ref, index+1, type, keys);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AdminMastersViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        hideLoading();
                        e.printStackTrace();
                        load(adapters, ref, index+1, type, keys);
                    }
                });
    }

    private View createView(MasterItem item) {
        View view = getLayoutInflater().inflate(R.layout.master_card, null, false);
        ((ImageView)view.findViewById(R.id.image)).setImageBitmap(item.image);
        ((TextView)view.findViewById(R.id.name)).setText(item.name);
        ((TextView)view.findViewById(R.id.plan)).setText(item.plan);

        return view;
    }

    private class MasterItem {
        public String name;
        public String plan;
        public Bitmap image;
        public String id;
    }

    private class GridAdapter extends BaseAdapter {

        private ArrayList<MasterItem> masters = new ArrayList<>();
        private String listRef;
        private int type;

        public GridAdapter(String _listRef, int _type) {
            listRef = _listRef;
            type = _type;
        }

        public void addItem(MasterItem i) {
            masters.add(i);
            GridAdapter.this.notifyDataSetChanged();
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

            card.findViewById(R.id.change).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startEdit(listRef + getItem(i).id, type);
                }
            });

            return card;
        }
    }
}