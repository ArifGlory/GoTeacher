package com.goteacher.main.fragment;


import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
import com.goteacher.main.MainActivity;
import com.goteacher.main.adapter.AdapterPesanan;
import com.goteacher.utils.model.Pesanan;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.goteacher.Apps.app;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentChoosePesanan extends Fragment {


    public FragmentChoosePesanan() {
        // Required empty public constructor
    }

    private String myEmail;
    private SweetAlertDialog pDialogLoading,pDialodInfo;

    private FragmentMyPesanan myPesanan;
    private FragmentPesananMengajar pesananMengajar;
    public enum Tag {
        MYPESANAN , PESANANMENGAJAR
    }
    private Tag mTag = Tag.MYPESANAN; // set tag/flag awal
    private Handler mHandler = new Handler();
    private FragmentTransaction fragmentTransaction;
    Fragment fragment;
    LinearLayout lineMyPesanan;
    LinearLayout linePesananMengajar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_choosepesanan, container, false);
        Firebase.setAndroidContext(this.getActivity());
        FirebaseApp.initializeApp(this.getActivity());
        lineMyPesanan       = view.findViewById(R.id.lineMyPesanan);
        linePesananMengajar = view.findViewById(R.id.linePesananMengajar);

        if (app().account == null){
            new SweetAlertDialog(getActivity(),SweetAlertDialog.NORMAL_TYPE)
                    .setTitle("Anda  Belum login");
        }else {

            myEmail = app().account.getEmail();
            lineMyPesanan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadFragment(Tag.MYPESANAN);
                }
            });
            linePesananMengajar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadFragment(Tag.PESANANMENGAJAR);
                }
            });

        }

        return view;
    }

    public void loadFragment(Tag tag) {

        // set tag dan netralkan semua class fragment
        mTag = tag;
        myPesanan = null;
        pesananMengajar = null;


        mHandler.post(new Runnable() {
            @Override
            public void run() {
                fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up); // animasi popup

                    switch (mTag) {
                        case MYPESANAN:
                            fragment = myPesanan = new FragmentMyPesanan();
                            break;
                        case PESANANMENGAJAR:
                            fragment = pesananMengajar = new FragmentPesananMengajar();
                            break;

                    }
                    fragmentTransaction.replace(R.id.fragment_container, fragment, mTag.toString());
                    fragmentTransaction.commitAllowingStateLoss();

            }
        });
    }





}
