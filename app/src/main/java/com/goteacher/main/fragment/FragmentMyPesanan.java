package com.goteacher.main.fragment;


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

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.goteacher.R;
import com.goteacher.main.adapter.AdapterPesanan;
import com.goteacher.utils.model.Pesanan;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.goteacher.Apps.app;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentMyPesanan extends Fragment {


    public FragmentMyPesanan() {
        // Required empty public constructor
    }

    private String myEmail;
    private SweetAlertDialog pDialogLoading,pDialodInfo;
    RecyclerView recyclerView;
    FirebaseFirestore firestore;
    AdapterPesanan adapterPesanan;
    CollectionReference ref;
    private List<Pesanan> pesananList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_mypesanan, container, false);
        Firebase.setAndroidContext(this.getActivity());
        FirebaseApp.initializeApp(this.getActivity());
        firestore = FirebaseFirestore.getInstance();
        ref = firestore.collection("pesanan");
        recyclerView = view.findViewById(R.id.rvLayanan);
        pesananList = new ArrayList<>();
        adapterPesanan = new AdapterPesanan(getActivity(),pesananList);

        if (app().account == null){
            new SweetAlertDialog(getActivity(),SweetAlertDialog.NORMAL_TYPE)
                    .setTitle("Anda  Belum login");
        }else {

            myEmail = app().account.getEmail();

            recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
            recyclerView.setHasFixedSize(true);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(adapterPesanan);


            pDialogLoading = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
            pDialogLoading.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialogLoading.setTitleText("Loading..");
            pDialogLoading.setCancelable(false);
            pDialogLoading.show();

            getGambar();
        }

        return view;
    }


    public void getGambar(){
        ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                pesananList.clear();
                adapterPesanan.notifyDataSetChanged();

                if (task.isSuccessful()){
                    pDialogLoading.dismiss();
                    for (DocumentSnapshot doc : task.getResult()){
                        String idUser = doc.get("idUser").toString();

                        if (idUser.equals(myEmail)){
                            Pesanan pesanan = doc.toObject(Pesanan.class);

                            pesananList.add(pesanan);
                            adapterPesanan.notifyDataSetChanged();
                        }
                    }

                }else {
                    pDialogLoading.dismiss();
                    new SweetAlertDialog(getActivity(),SweetAlertDialog.ERROR_TYPE)
                            .setContentText("Pengambilan data gagal")
                            .show();
                    Log.d("gagalGetData:",task.getException().toString());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getGambar();
    }
}
