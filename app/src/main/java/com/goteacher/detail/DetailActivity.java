package com.goteacher.detail;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.goteacher.R;
import com.goteacher.detail.fragment.AddressMap;
import com.goteacher.detail.fragment.Phone;
import com.goteacher.main.KonfirmasiActivity;
import com.goteacher.utils.PermissionHelper;
import com.goteacher.utils.model.Model;
import com.goteacher.utils.model.UserModel;

import java.util.Objects;

import javax.annotation.Nullable;

import static com.goteacher.Apps.app;
import static com.goteacher.utils.Utils.getCurrency;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener, PermissionHelper.PermissionListener,
        OnMapReadyCallback {

    private enum Tag {
        HOME, PHONE, MAP
    }

    private Tag mTag = Tag.HOME;
    private Model model;
    private FirebaseFirestore firestore;

    private ImageView imgUser;
    private TextView username, phone, occupation, education;

    private AddressMap addressMap;

    private Handler mHandler = new Handler();
    private FragmentTransaction fragmentTransaction;

    private boolean locked = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        model = app().model;
        firestore = FirebaseFirestore.getInstance();
        app().detail = this;
        app().permission = new PermissionHelper(this);
        app().permission.setPermissionListener(this);

        initCollapsingToolbar();
        setView();
    }

    private void setView() {

        TextView title = findViewById(R.id.teacher_courses);
        TextView desc  = findViewById(R.id.teacher_courses_desc);
        TextView rates = findViewById(R.id.teacher_courses_rates);
        TextView email = findViewById(R.id.teacher_email);
        ImageView banner = findViewById(R.id.teacher_banner);
        TextView address = findViewById(R.id.teacher_address);
        Button btnHire   = findViewById(R.id.btnHire);

        username  = findViewById(R.id.teacher_name);
        imgUser = findViewById(R.id.teacher_img);
        occupation = findViewById(R.id.teacher_occupation);
        education  = findViewById(R.id.teacher_education);
        phone = findViewById(R.id.teacher_phone);

        title.setText(model.getTitle());
        desc.setText(model.getDesc());
        email.setText(model.getCreator());
        rates.setText(getCurrency(model.getRates()));
        Glide.with(DetailActivity.this).load(model.getImgURL()).into(banner);

        phone.setOnClickListener(this);
        email.setOnClickListener(this);

        if (app().prefs.isAdmin()) {
            findViewById(R.id.remove).setOnClickListener(this);
            findViewById(R.id.remove).setVisibility(View.VISIBLE);
        }

        if (model.isActiveAddress() && !model.getAddress().isEmpty() && !model.getCoordinate().isEmpty()) {
            address.setText(model.getAddress());
            findViewById(R.id.show_map).setOnClickListener(this);
            findViewById(R.id.map_layout).setVisibility(View.VISIBLE);
            MapFragment mapFragment = (MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map);
            Objects.requireNonNull(mapFragment).getMapAsync(this);
        }

        getTeacherData();

        banner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),BannerFullScreen.class);
                intent.putExtra("url",model.getImgURL());
                startActivity(intent);
            }
        });
        btnHire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(getApplicationContext(), KonfirmasiActivity.class);
               intent.putExtra("model",model);
               startActivity(intent);
            }
        });
    }

    AppBarLayout appBarLayout;
    private void initCollapsingToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.appbar);

        setSupportActionBar(toolbar);
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (locked)
                    appBarLayout.setExpanded(true);
            }
        });
        new CountDownTimer(1000, 500) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                locked = false;
            }
        }.start();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        String[] coordinates = model.getCoordinate().split(", ");
        LatLng latLng = new LatLng(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
        googleMap.addMarker(new MarkerOptions().position(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.teacher_phone:
                loadFragment(Tag.PHONE);
                break;
            case R.id.teacher_email:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", model.getCreator(), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                startActivity(Intent.createChooser(emailIntent, "Kirim Email..."));
                break;
            case R.id.show_map:
                loadFragment(Tag.MAP);
                break;
            case R.id.remove:
                remove();
                break;
        }
    }

    private void getTeacherData() {
        DocumentReference docRef = firestore.collection(app().packageName).document(app().dbName)
                .collection(app().User).document(Objects.requireNonNull(model.getCreator()));
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null)
                    return;
                try {
                    if (snapshot != null && snapshot.exists()) {
                        username.setText(snapshot.getString(UserModel.key.name.name()));
                        occupation.setText(snapshot.getString(UserModel.key.occupation.name()));
                        education.setText(snapshot.getString(UserModel.key.education.name()));
                        phone.setText(snapshot.getString(UserModel.key.phone.name()));
                        String img = snapshot.getString(UserModel.key.imgURL.name());
                        if (img != null && !img.isEmpty())
                            Glide.with(DetailActivity.this).load(img).into(imgUser);
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void remove() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getResources().getString(R.string.title_delete));
        alertDialog.setMessage(model.getTitle());
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "BATAL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        firestore.collection(app().packageName).document(app().dbName)
                                .collection(app().Ads).document(model.getId())
                                .delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                            finish();
                                        else
                                            app().main.showInfo(getResources().getString(R.string.info_connection_failure));
                                    }
                                });
                    }
                });
        alertDialog.show();

    }

    public void showInfo(String message) {
        Snackbar.make(findViewById(R.id.coordinator), message, Snackbar.LENGTH_LONG).show();
    }

    public void loadFragment(Tag tag) {
        if (getSupportFragmentManager().findFragmentByTag(tag.toString()) != null)
            return;

        mTag = tag;
        addressMap = null;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                if (mTag != Tag.HOME) {
                    Fragment fragment;
                    switch (mTag) {
                        case MAP:
                            appBarLayout.setExpanded(false);
                            fragment = addressMap = new AddressMap().set(model.getCoordinate());
                            break;
                        default:
                            fragment = new Phone().set(phone.getText().toString());
                    }
                    fragmentTransaction.replace(R.id.fragment_container, fragment, mTag.toString());
                    fragmentTransaction.commitAllowingStateLoss();
                } else if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) != null) {
                    fragmentTransaction.
                            remove(Objects.requireNonNull(
                                    getSupportFragmentManager().findFragmentById(R.id.fragment_container))).commit();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        app().permission.onRequestCallBack(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionCheckDone() {
        if (mTag == Tag.MAP && addressMap != null)
                addressMap.setLocation();
    }


    @Override
    public void onBackPressed() {
        if (mTag != Tag.HOME)
            loadFragment(Tag.HOME);
        else
            super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app().detail = null;
    }
}