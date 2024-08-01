package com.example.beautyapp;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class PhotoViewer extends DialogFragment {

    private Bitmap img;
    private int pos;

    public PhotoViewer(Bitmap photo, int position) {
        img = photo;
        pos = position;
    }

    public interface PhotoListener {
        void DeleteImage(int position);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        PhotoListener listener = null;
        try {
            listener = (PhotoListener) getActivity();
        }
        catch (Exception e) {

        }

        View view = getLayoutInflater().inflate(R.layout.photo_layout, null, false);

        ImageView photo = view.findViewById(R.id.photo);

        int w = (int) (getResources().getDisplayMetrics().widthPixels - 32/getResources().getDisplayMetrics().density);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(w,  w);

        photo.setLayoutParams(params);
        photo.setImageBitmap(img);

        photo.setScaleType(ImageView.ScaleType.CENTER_CROP);

        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        if(listener != null) {
            PhotoListener finalListener = listener;
            view.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finalListener.DeleteImage(pos);
                    dismiss();
                }
            });
        }
        else {
            view.findViewById(R.id.delete).setVisibility(View.INVISIBLE);
        }

        builder.setView(view);
        Dialog d = builder.create();
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return d;
    }
}
