package com.goteacher.detail.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.goteacher.R;
import com.goteacher.utils.model.UserModel;

import java.util.Objects;

import static com.goteacher.Apps.app;

public class Phone extends Fragment implements View.OnClickListener {

    String number = "";

    public Phone set(String phoneNumber) {
        Phone fragment = new Phone();
        Bundle args = new Bundle();
        args.putString(UserModel.key.phone.name(), phoneNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.detail_phone, container, false);

        if (getArguments() != null)
            number = getArguments().getString(UserModel.key.phone.name());

        view.findViewById(R.id.phone_call).setOnClickListener(this);
        view.findViewById(R.id.whatsapp).setOnClickListener(this);
        view.findViewById(R.id.phone_layout).setOnClickListener(this);
        view.findViewById(R.id.phone_blank_layout).setOnClickListener(this);
        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.phone_call:
                if (number.isEmpty())
                    return;
                try {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Test222", "error : " + e.toString());
                }
                break;
            case R.id.whatsapp:
                if (number.isEmpty())
                    return;
                try {
                    int length = number.length();
                    String url = "https://api.whatsapp.com/send?phone=+62 " + number.substring(1, length);
                    PackageManager pm = app().detail.getPackageManager();
                    pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } catch (PackageManager.NameNotFoundException e) {
                    app().detail.showInfo("Aplikasi WhatsApp belum ter-install");
                    e.printStackTrace();
                }
                break;
            case R.id.phone_layout:
                Objects.requireNonNull(getActivity()).onBackPressed();
                break;
        }
    }
}
