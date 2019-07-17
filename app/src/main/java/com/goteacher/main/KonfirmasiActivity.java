package com.goteacher.main;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.goteacher.R;
import com.goteacher.detail.DetailActivity;
import com.goteacher.utils.model.Model;
import com.goteacher.utils.model.Pesanan;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.goteacher.utils.Utils.getCurrency;
import static com.goteacher.Apps.app;

public class KonfirmasiActivity extends AppCompatActivity {

    Intent intent;
    Model model;
    TextView teacher_courses,teacher_courses_rates;
    Button btnHire;
    ImageView teacher_banner;
    FirebaseFirestore firestore;
    CollectionReference ref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_konfirmasi);
        Firebase.setAndroidContext(KonfirmasiActivity.this);
        FirebaseApp.initializeApp(KonfirmasiActivity.this);
        firestore = FirebaseFirestore.getInstance();
        ref = firestore.collection("pesanan");

        intent = getIntent();

        teacher_banner          = findViewById(R.id.teacher_banner);
        teacher_courses         = findViewById(R.id.teacher_courses);
        teacher_courses_rates   = findViewById(R.id.teacher_courses_rates);
        btnHire                 = findViewById(R.id.btnHire);

        model = (Model) intent.getSerializableExtra("model");
        final String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        teacher_courses.setText(model.getTitle());
        teacher_courses_rates.setText(getCurrency(model.getRates()));
        Glide.with(KonfirmasiActivity.this).load(model.getImgURL()).into(teacher_banner);

        String email =  app().account.getEmail();
        final Pesanan pesanan = new Pesanan(
                model.getId(),
                email,
                model.getTitle(),
                String.valueOf(model.getRates()),
                "no",
                "menunggu konfirmasi",
                model.getCreator()
        );
        pesanan.setIdPesanan(time);

        btnHire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ref.document(time).set(pesanan).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(),"Berhasil Dipesan",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("erorPesan:","erorPesan"+e.toString());
                    }
                });
            }
        });

    }
}
