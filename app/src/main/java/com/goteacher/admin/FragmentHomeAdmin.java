package com.goteacher.admin;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.goteacher.R;
import com.goteacher.main.adapter.AdapterPesanan;
import com.goteacher.utils.model.Pesanan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;



/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentHomeAdmin extends Fragment {


    public FragmentHomeAdmin() {
        // Required empty public constructor
    }


    FirebaseFirestore firestore;

    private SweetAlertDialog pDialogLoading,pDialodInfo;


    FirebaseUser fbUser;
    private FirebaseAuth fAuth;
    CollectionReference ref;
    RecyclerView rvPesanan;
    AdapterPesanan adapter;
    private List<Pesanan> pesananList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_home_admin, container, false);
        Firebase.setAndroidContext(this.getActivity());
        FirebaseApp.initializeApp(this.getActivity());
        firestore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        ref = firestore.collection("pesanan");
        rvPesanan = view.findViewById(R.id.rvHomeAdmin);
        pesananList = new ArrayList<>();
        adapter = new AdapterPesanan(getActivity(),pesananList);

        rvPesanan.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rvPesanan.setHasFixedSize(true);
        rvPesanan.setItemAnimator(new DefaultItemAnimator());
        rvPesanan.setAdapter(adapter);

        pDialogLoading = new SweetAlertDialog(this.getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        pDialogLoading.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialogLoading.setTitleText("Menampilkan data..");
        pDialogLoading.setCancelable(false);
        pDialogLoading.show();

        getDataPesanan();

        return view;
    }

    private void getDataPesanan(){

        ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                pesananList.clear();
                adapter.notifyDataSetChanged();

                if (task.isSuccessful()){
                    pDialogLoading.dismiss();
                    for (DocumentSnapshot doc : task.getResult()){
                        Pesanan pesanan = doc.toObject(Pesanan.class);
                        pesananList.add(pesanan);
                    }
                    Collections.reverse(pesananList);
                    adapter.notifyDataSetChanged();


                }else{
                    pDialogLoading.dismiss();
                    new SweetAlertDialog(getActivity(),SweetAlertDialog.ERROR_TYPE)
                            .setContentText("Terjadi kesalahan, coba lagi nanti")
                            .show();
                    Log.d("gagalGetData:",task.getException().toString());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pDialogLoading.dismiss();
                new SweetAlertDialog(getActivity(),SweetAlertDialog.ERROR_TYPE)
                        .setContentText("Terjadi kesalahan, coba lagi nanti")
                        .show();
                Log.d("gagalGetData:",e.toString());
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        getDataPesanan();
    }
}
