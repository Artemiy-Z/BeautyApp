package com.example.beautyapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class CatalogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        findViewById(R.id.account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CatalogActivity.this, AccountActivity.class));
                overridePendingTransition(R.anim.alpha_enter, R.anim.alpha_exit);
                finish();
            }
        });

        findViewById(R.id.haircut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CatalogActivity.this, HaircutActivity.class));
                finish();
            }
        });
        findViewById(R.id.massage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CatalogActivity.this, MassageActivity.class));
                finish();
            }
        });
        findViewById(R.id.manicure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CatalogActivity.this, ManiqureActivity.class));
                finish();
            }
        });
        findViewById(R.id.cosmetic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CatalogActivity.this, MakiashActivity.class));
                finish();
            }
        });
        findViewById(R.id.spa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CatalogActivity.this, SpaActivity.class));
                finish();
            }
        });
        findViewById(R.id.facecare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CatalogActivity.this, FaceActivity.class));
                finish();
            }
        });
        findViewById(R.id.epilation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CatalogActivity.this, EpilationActivity.class));
                finish();
            }
        });
        findViewById(R.id.paint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CatalogActivity.this, PaintActivity.class));
                finish();
            }
        });
    }
}