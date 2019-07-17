package com.goteacher.main;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.goteacher.R;
import com.goteacher.admin.AdminActivity;
import com.goteacher.utils.SharedVariable;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LoginAdminActivity extends AppCompatActivity {

    Button btnLogin;
    EditText etEmail,etPassword;
    private FirebaseAuth fAuth;
    private SweetAlertDialog pDialogLoading,pDialodInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_admin);
        Firebase.setAndroidContext(this);
        FirebaseApp.initializeApp(LoginAdminActivity.this);
        fAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        pDialogLoading = new SweetAlertDialog(LoginAdminActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialogLoading.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialogLoading.setTitleText("Loading..");
        pDialogLoading.setCancelable(false);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkValidation();
            }
        });

    }

    private void checkValidation(){
        String email    = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (email.equals("") || email.length() == 0
        || password.equals("") || password.length() == 0
        ){
            new SweetAlertDialog(this,SweetAlertDialog.ERROR_TYPE)
                    .setContentText("Semua field harus diiisi")
                    .show();

        }else{
            doLogin(email,password);
        }
    }

    private void doLogin(String email,String password){
        pDialogLoading.show();
        fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(LoginAdminActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (!task.isSuccessful()){
                    pDialogLoading.dismiss();
                    new SweetAlertDialog(LoginAdminActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Login gagal")
                            .setContentText("Periksa kembali Email dan Password anda")
                            .show();

                }else{
                    pDialogLoading.dismiss();
                    // Successfully signed in
                    SharedVariable.nama = "Admin";
                    SharedVariable.isAdmin = "true";
                    SharedVariable.userID = fAuth.getCurrentUser().getUid();
                    // get the Firebase user
                    FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

                    // get the FCM token
                    String token = FirebaseInstanceId.getInstance().getToken();


                    Intent i = new Intent(LoginAdminActivity.this, AdminActivity.class);
                    startActivity(i);

                }


            }
        });
    }
}
