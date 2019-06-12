package com.goteacher.main.fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.goteacher.R;
import com.goteacher.utils.PermissionHelper;
import com.goteacher.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.goteacher.Apps.app;


public class UserProfile extends Fragment implements View.OnClickListener, PermissionHelper.PermissionListener {

    private TextView email;
    private LinearLayout profile;
    private ImageView img, genderIcon;
    private SignInButton login;

    private GoogleSignInClient client;
    private EditText phoneNumber, username, eEducation, eOccupation, eAddress;
    private Button adminSymbol, userUpdate;
    private ProgressDialog pDialog;
    private RadioGroup genderGroup;
    private String name, occupation, education, address, phone;
    private boolean showUpdate = false;
    private boolean gender = true;

    private final int CODE = 888;
    private String filePath = "";
    private String fileName = "";

    private final String endPoint = "https://go-teacher2018.000webhostapp.com/";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user, container, false);

        img   = view.findViewById(R.id.user_img);
        email = view.findViewById(R.id.user_email);
        eAddress = view.findViewById(R.id.user_address);
        profile = view.findViewById(R.id.user_profile);
        username = view.findViewById(R.id.user_name);
        phoneNumber = view.findViewById(R.id.user_phone);
        eEducation  = view.findViewById(R.id.user_education);
        eOccupation = view.findViewById(R.id.user_occupation);

        adminSymbol  = view.findViewById(R.id.admin);
        userUpdate   = view.findViewById(R.id.user_update);
        genderGroup  = view.findViewById(R.id.gender_group);
        genderIcon   = view.findViewById(R.id.gender_icon);

        client  = GoogleSignIn.getClient(Objects.requireNonNull(getActivity()),
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build());

        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(true);
        pDialog.setMessage(getActivity().getResources().getString(R.string.info_still_loading));

        login = view.findViewById(R.id.sign_in_button);
        SignInButton logout = view.findViewById(R.id.sign_out_button);
        setSignInButton(login, true);
        setSignInButton(logout, false);

        adminSymbol.setOnClickListener(this);
        userUpdate.setOnClickListener(this);
        login.setOnClickListener(this);
        logout.setOnClickListener(this);
        view.findViewById(R.id.user_man).setOnClickListener(this);
        view.findViewById(R.id.user_woman).setOnClickListener(this);
        view.findViewById(R.id.camera).setOnClickListener(this);
        view.setOnClickListener(this);

        app().permission = new PermissionHelper(getActivity());
        app().permission.setPermissionListener(this);

        updateUI();
        return view;
    }

    private void setSignInButton(SignInButton v, boolean login) {
        TextView txt = (TextView) v.getChildAt(0);
        txt.setText(Objects.requireNonNull(
                getActivity()).getResources().getString(login ? R.string.title_sign_in : R.string.title_sign_out));
        txt.setTextColor(getResources().getColor(R.color.magenta));
        txt.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                pDialog.show();
                startActivityForResult(client.getSignInIntent(), 123);
                break;
            case R.id.sign_out_button:
                pDialog.show();
                signOut();
                break;
            case R.id.user_man:
                setGenderIcon(true);
                break;
            case R.id.user_woman:
                setGenderIcon(false);
                break;
            case R.id.user_update:
                pDialog.show();
                if (!filePath.isEmpty())
                    new Upload().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else
                    updateProfile();
                break;
            case R.id.camera:
                List<String> listPermissions = new ArrayList<>();
                listPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                listPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                app().permission.checkAndRequestPermissions(listPermissions);
                break;
        }
    }

    public void updateUI() {
        profile.setVisibility(app().account != null ? View.VISIBLE : View.GONE);
        login.setVisibility(app().account != null ? View.GONE : View.VISIBLE);

        if (app().account != null) {
            name = app().prefs.getUsername();
            address = app().prefs.getAddress();
            occupation = app().prefs.getOccupation();
            education = app().prefs.getEducation();
            phone = app().prefs.getPhone();
            gender = app().prefs.getGender();

            username.setText(name);
            phoneNumber.setText(phone);
            eOccupation.setText(occupation);
            eEducation.setText(education);
            eAddress.setText(address);
            email.setText(app().account.getEmail());

            userUpdate.setVisibility(View.GONE);
            genderGroup.check(gender ? R.id.user_man :  R.id.user_woman);
            adminSymbol.setVisibility(app().prefs.isAdmin() ? View.VISIBLE : View.GONE);
            genderIcon.setImageResource(gender ? R.drawable.ic_man : R.drawable.ic_woman);
            Glide.with(Objects.requireNonNull(getActivity())).load(app().prefs.getImage()).into(img);

            username.addTextChangedListener(new GenericTextWatcher(username));
            eOccupation.addTextChangedListener(new GenericTextWatcher(eOccupation));
            eEducation.addTextChangedListener(new GenericTextWatcher(eEducation));
            eAddress.addTextChangedListener(new GenericTextWatcher(eAddress));
            phoneNumber.addTextChangedListener(new GenericTextWatcher(phoneNumber));
            checker();
        }
        hideDialog();
    }

    private void hideDialog() {
        if (pDialog != null && pDialog.isShowing())
            pDialog.hide();
    }

    @Override
    public void onPermissionCheckDone() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, Objects.requireNonNull(
                getActivity()).getResources().getString(R.string.title_choose_photo)), CODE);
    }

    private class GenericTextWatcher implements TextWatcher{

        private View view;
        private GenericTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            switch(view.getId()){
                case R.id.user_name:
                case R.id.user_education:
                case R.id.user_occupation:
                case R.id.user_address:
                case R.id.user_phone:
                    updateChecker();
                    break;
            }
        }

        public void afterTextChanged(Editable editable) { }
    }

    private void updateChecker() {
        boolean show = !username.getText().toString().equals(name) || !eOccupation.getText().toString().equals(occupation)
                || !eEducation.getText().toString().equals(education) || !phoneNumber.getText().toString().equals(phone)
                || !eAddress.getText().toString().equals(address) || gender != app().prefs.getGender() || (filePath != null && !filePath.isEmpty());
        if (showUpdate != show) {
            showUpdate = show;
            userUpdate.setVisibility(showUpdate ? View.VISIBLE : View.GONE);
        }
    }


    private void setGenderIcon(boolean man) {
        gender = man;
        genderIcon.setImageResource(man ? R.drawable.ic_man : R.drawable.ic_woman);
        updateChecker();
    }

    private void updateProfile() {
        name = username.getText().toString();
        address = eAddress.getText().toString();
        phone = phoneNumber.getText().toString();
        education = eEducation.getText().toString();
        occupation = eOccupation.getText().toString();

        if (checker()) {
            app().prefs.setUsername(name);
            app().prefs.setAddress(address);
            app().prefs.setPhone(phone);
            app().prefs.setEducation(education);
            app().prefs.setOccupation(occupation);
            app().prefs.setGender(gender);
            app().main.updateDataUser();
        } else
            hideDialog();
    }

    private boolean checker() {

        if (app().prefs.getImage().isEmpty()) {
            app().main.showInfo(Objects.requireNonNull(getActivity()).getResources().getString(R.string.info_need_photo));
            return false;
        }

        if (name.length() < 4) {
            username.setError(
                    Objects.requireNonNull(getActivity()).getResources().getString(
                            name.isEmpty() ? R.string.title_empty : R.string.info_min_4_letters));
            return false;
        }

        if (occupation.length() < 4) {
            eOccupation.setError(
                    Objects.requireNonNull(getActivity()).getResources().getString(
                            occupation.isEmpty() ? R.string.title_empty : R.string.info_min_4_letters));
            return false;
        }

        if (education.length() < 3) {
            eEducation.setError(
                    Objects.requireNonNull(getActivity()).getResources().getString(
                            education.isEmpty() ? R.string.title_empty : R.string.info_min_3_letters));
            return false;
        }

        if (!Utils.phoneNumberChecker(phone)) {
            phoneNumber.setError(Objects.requireNonNull(getActivity()).getResources().getString(
                    app().prefs.getPhone().isEmpty() ? R.string.title_empty : R.string.title_wrong_format));
            return false;
        }

        if (address.length() < 4) {
            eAddress.setError(
                    Objects.requireNonNull(getActivity()).getResources().getString(
                            address.isEmpty() ? R.string.title_empty : R.string.info_min_3_letters));
            return false;
        }

        return true;
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
                app().prefs.setImage(endPoint + "img/" + fileName);
                updateProfile();
            } else {
                hideDialog();
                app().main.showInfo(getResources().getString(R.string.info_connection_failure));
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 123) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                app().account = task.getResult(ApiException.class);
            } catch (ApiException e) {
                e.printStackTrace();
            }
            Log.d("akun :",""+task.getResult().getDisplayName());
            app().main.updateMyProfile();

            /*if (resultCode == Activity.RESULT_OK) {
                try {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    app().account = task.getResult(ApiException.class);
                    Log.d("akun :",""+task.getResult().getDisplayName());
                    app().main.updateMyProfile();
                } catch (ApiException e) {
                    hideDialog();
                    e.printStackTrace();
                    Log.d("eror login google:",""+e.getMessage().toString());
                }
            } else {
                hideDialog();
                app().main.showInfo(getResources().getString(R.string.info_connection_failure));

            }*/
        } else if (requestCode == CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                final Uri uri = data.getData();
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Bitmap bitmap = Utils.resizeBitmap(
                                    MediaStore.Images.Media.getBitmap(Objects.requireNonNull(
                                            getActivity()).getContentResolver(), uri), 800);
                            Glide.with(Objects.requireNonNull(getActivity())).load(bitmap).into(img);
                            filePath = Utils.getPathFromUri(getActivity(), uri);
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

    private void signOut() {
        client.signOut()
                .addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        app().account = null;
                        app().prefs.clear();
                        updateUI();
                    }
                });
    }

}