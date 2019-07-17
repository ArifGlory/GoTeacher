package com.goteacher.admin;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.goteacher.R;
import com.goteacher.main.KonfirmasiActivity;
import com.goteacher.utils.model.Pesanan;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DetailPesananActivity extends AppCompatActivity {

    Button btnTerima,btnTolak;
    TextView txtStatus,txtHarga,txtNamaPemesan,txtTitleCourse,txtNoHape;
    ImageView imgBuktiBayar;
    Intent intent;
    Pesanan pesanan;
    FirebaseFirestore firestore;
    CollectionReference ref,refPesanan;
    private SweetAlertDialog pDialogLoading,pDialodInfo;
    private String status,isAdmin;

    static final int RC_PERMISSION_READ_EXTERNAL_STORAGE = 1;
    static final int RC_IMAGE_GALLERY = 2;
    Uri uriGambar,file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pesanan);
        Firebase.setAndroidContext(DetailPesananActivity.this);
        FirebaseApp.initializeApp(DetailPesananActivity.this);
        firestore = FirebaseFirestore.getInstance();
        ref = firestore.collection("com.goteacher").document("Teacher").collection("User");
        refPesanan = firestore.collection("pesanan");

        intent = getIntent();
        pesanan = (Pesanan) intent.getSerializableExtra("pesanan");
        isAdmin = intent.getStringExtra("isAdmin");
        status  = pesanan.getStatus();

        pDialogLoading = new SweetAlertDialog(DetailPesananActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialogLoading.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialogLoading.setTitleText("Loading..");
        pDialogLoading.setCancelable(false);

        imgBuktiBayar   = findViewById(R.id.ivBukti);
        btnTerima       = findViewById(R.id.btnTerima);
        btnTolak        = findViewById(R.id.btnTolak);
        txtNamaPemesan  = findViewById(R.id.txtNamaPemesan);
        txtTitleCourse  = findViewById(R.id.txtCourse);
        txtHarga        = findViewById(R.id.txtHargaCourse);
        txtStatus       = findViewById(R.id.txtStatus);
        txtNoHape       = findViewById(R.id.txtNoHape);

        if (!isAdmin.equals("true")){
            setButtonInvisible();
        }

        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.ENGLISH);
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        int harga = Integer.parseInt(pesanan.getHargaCourse());
        txtHarga.setText(formatRupiah.format((double) harga));

        txtTitleCourse.setText(pesanan.getTitleCourse());
        if (pesanan.getImgBuktiBayar().equals("no")){
            imgBuktiBayar.setImageResource(R.drawable.belum_upload);
        }else {
            Glide.with(this)
                    .load(pesanan.getImgBuktiBayar())
                    .into(imgBuktiBayar);
        }
        txtStatus.setText(pesanan.getStatus());

        if (status.equals("ditolak")){
            btnTerima.setEnabled(false);
            btnTerima.setVisibility(View.INVISIBLE);
            txtStatus.setTextColor(getResources().getColor(R.color.colorAccent));

            btnTolak.setVisibility(View.INVISIBLE);
            btnTolak.setEnabled(false);
        }else if (status.equals("diterima")){
            btnTerima.setEnabled(false);
            btnTerima.setVisibility(View.INVISIBLE);
            txtStatus.setTextColor(getResources().getColor(R.color.colorPrimary));

            btnTolak.setVisibility(View.INVISIBLE);
            btnTolak.setEnabled(false);
        }

        btnTolak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new SweetAlertDialog(DetailPesananActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Tolak Pesanan")
                        .setContentText("Anda yakin menolak pesanan ini?")
                        .setConfirmText("Ya")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();

                                refPesanan.document(pesanan.getIdPesanan()).update("status","ditolak").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            new SweetAlertDialog(DetailPesananActivity.this,SweetAlertDialog.SUCCESS_TYPE)
                                                    .setContentText("Pesanan berhasil ditolak")
                                                    .show();

                                            setButtonInvisible();
                                            txtStatus.setText("ditolak");
                                            txtStatus.setTextColor(getResources().getColor(R.color.colorAccent));
                                        }else {
                                            new SweetAlertDialog(DetailPesananActivity.this,SweetAlertDialog.ERROR_TYPE)
                                                    .setContentText("terjadi kesalahan coba lagi nanti: ")
                                                    .show();
                                            Log.d("gagalDitolak:",task.getException().toString());
                                        }
                                    }
                                });

                            }
                        })
                        .setCancelButton("Tidak", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                            }
                        })
                        .show();

            }
        });
        btnTerima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SweetAlertDialog(DetailPesananActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Terima Pesanan")
                        .setContentText("Anda yakin menerima pesanan ini?")
                        .setConfirmText("Ya")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                refPesanan.document(pesanan.getIdPesanan()).update("status","diterima").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            new SweetAlertDialog(DetailPesananActivity.this,SweetAlertDialog.SUCCESS_TYPE)
                                                    .setContentText("Pesanan berhasil diterima")
                                                    .show();
                                            setButtonInvisible();
                                            txtStatus.setText("diterima");
                                            txtStatus.setTextColor(getResources().getColor(R.color.colorPrimary));

                                            pushDataPesananKeUser();


                                        }else {
                                            new SweetAlertDialog(DetailPesananActivity.this,SweetAlertDialog.ERROR_TYPE)
                                                    .setContentText("terjadi kesalahan coba lagi nanti: ")
                                                    .show();
                                            Log.d("gagalDitolak:",task.getException().toString());
                                        }
                                    }
                                });

                            }
                        })
                        .setCancelButton("Tidak", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                            }
                        })
                        .show();
            }
        });
        imgBuktiBayar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAdmin.equals("true")){
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(DetailPesananActivity.this, new String[] {android.Manifest.permission.READ_EXTERNAL_STORAGE}, RC_PERMISSION_READ_EXTERNAL_STORAGE);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, RC_IMAGE_GALLERY);
                    }
                }
            }
        });

        getDataUser(pesanan.getIdUser());


    }

    private void pushDataPesananKeUser(){
        //final String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        pesanan.setStatus("diterima");
        ref.document(pesanan.getIdPemilikCourse()).collection("listPesanan")
                .document(pesanan.getIdPesanan()).set(pesanan);
    }

    private void getDataUser(final String idUser){

        ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (DocumentSnapshot doc : task.getResult()){
                        String emailUser = doc.getId();
                        Log.d("emailUser:",emailUser);
                        if (emailUser.equals(idUser)){
                            String namaUser = doc.get("name").toString();
                            String noHape   = doc.get("phone").toString();
                            txtNamaPemesan.setText(namaUser);
                            txtNoHape.setText(noHape);
                        }
                    }
                }else {
                    new SweetAlertDialog(getApplicationContext(),SweetAlertDialog.ERROR_TYPE)
                            .setContentText("gagal mendapatkan data user : "+idUser)
                            .show();
                    Log.d("gagalGetData:",task.getException().toString());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                new SweetAlertDialog(getApplicationContext(),SweetAlertDialog.ERROR_TYPE)
                        .setContentText("gagal mendapatkan data user : "+idUser)
                        .show();
                Log.d("gagalGetData:",e.toString());
            }
        });
    }

    private void setButtonInvisible(){
        btnTerima.setEnabled(false);
        btnTerima.setVisibility(View.INVISIBLE);

        btnTolak.setVisibility(View.INVISIBLE);
        btnTolak.setEnabled(false);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        // menangkap hasil balikan dari Place Picker, dan menampilkannya pada TextView

        if (requestCode == RC_IMAGE_GALLERY && resultCode == RESULT_OK) {
            uriGambar = data.getData();
            //Toast.makeText(DetailPesananActivity.this, "gambar terpilih : !\n" + tipe, Toast.LENGTH_LONG).show();

           // imgGambar.setImageURI(uri);
            uploadGambar(uriGambar);
        }
        else if (requestCode == 100 && resultCode == RESULT_OK){

        }
    }

    private void uploadGambar(final Uri uri){

        pDialogLoading.show();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imagesRef = storageRef.child("images");
        StorageReference userRef = imagesRef.child(pesanan.getIdPesanan());
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = pesanan.getIdCourse() + "_" + timeStamp;
        final StorageReference fileRef = userRef.child(filename);

        UploadTask uploadTask = fileRef.putFile(uri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                new SweetAlertDialog(DetailPesananActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Upload gagal")
                        .setContentText("terjadi kesalahan, coba lagi nanti")
                        .show();
                pDialogLoading.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {



                // save image to database
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        pDialogLoading.dismiss();

                        Uri downloadUrl = uri;

                        refPesanan.document(pesanan.getIdPesanan()).update("imgBuktiBayar",downloadUrl.toString());
                        imgBuktiBayar.setImageURI(uriGambar);


                        new SweetAlertDialog(DetailPesananActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Sukses!")
                                .setContentText("Bukti bayar Berhasil Disimpan")
                                .show();

                    }
                });




            }
        });
    }
}
