package com.example.magpie_wingman;

import android.app.Application;

import com.example.magpie_wingman.data.DbManager;
import com.google.firebase.FirebaseApp;

/**
 * Initializes and maintains things that are meant to be instantiated at run and global across activities.
 * Use this because we want only one instance of FirebaseApp and dbManager running.
 */
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // init Firebase
        FirebaseApp.initializeApp(this);

        DbManager.init(this);

        DbManager.getInstance();
    }
}