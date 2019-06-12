package com.goteacher.form.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.goteacher.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.goteacher.Apps.app;


public class AddressMap extends Fragment implements View.OnClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private String coordinate = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.ads_form_map, container, false);
        coordinate = app().adsForm.getCoordinate();
        try {
            FragmentManager fm = getChildFragmentManager();
            SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentByTag("mapFragment");
            if (mapFragment == null) {
                mapFragment = new SupportMapFragment();
                FragmentTransaction ft = fm.beginTransaction();
                ft.add(R.id.mapContainer, mapFragment, "mapFragment");
                ft.commit();
                fm.executePendingTransactions();
            }
            Objects.requireNonNull(mapFragment).getMapAsync(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        view.findViewById(R.id.back).setOnClickListener(this);
        view.findViewById(R.id.map_cancel).setOnClickListener(this);
        view.findViewById(R.id.map_ok).setOnClickListener(this);
        view.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
            case R.id.map_cancel:
                Objects.requireNonNull(getActivity()).onBackPressed();
                break;
            case R.id.map_ok:
                app().adsForm.setCoordinate(mMap.getCameraPosition().target.latitude + ", " + mMap.getCameraPosition().target.longitude);
                Objects.requireNonNull(getActivity()).onBackPressed();
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng latLng = new LatLng(-6.990172, 110.422943);
        if (!coordinate.isEmpty()) {
            String[] coordinates = coordinate.split(", ");
            latLng = new LatLng(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
        setLocation();
    }

    public void setLocation() {
        if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            List<String> listPermissions = new ArrayList<>();
            listPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            listPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            app().permission.checkAndRequestPermissions(listPermissions);
        } else
            mMap.setMyLocationEnabled(true);
    }
}
