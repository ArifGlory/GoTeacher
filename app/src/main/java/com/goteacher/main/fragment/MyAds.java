package com.goteacher.main.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.goteacher.R;
import com.goteacher.detail.DetailActivity;
import com.goteacher.form.AdsFormActivity;
import com.goteacher.main.adapter.PersonalAdapter;
import com.goteacher.utils.Utils;
import com.goteacher.utils.model.Model;

import java.util.ArrayList;
import java.util.Objects;

import static com.goteacher.Apps.app;

public class MyAds extends Fragment implements View.OnClickListener, PersonalAdapter.Listener {

    private FirebaseFirestore firestore;
    private RecyclerView myList;
    private PersonalAdapter adapter;
    private ImageButton add;

    private ArrayList<Model> myData = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_personal, container, false);

        firestore = FirebaseFirestore.getInstance();
        add = view.findViewById(R.id.add);
        myList = view.findViewById(R.id.my_list);
        myList.setLayoutManager(new LinearLayoutManager(getActivity()));
        myList.setItemAnimator(new DefaultItemAnimator());
        myList.addOnScrollListener(new ScrollListener());

        add.setOnClickListener(this);
        view.setOnClickListener(this);
        refreshList(app().main.getMyData());
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add:
                if (app().prefs.isActive()) {
                    app().model = new Model();
                    startActivity(new Intent(getActivity(), AdsFormActivity.class));
                }
                else {
                    app().main.showInfo(getResources().getString(R.string.info_banned));
                    Objects.requireNonNull(getActivity()).onBackPressed();
                }
                break;
        }
    }

    public void refreshList(ArrayList<Model> data) {
        myData = data;
        myData = Utils.sortData(myData, 99);
        if (adapter == null) {
            adapter = new PersonalAdapter(getActivity(), myData, this);
            myList.setAdapter(adapter);
        } else
            adapter.updateList(myData);
    }

    @Override
    public void onView(int pos) {
        app().model = myData.get(pos);
        startActivity(new Intent(getActivity(), DetailActivity.class));
    }

    @Override
    public void onEdit(int pos) {
        app().model = myData.get(pos);
        startActivity(new Intent(getActivity(), AdsFormActivity.class));
    }

    @Override
    public void onDelete(int pos) {
        final String id = myData.get(pos).getId();
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(Objects.requireNonNull(getActivity()).getResources().getString(R.string.title_delete));
        alertDialog.setMessage(myData.get(pos).getTitle());
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
                                .collection(app().Ads).document(id)
                                .delete()
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        app().main.showInfo(Objects.requireNonNull(getActivity()).getResources().getString(R.string.info_connection_failure));
                                    }
                                });
                    }
                });
        alertDialog.show();
    }


    private class ScrollListener extends RecyclerView.OnScrollListener {
        private boolean hide = false;
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            boolean scrollingUp = dy > 0;
            if (hide != scrollingUp) {
                hide  = scrollingUp;
                add.setVisibility(hide ? View.GONE : View.VISIBLE);
            }
        }
    }
}