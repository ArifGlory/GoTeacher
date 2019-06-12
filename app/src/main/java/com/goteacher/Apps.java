package com.goteacher;

import android.app.Application;
import android.content.res.Configuration;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.goteacher.detail.DetailActivity;
import com.goteacher.form.AdsFormActivity;
import com.goteacher.main.MainActivity;
import com.goteacher.utils.PermissionHelper;
import com.goteacher.utils.PreferencesHelper;
import com.goteacher.utils.model.Model;

import java.util.Locale;

/**
 * Class ini akan di panggil paling pertama, karena extends Application
 * Jangan lupa daftarkan class ini di manifest:
 *
 *  <application
 *         android:name=".Apps"
 *
 *  Project ini menggunakan Singleton Architecture (Pattern)
 *  Sangat berguna untuk mengurangi variable static dan meng-handle masalah di JVM (Out Of Memory)
 *  Jadi, taruh semua variable yang bersifat global disini
 * */
public class Apps extends Application {

    public final String TAG = "GoTeacher";
    private static Apps instance;
    public GoogleSignInAccount account;
    public FirebaseAuth mAuth;

    public final String Ads = "Ads";
    public final String User = "User";
    public String dbName = "Teacher";
    public String packageName = "";
    public String adminEmail = "nurrohimilmam@gmail.com";

    public MainActivity main;
    public DetailActivity detail;
    public PreferencesHelper prefs;
    public PermissionHelper permission;
    public AdsFormActivity adsForm;


    public String[] category;
    public Model model;

    public static Apps app() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        packageName = getPackageName();
        instance = this;

        // Rubah config app ke Indonesia
        Locale locale = new Locale("id");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, null);

    }
}
