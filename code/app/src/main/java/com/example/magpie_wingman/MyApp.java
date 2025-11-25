package com.example.magpie_wingman;

import android.app.Application;

import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.User;
import com.google.firebase.FirebaseApp;

/**
 * Initializes and maintains things that are meant to be instantiated at run and global across activities.
 * Use this because we want only one instance of FirebaseApp and DbManager running,
 * and also a single place to store the currently logged-in user.
 */
public class MyApp extends Application {

    private static MyApp instance;

    private User currentUser;   // <- globally accessible logged-in user

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;  // make this Application globally accessible

        // init Firebase
        FirebaseApp.initializeApp(this);

        // init DbManager singleton
        DbManager.init(this);
        DbManager.getInstance();
    }

    public static MyApp getInstance() {
        return instance;
    }

    // ---- Current user global accessors ----

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void clearCurrentUser() {
        this.currentUser = null;
    }
}