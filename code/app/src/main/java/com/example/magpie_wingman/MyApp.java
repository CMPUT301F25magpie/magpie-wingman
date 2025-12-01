package com.example.magpie_wingman;

import android.app.Application;

import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.User;
import com.google.firebase.FirebaseApp;

/**
 * Application-level singleton used to initialize global services and provide
 * shared state across the entire app. This class initializes Firebase, sets up
 * the {@link DbManager} singleton, and stores the currently authenticated user
 * so it can be accessed from any part of the application.
 * //I got the idea for this from <a href="https://stackoverflow.com/questions/43197379/application-singleton-use-in-android">...</a> and
 * https://stackoverflow.com/questions/45903111/android-extending-application-class-why-do-we-need-to-implement-singleton-patt
 * <p>The class exposes {@link #getInstance()} to retrieve the global application
 * instance and provides getter/setter methods for tracking the logged-in user from the other fragments and activites.</p>
 */

public class MyApp extends Application {

    private static MyApp instance;

    private User currentUser;   // globally accessible user object representing the logged in user

    /**
     * Called when the application is first created. Initializes Firebase, stores
     * the global application instance, and sets up the {@link DbManager} singleton
     * used for all Firestore and Storage operations.
     */
    @Override

    public void onCreate() {
        super.onCreate();

        instance = this;

        // init Firebase
        FirebaseApp.initializeApp(this);

        // init DbManager singleton
        DbManager.init(this);
        DbManager.getInstance();
    }

    /**
     * Returns the global {@code MyApp} application instance. This allows access to
     * application-level singletons and state from anywhere in the app.
     *
     * @return The shared {@code MyApp} instance.
     */
    public static MyApp getInstance() {
        return instance;
    }

    // ---- Current user global accessors ----

    /**
     * Retrieves the user currently logged into the application. This value is set
     * after authentication and remains available globally for the duration of the
     * app session.
     *
     * @return The currently authenticated {@link User}, or null if no user is logged in.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Stores the authenticated user globally so their information can be accessed
     * throughout the application. This is called upon successful login or
     * auto-login.
     *
     * @param currentUser The {@link User} object representing the logged-in user.
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

}