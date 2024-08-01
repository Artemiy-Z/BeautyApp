package com.example.beautyapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialStyledDatePickerDialog;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class HaircutActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(HaircutActivity.this, CatalogActivity.class));
        finish();
    }

    private Spinner diff;
    private ArrayAdapter<String> adapter;
    private String id = "";
    private static int[] days;
    public static Date pickedDate = null;
    private AppCompatButton next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_haircut);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        next = findViewById(R.id.next);

        diff = findViewById(R.id.difficulity);
        String[] diffVariants = {
                "Сложность стрижки...",
                "Стандартная стрижка",
                "Сложная стрижка"};

        adapter = new ArrayAdapter<>(this, R.layout.spinner_item, diffVariants);
        diff.setAdapter(adapter);

        findViewById(R.id.pencil).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HaircutActivity.this, MastersViewActivity.class);
                intent.putExtra("kategory", "haircut");
                startActivityForResult(intent, 205);
            }
        });

        findViewById(R.id.datepicker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!id.equals("")) {
                    DatePickerFragment d = new DatePickerFragment();
                    d.show(getSupportFragmentManager(), "datePicker");
                }
                else {
                    Toast.makeText(HaircutActivity.this, "Сначала выберите мастера!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        check();
    }

    private void check() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setNextAlpha();

                check();
            }
        }, 1000);
    }

    private void setNextAlpha() {

        next.setAlpha(0.6f);
        next.setOnClickListener(null);

        if(diff.getSelectedItemPosition() == 0)
            return;
        else if(id.equals(""))
            return;
        else if(pickedDate == null)
            return;

        next.setAlpha(1f);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HaircutActivity.this, TimeSelectorActivity.class);

                intent.putExtra("kategory", "haircut");
                intent.putExtra("masterID", id);
                intent.putExtra("master", ((TextView)findViewById(R.id.master)).getText().toString().replace("Мастер:", ""));
                DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRENCH);
                intent.putExtra("date", df.format(pickedDate));
                intent.putExtra("difficulity", adapter.getItem(diff.getSelectedItemPosition()));

                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == 205) {
            id = data.getStringExtra("id");
            ((TextView)findViewById(R.id.master)).setText("Мастер: " + data.getStringExtra("name"));
            days = data.getIntArrayExtra("days");
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog pickerDialog = new DatePickerDialog(getActivity(), R.style.Theme_BeautyApp, this, year, month, day);

            Field mDatePickerField;
            try {
                mDatePickerField = pickerDialog.getClass().getDeclaredField("mDatePicker");
                mDatePickerField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }

            pickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis() - 1000);

            calendar.add(Calendar.DATE, 6);
            pickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

            return pickerDialog;
        }

        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {

            TextView date = (TextView) requireActivity().findViewById(R.id.date);

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(0);
            cal.set(year, month, day, 0, 0, 0);
            Date chosen = cal.getTime();

            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRENCH);
            String formattedDate = df.format(chosen);

            date.setText(formattedDate);
            pickedDate = chosen;
        }
    }
}