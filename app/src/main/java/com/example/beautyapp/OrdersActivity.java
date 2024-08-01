package com.example.beautyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
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

public class OrdersActivity extends AppCompatActivity {

    private View loading;
    private int i;

    private LinearLayout ll;

    private void showLoading() {
        if (loading == null) {
            loading = getLayoutInflater().inflate(R.layout.loading, null, false);
            loading.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            ((ViewGroup) findViewById(R.id.main_cont)).addView(loading);
        }
        loading.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        if (loading != null) {
            loading.setVisibility(View.INVISIBLE);
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(OrdersActivity.this, AccountActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        showLoading();

        ll = findViewById(R.id.linear);

        new FirebaseGiver().getDatabase().getReference().child("users").child(new FirebaseGiver().getAuth().getUid())
                .child("orders").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> keys = new ArrayList<>();

                        for (DataSnapshot s : snapshot.getChildren()) {
                            keys.add(s.getKey());
                        }

                        download(0, keys, findViewById(R.id.linear), snapshot.getRef());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        hideLoading();
                        Toast.makeText(OrdersActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void download(int index, ArrayList<String> keys, LinearLayout layout, DatabaseReference ref) {
        if (index == keys.size()) {
            hideLoading();
            return;
        }

        OrderItem item = new OrderItem();

        ref.child(keys.get(index)).child("difficulty").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                item.title = snapshot.getValue(String.class);
                ref.child(keys.get(index)).child("time").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        item.time = snapshot.getValue(String.class);
                        ref.child(keys.get(index)).child("date").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                item.date = snapshot.getValue(String.class);
                                ref.child(keys.get(index)).child("master_name").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        item.master_name = snapshot.getValue(String.class);
                                        item.ref = ref.child(keys.get(index));
                                        layout.addView(createCard(item));
                                        download(index + 1, keys, layout, ref);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(OrdersActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(OrdersActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrdersActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public View createCard(OrderItem item) {
        View view = getLayoutInflater().inflate(R.layout.order_layout, null, false);

        ((TextView) view.findViewById(R.id.title)).setText(item.title);
        ((TextView) view.findViewById(R.id.date)).setText(item.date);
        ((TextView) view.findViewById(R.id.time)).setText(item.time);
        ((TextView) view.findViewById(R.id.master)).setText(item.master_name);

        view.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i++;
                Handler handler = new Handler();
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        i = 0;
                        Toast.makeText(OrdersActivity.this, "Нажмите дважды, чтобы удалить", Toast.LENGTH_SHORT).show();
                    }
                };
                if (i == 1) {
                    //Single click
                    handler.postDelayed(r, 250);
                } else if (i == 2) {
                    //Double click
                    i = 0;
                    item.ref.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(OrdersActivity.this, "Заказ отменен", Toast.LENGTH_SHORT).show();
                            ll.removeView(view);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(OrdersActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });



        return view;
    }

    private class OrderItem {
        public String title;
        public String date;
        public String master_name;
        public String time;
        public DatabaseReference ref;
    }
}