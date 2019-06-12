package com.goteacher.form;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.goteacher.R;
import com.goteacher.form.fragment.AddressMap;
import com.goteacher.utils.PermissionHelper;
import com.goteacher.utils.Utils;
import com.goteacher.utils.model.Model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.goteacher.Apps.app;

public class AdsFormActivity extends AppCompatActivity implements View.OnClickListener, PermissionHelper.PermissionListener,
        CompoundButton.OnCheckedChangeListener, OnMapReadyCallback {

    private FirebaseFirestore firestore;
    private EditText eTitle, eDesc, eRates, eAddress;
    private CheckBox cbElem, cbJun, cbSen, cbUniv, cbAddress;
    private Button submit;
    private LinearLayout addressLayout;
    private String title, desc, rates, address;

    private final int CODE = 888;
    private String filePath = "";
    private String fileName = "";
    private String imgURL = "";
    private String coordinate = "";
    private ImageView banner;
    private Model model;

    private final String endPoint = "https://go-teacher2018.000webhostapp.com/";
    private ProgressDialog pDialog;
    private boolean[] cat = {false, false, false, false};
    private boolean showUpdate = false;
    private boolean updateSession = false;

    private GoogleMap mMap;
    private Marker marker;
    private LatLng latLng  = new LatLng(-6.990172, 110.422943);
    private boolean mapOpen = false;
    private AddressMap addressMap;

    private Handler mHandler = new Handler();
    private FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ads_form);

        app().adsForm = this;
        model = app().model;
        firestore = FirebaseFirestore.getInstance();

        eTitle  = findViewById(R.id.ads_title);
        eDesc   = findViewById(R.id.ads_desc);
        eRates  = findViewById(R.id.ads_rates);
        cbElem  = findViewById(R.id.ads_elementary);
        cbJun   = findViewById(R.id.ads_junior);
        cbSen   = findViewById(R.id.ads_senior);
        cbUniv  = findViewById(R.id.ads_univ);
        banner  = findViewById(R.id.ads_banner);
        submit  = findViewById(R.id.ads_submit);
        eAddress = findViewById(R.id.ads_address);
        cbAddress = findViewById(R.id.cb_ads_address);
        addressLayout = findViewById(R.id.address_layout);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);
        pDialog.setMessage(getResources().getString(R.string.info_still_loading));

        submit.setOnClickListener(this);
        findViewById(R.id.ads_banner).setOnClickListener(this);
        findViewById(R.id.show_map).setOnClickListener(this);

        app().permission = new PermissionHelper(this);
        app().permission.setPermissionListener(this);

        initView();
    }

    private void initView() {
        updateSession = !model.getTitle().isEmpty();
        if (updateSession) {
            eTitle.setText(model.getTitle());
            eDesc.setText(model.getDesc());
            eRates.setText(String.valueOf(model.getRates()));
            eRates.setText(String.valueOf(model.getRates()));
            eAddress.setText(String.valueOf(model.getAddress()));
            for (String value : model.getCategory()) {
                switch (value) {
                    case "SD":
                        cat[0] = true;
                        break;
                    case "SMP":
                        cat[1] = true;
                        break;
                    case "SMA":
                        cat[2] = true;
                        break;
                    case "Universitas":
                        cat[3] = true;
                        break;
                }
            }
            cbElem.setChecked(cat[0]);
            cbJun.setChecked(cat[1]);
            cbSen.setChecked(cat[2]);
            cbUniv.setChecked(cat[3]);
            cbAddress.setChecked(model.isActiveAddress());
            addressLayout.setVisibility(model.isActiveAddress() ? View.VISIBLE : View.GONE);
            coordinate = model.getCoordinate();
            Glide.with(this).load(model.getImgURL()).into(banner);

            eTitle.addTextChangedListener(new GenericTextWatcher());
            eDesc.addTextChangedListener(new GenericTextWatcher());
            eRates.addTextChangedListener(new GenericTextWatcher());
            eAddress.addTextChangedListener(new GenericTextWatcher());
            cbElem.setOnCheckedChangeListener(this);
            cbJun.setOnCheckedChangeListener(this);
            cbSen.setOnCheckedChangeListener(this);
            cbUniv.setOnCheckedChangeListener(this);
            submit.setText(getResources().getString(R.string.title_save_class));
        }
        cbAddress.setOnCheckedChangeListener(this);
        submit.setVisibility(updateSession ? View.GONE : View.VISIBLE);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
    }

    public String getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(String _coordinate) {
        coordinate = _coordinate;
        refreshMarker();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ads_submit:
                submitAds();
                break;
            case R.id.ads_banner:
                List<String> listPermissions = new ArrayList<>();
                listPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                listPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                app().permission.checkAndRequestPermissions(listPermissions);
                break;
            case R.id.show_map:
                loadFragment();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_ads_address:
                addressLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                break;
        }
        updateChecker();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        refreshMarker();
    }

    private void refreshMarker() {
        if (marker != null)
            marker.remove();

        if (!coordinate.isEmpty()) {
            String[] coordinates = coordinate.split(", ");
            latLng = new LatLng(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
            marker = mMap.addMarker(new MarkerOptions().position(latLng));
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private class GenericTextWatcher implements TextWatcher {

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            updateChecker();
        }

        public void afterTextChanged(Editable editable) { }
    }

    private void updateChecker() {
        if (!updateSession)
            return;
        boolean show = !eTitle.getText().toString().equals(model.getTitle()) || !eDesc.getText().toString().equals(model.getDesc())
                || !eAddress.getText().toString().equals(model.getAddress()) || !eRates.getText().toString().equals(String.valueOf(model.getRates()))
                || !filePath.isEmpty() || !coordinate.equals(model.getCoordinate()) || cbAddress.isChecked() != model.isActiveAddress()
                || cbElem.isChecked() != cat[0] || cbJun.isChecked() != cat[1] || cbSen.isChecked() != cat[2] || cbUniv.isChecked() != cat[3];
        if (showUpdate != show) {
            showUpdate = show;
            submit.setVisibility(showUpdate ? View.VISIBLE : View.GONE);
        }
    }

    private void submitAds() {
        title = eTitle.getText().toString();
        desc  = eDesc.getText().toString();
        rates = eRates.getText().toString();
        address = eAddress.getText().toString();

        if (title.isEmpty()) {
            eTitle.setError(getResources().getString(R.string.info_fill_correctly));
            return;
        }

        if (desc.isEmpty()) {
            eDesc.setError(getResources().getString(R.string.info_fill_correctly));
            return;
        }

        if (!cbElem.isChecked() && !cbJun.isChecked() && !cbSen.isChecked() && !cbUniv.isChecked()) {
            showInfo(getResources().getString(R.string.info_need_to_set_category));
            return;
        }

        if (rates.equals("0") || rates.length() < 5) {
            eRates.setError(getResources().getString(R.string.info_fill_correctly));
            return;
        }

        if (cbAddress.isChecked() && (address.length() < 4 || coordinate.isEmpty())) {
            if (address.length() < 4)
                eAddress.setError(getResources().getString(R.string.info_fill_correctly));
            else
                showInfo(getResources().getString(R.string.info_choose_map_location));
            return;
        }

        if (!updateSession && filePath.isEmpty()) {
            showInfo(getResources().getString(R.string.info_ads_banner_empty));
            return;
        }

        pDialog.show();
        if (!filePath.isEmpty())
            new Upload().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            addAds();
    }

    @SuppressLint("StaticFieldLeak")
    public class Upload extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                String dirPath  = String.valueOf(Environment.getExternalStorageDirectory()) + File.separator
                        + app().TAG + File.separator + "pic/";
                String path = dirPath + Utils.getUsername() + "_" + System.currentTimeMillis() + ".jpg";
                File file = new File(path);
                if (Utils.directoryExist(new File (dirPath)))
                    fileName = file.getName();

                Bitmap bm = Utils.resizeImage(filePath, path, 800);
                if (bm != null) {
                    RequestBody body = RequestBody.create(MediaType.parse("image/jpg"), file);
                    RequestBody formBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("image", file.getName(), body).build();
                    Request request = new Request.Builder().url(endPoint + "upload.php").post(formBody).build();

                    OkHttpClient client = new OkHttpClient();
                    Response response = client.newCall(request).execute();
                    return Objects.requireNonNull(response.body()).string();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("success") && !fileName.isEmpty()) {
                imgURL = endPoint + "img/" + fileName;
                addAds();
            } else {
                hideDialog();
                showInfo(getResources().getString(R.string.info_connection_failure));
            }
        }
    }


    private void addAds() {
        Map<String, Object> data = new HashMap<>();
        data.put(Model.key.title.name(), title);
        data.put(Model.key.desc.name(), desc);
        data.put(Model.key.rates.name(), Long.parseLong(rates));
        data.put(Model.key.category.name(), getCategory());
        data.put(Model.key.active_address.name(), cbAddress.isChecked());
        data.put(Model.key.address.name(), address);
        data.put(Model.key.coordinate.name(), coordinate);
        data.put(Model.key.imgURL.name(), imgURL.isEmpty() ? model.getImgURL() : imgURL);
        if (updateSession) {
            firestore.collection(app().packageName).document(app().dbName)
                    .collection(app().Ads).document(model.getId())
                    .update(data)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                                finish();
                            else {
                                hideDialog();
                                showInfo(getResources().getString(R.string.info_connection_failure));
                            }
                        }
                    });
        } else {
            data.put(Model.key.creator.name(), Objects.requireNonNull(app().account.getEmail()));
            data.put(Model.key.published.name(), true);
            data.put(Model.key.created.name(), System.currentTimeMillis());
            firestore.collection(app().packageName).document(app().dbName)
                    .collection(app().Ads)
                    .add(data)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful())
                                finish();
                            else {
                                hideDialog();
                                showInfo(getResources().getString(R.string.info_connection_failure));
                            }
                        }
                    });
        }

    }

    private void showInfo(String info) {
        Snackbar.make(findViewById(R.id.coordinator), info, Snackbar.LENGTH_LONG).show();
    }

    private void hideDialog() {
        if (pDialog != null && pDialog.isShowing())
            pDialog.hide();
    }

    private List<String> getCategory() {
        List<String> category = new ArrayList<>();
        if (cbElem.isChecked())
            category.add(app().category[1]);
        if (cbJun.isChecked())
            category.add(app().category[2]);
        if (cbSen.isChecked())
            category.add(app().category[3]);
        if (cbUniv.isChecked())
            category.add(app().category[4]);
        return category;
    }

    public void loadFragment() {
        addressMap = null;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                if (!mapOpen) {
                    addressMap = new AddressMap();
                    fragmentTransaction.replace(R.id.fragment_container, addressMap, "");
                    fragmentTransaction.commitAllowingStateLoss();
                } else if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) != null) {
                    fragmentTransaction.
                            remove(Objects.requireNonNull(
                                    getSupportFragmentManager().findFragmentById(R.id.fragment_container))).commit();
                }
                mapOpen = !mapOpen;
            }
        });
    }

    @Override
    public void onPermissionCheckDone() {
        if (mapOpen) {
            if (addressMap != null)
                addressMap.setLocation();
        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.title_choose_image)), CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                final Uri uri = data.getData();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Bitmap bitmap = Utils.resizeBitmap(
                                    MediaStore.Images.Media.getBitmap(getContentResolver(), uri), 800);
                            Glide.with(AdsFormActivity.this).load(bitmap).into(banner);
                            filePath = Utils.getPathFromUri(AdsFormActivity.this, uri);
                            updateChecker();
                        } catch (Exception e) {
                            e.printStackTrace();
                            app().main.showInfo(getResources().getString(R.string.info_something_wrong));
                        }

                    }
                });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        app().permission.onRequestCallBack(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        if (mapOpen)
            loadFragment();
        else
            super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app().adsForm = null;
    }
}