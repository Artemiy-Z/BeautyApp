package com.example.beautyapp;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class PlanDialog extends DialogFragment {

    public interface PlanListener {
        public void sendData(ArrayList<Integer> checked_days);
    }

    private PlanListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        listener = (PlanListener) getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getLayoutInflater().inflate(R.layout.dialog_set_workplan, null, false);

        boolean[] days = new boolean[7];

        view.findViewById(R.id.pn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (days[0]) {
                    days[0] = false;
                    view.setAlpha(0.5f);
                } else {
                    days[0] = true;
                    view.setAlpha(1f);
                }
            }
        });
        view.findViewById(R.id.pn).setAlpha(0.5f);

        view.findViewById(R.id.vt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (days[1]) {
                    days[1] = false;
                    view.setAlpha(0.5f);
                } else {
                    days[1] = true;
                    view.setAlpha(1f);
                }
            }
        });
        view.findViewById(R.id.vt).setAlpha(0.5f);

        view.findViewById(R.id.sr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (days[2]) {
                    days[2] = false;
                    view.setAlpha(0.5f);
                } else {
                    days[2] = true;
                    view.setAlpha(1f);
                }
            }
        });
        view.findViewById(R.id.sr).setAlpha(0.5f);

        view.findViewById(R.id.ct).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (days[3]) {
                    days[3] = false;
                    view.setAlpha(0.5f);
                } else {
                    days[3] = true;
                    view.setAlpha(1f);
                }
            }
        });
        view.findViewById(R.id.ct).setAlpha(0.5f);

        view.findViewById(R.id.pt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (days[4]) {
                    days[4] = false;
                    view.setAlpha(0.5f);
                } else {
                    days[4] = true;
                    view.setAlpha(1f);
                }
            }
        });
        view.findViewById(R.id.pt).setAlpha(0.5f);

        view.findViewById(R.id.sb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (days[5]) {
                    days[5] = false;
                    view.setAlpha(0.5f);
                } else {
                    days[5] = true;
                    view.setAlpha(1f);
                }
            }
        });
        view.findViewById(R.id.sb).setAlpha(0.5f);
        view.findViewById(R.id.vs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (days[6]) {
                    days[6] = false;
                    view.setAlpha(0.5f);
                } else {
                    days[6] = true;
                    view.setAlpha(1f);
                }
            }
        });
        view.findViewById(R.id.vs).setAlpha(0.5f);

        view.findViewById(R.id.positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ArrayList<Integer> out = new ArrayList<>();

                for (int i = 0; i < 7; i++) {
                    if (days[i])
                        out.add(i);
                }

                listener.sendData(out);

                dismiss();
            }
        });

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        builder.setView(view);

        return builder.create();
    }
}
