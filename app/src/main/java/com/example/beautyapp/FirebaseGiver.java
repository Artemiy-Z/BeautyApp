package com.example.beautyapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseGiver {
    public FirebaseDatabase getDatabase() {
        return FirebaseDatabase.getInstance("https://beauty-d4ba8-default-rtdb.europe-west1.firebasedatabase.app");
    }

    public FirebaseStorage getStorage() {
        return FirebaseStorage.getInstance("gs://beauty-d4ba8.appspot.com/");
    }

    public FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }
}
