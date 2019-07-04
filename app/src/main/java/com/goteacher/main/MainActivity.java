package com.goteacher.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.goteacher.detail.DetailActivity;
import com.goteacher.R;
import com.goteacher.main.adapter.Adapter;
import com.goteacher.main.adapter.SearchAdapter;
import com.goteacher.main.fragment.FragmentChoosePesanan;
import com.goteacher.main.fragment.FragmentMyPesanan;
import com.goteacher.main.fragment.MyAds;
import com.goteacher.main.fragment.UserProfile;
import com.goteacher.utils.GridDecoration;
import com.goteacher.utils.PreferencesHelper;
import com.goteacher.utils.RecyclerViewListener;
import com.goteacher.utils.Utils;
import com.goteacher.utils.model.Model;
import com.goteacher.utils.model.UserModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import static com.goteacher.Apps.app;
import static com.goteacher.utils.Utils.dpToPx;

/**
 * Home atau Main Menu
 * **/
public class MainActivity extends AppCompatActivity implements View.OnClickListener, RecyclerViewListener.OnItemClick  {

    //Variable

    //flag untuk perpindahan fragment atau view
    public enum Tag {
        HOME, USER, ADS , PESANAN , CHOOSEPESANAN
    }

    private Handler mHandler = new Handler();
    private Tag mTag = Tag.HOME; // set tag/flag awal ke HOME

    private FragmentTransaction fragmentTransaction;

    private UserProfile myProfile;
    private MyAds myAds;
    private FragmentMyPesanan myPesanan;
    private FragmentChoosePesanan choosePesanan;

    private FirebaseFirestore firestore;
    private RecyclerView mainList, searchList;
    private ArrayList<Model> allData = new ArrayList<>();
    private ArrayList<Model> searchData = new ArrayList<>();
    private ArrayList<Model> myData = new ArrayList<>();
    private long exitTime = 0L;
    private DocumentReference docUser;

    private ListenerRegistration firestoreListener;
    private Adapter adapter;

    private LinearLayout filterLayout;

    private RelativeLayout sortingLayout, categoryLayout, searchLayout;
    private EditText search;
    private ImageButton clearSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        app().main = this;
        app().mAuth = FirebaseAuth.getInstance();
        app().account = GoogleSignIn.getLastSignedInAccount(this);
        app().category = getResources().getStringArray(R.array.category);
        app().prefs = new PreferencesHelper();
        firestore = FirebaseFirestore.getInstance();
        setView();
        getAllData();
        updateMyProfile();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    loadFragment(Tag.HOME);
                    return true;
                case R.id.navigation_daftar:
                     //jika sudah login dan data diri lengkap, alihkan ke tampilan personal ads
                if (app().account != null &&  app().prefs.getUsername().length() >= 4
                        && app().prefs.getEducation().length() >= 3 && !app().prefs.getImage().isEmpty()
                        && (Utils.phoneNumberChecker(app().prefs.getPhone())) && app().prefs.getAddress().length() > 4
                        && app().prefs.isActive())
                    loadFragment(Tag.ADS);
                else {
                    if (app().account == null) //jika belum login
                        showInfo(getResources().getString(R.string.info_need_login_to_add));
                    else // jika akun disuspend atau biodata kurang lengkap
                        showInfo(getResources().getString(
                            !app().prefs.isActive() ? R.string.info_banned : R.string.info_need_complete_bio));
                    loadFragment(Tag.USER);
                }
                    return true;
                case R.id.navigation_profil:
                    loadFragment(Tag.USER);
                    return true;

                case R.id.navigation_pesanan:
                    if (app().account != null)
                    loadFragment(Tag.CHOOSEPESANAN);
                    else {
                        showInfo(getResources().getString(R.string.info_need_login_to_add));
                    }
                    return true;

            }
            return false;
        }
    };

    /**
     * Init semua variable /  widget yang berkaitan dengan view di sini
     * **/
    private void setView() {

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        filterLayout = findViewById(R.id.filter_layout);
        sortingLayout  = findViewById(R.id.sorting_layout);
        categoryLayout = findViewById(R.id.category_layout);
        searchLayout   = findViewById(R.id.search_layout);
        clearSearch    = findViewById(R.id.ads_search_clear);

        searchList   = findViewById(R.id.search_list);
        searchList.setLayoutManager(new LinearLayoutManager(this));
        searchList.setItemAnimator(new DefaultItemAnimator());

        // init main list di home
        // tambahkan listener ketika di klik dan scroll
        mainList = findViewById(R.id.ads_list);
        mainList.addItemDecoration(new GridDecoration(2, dpToPx(), true));
        mainList.setItemAnimator(new DefaultItemAnimator());
        mainList.setLayoutManager(new GridLayoutManager(this, 2));
        mainList.addOnItemTouchListener(new RecyclerViewListener(this, mainList,this));
        mainList.addOnScrollListener(new ScrollListener());

        // set category sesuai pilihan user yang tersimpan di preference
        RadioGroup rg = findViewById(R.id.category_group);
        rg.check(getCategory(app().prefs.getCategory()));

        // fungsi agar bisa di klik, check method nya di onClick(View v)
//        findViewById(R.id.btn_home).setOnClickListener(this);
//        findViewById(R.id.btn_user).setOnClickListener(this);
//        findViewById(R.id.btn_ads).setOnClickListener(this);
        findViewById(R.id.filter_sort).setOnClickListener(this);
        findViewById(R.id.ads_search_clear).setOnClickListener(this);
        findViewById(R.id.filter_category).setOnClickListener(this);
        findViewById(R.id.sorting_new).setOnClickListener(this);
        findViewById(R.id.sorting_name_az).setOnClickListener(this);
        findViewById(R.id.sorting_name_za).setOnClickListener(this);
        findViewById(R.id.sorting_new).setOnClickListener(this);
        findViewById(R.id.sorting_old).setOnClickListener(this);
        findViewById(R.id.sorting_most_expensive).setOnClickListener(this);
        findViewById(R.id.sorting_ceapest).setOnClickListener(this);
        findViewById(R.id.sort_close).setOnClickListener(this);
        findViewById(R.id.category_close).setOnClickListener(this);
        findViewById(R.id.sorting_layout).setOnClickListener(this);
        findViewById(R.id.category_layout).setOnClickListener(this);
        findViewById(R.id.search_layout).setOnClickListener(this);
        findViewById(R.id.rb1).setOnClickListener(this);
        findViewById(R.id.rb2).setOnClickListener(this);
        findViewById(R.id.rb3).setOnClickListener(this);
        findViewById(R.id.rb4).setOnClickListener(this);
        findViewById(R.id.rb5).setOnClickListener(this);

        // set tombol ke home, dan set sortir sesuai pilihan user yang tersimpan di preference
        setButtonState(0);
        setSortButtonState(app().prefs.getSort());

        // listener untuk mendeteksi perubahan di kolom search
        search = findViewById(R.id.ads_search);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // munculkan tombol clear search jika kolom search tidak kosong
                clearSearch.setVisibility(s.toString().isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // fungsi ketika kita menekan tombol search
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String input = search.getText().toString();
                    if (input.isEmpty()) // jika text kosong
                        return false;

                    // jika ada, clear model
                    searchData.clear();
                    // cari data yang judul nya mirip dengan yang dicari
                    for (Model model : allData) {
                        if (model.getTitle().toLowerCase().contains(input.toLowerCase()))
                            searchData.add(model);
                    }
                    // tambahakan ke adapter
                    searchList.setAdapter(new SearchAdapter(app().main, searchData));
                    // tambahkan fungsi agar list bisa di klik
                    searchList.addOnItemTouchListener(new RecyclerViewListener(app().main, searchList, new RecyclerViewListener.OnItemClick() {
                        @Override
                        public void onItemClick(View view, int position) {
                            // jika di klik, isi model sesuai data item yang di pilih, kemudian buka detail donasi
                            app().model = searchData.get(position);
                            startActivity(new Intent(app().main, DetailActivity.class));
                        }
                    }));
                    // jika data kosong, tampilkan layout "Pencarian tidak ditemukan"
                    findViewById(R.id.search_info).setVisibility(searchData.size() == 0 ? View.VISIBLE : View.GONE);
                    // munculkan tampilan search, tanpa ini layout nya akan tetap invisible
                    searchLayout.setVisibility(View.VISIBLE);
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Ketika semua object yang di beri .setOnClickListener(this); di klik
     * */
    @Override
    public void onClick(View v) {
        // cek id nya
        switch (v.getId()) {
//            case R.id.btn_home:
//                loadFragment(Tag.HOME);
//                break;
//            case R.id.btn_user:
//                loadFragment(Tag.USER);
//                break;
//            case R.id.btn_ads:
//                // jika sudah login dan data diri lengkap, alihkan ke tampilan personal ads
//                if (app().account != null &&  app().prefs.getUsername().length() >= 4
//                        && app().prefs.getEducation().length() >= 3 && !app().prefs.getImage().isEmpty()
//                        && (Utils.phoneNumberChecker(app().prefs.getPhone())) && app().prefs.getAddress().length() > 4
//                        && app().prefs.isActive())
//                    loadFragment(Tag.ADS);
//                else {
//                    if (app().account == null) //jika belum login
//                        showInfo(getResources().getString(R.string.info_need_login_to_add));
//                    else // jika akun disuspend atau biodata kurang lengkap
//                        showInfo(getResources().getString(
//                            !app().prefs.isActive() ? R.string.info_banned : R.string.info_need_complete_bio));
//                    loadFragment(Tag.USER);
//                }
//                break;
            case R.id.ads_search_clear: // tombol X untuk clear search
                search.setText("");
                searchLayout.setVisibility(View.GONE);
                break;
            case R.id.filter_sort: // buka sortir
                sortingLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.filter_category: // buka category
                categoryLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.sort_close: // tutup sortir
                sortingLayout.setVisibility(View.GONE);
                break;
            case R.id.category_close: // tutup category
                categoryLayout.setVisibility(View.GONE);
                break;
            case R.id.sorting_name_az:
                sort(0);
                break;
            case R.id.sorting_name_za:
                sort(1);
                break;
            case R.id.sorting_new:
                sort(2);
                break;
            case R.id.sorting_old:
                sort(3);
                break;
            case R.id.sorting_most_expensive:
                sort(4);
                break;
            case R.id.sorting_ceapest:
                sort(5);
                break;
            case R.id.rb1: // rb1 - rb5, untuk sortir
                setCategory(0);
                break;
            case R.id.rb2:
                setCategory(1);
                break;
            case R.id.rb3:
                setCategory(2);
                break;
            case R.id.rb4:
                setCategory(3);
                break;
            case R.id.rb5:
                setCategory(4);
                break;
        }
    }

    /**
     * Fungsi onClick pada List di home
     * **/
    @Override
    public void onItemClick(View view, int position) {
        // ambil data item yang dipilih, lalu buka detailnya
        app().model = allData.get(position);
        startActivity(new Intent(this, DetailActivity.class));
    }

    // Tampilkan atau load fragment user profile dan personal ads
    public void loadFragment(Tag tag) {
        // jika tombol yang diklik adalah tampilan yang aktif saat ini, return
        if (getSupportFragmentManager().findFragmentByTag(tag.toString()) != null)
            return;

        // set tag dan netralkan semua class fragment
        mTag = tag;
        myProfile = null;
        myAds = null;
        myPesanan = null;
        choosePesanan = null;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up); // animasi popup
                Fragment fragment;
                // selain tombol home, load
                if (mTag != Tag.HOME) {
                    switch (mTag) {
                        case USER:
                            setButtonState(2);
                            fragment = myProfile = new UserProfile();
                            break;
                        case CHOOSEPESANAN:
                            setButtonState(2);
                            fragment = choosePesanan = new FragmentChoosePesanan();
                            break;
                        default:
                            setButtonState(1); // rubah juga warna tombol nya
                            fragment = myAds = new MyAds();
                    }
                    fragmentTransaction.replace(R.id.fragment_container, fragment, mTag.toString());
                    fragmentTransaction.commitAllowingStateLoss();
                } else if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) != null) { // jika tombol home, hapus fragment
                    fragmentTransaction.
                            remove(Objects.requireNonNull(
                                    getSupportFragmentManager().findFragmentById(R.id.fragment_container))).commit();
                    setButtonState(0);
                }
            }
        });
    }

    /**
     * merubah warna tombol, jika sesuai index yang di pilih, maka akan berwarna abu"
     * */
    private void setButtonState(int index) {
       /* findViewById(R.id.btn_home).setBackgroundColor(
                getResources().getColor(index == 0 ? R.color.grey_transparent : R.color.transparent));
        findViewById(R.id.btn_ads).setBackgroundColor(
                getResources().getColor(index == 1 ? R.color.grey_transparent : R.color.transparent));
        findViewById(R.id.btn_user).setBackgroundColor(
                getResources().getColor(index == 2 ? R.color.grey_transparent : R.color.transparent));*/
    }

    /**
     * Fungsi kategori ketika salah satu tombol sortir di klik
     * */
    private void setCategory(int index) {
        categoryLayout.setVisibility(View.GONE);
        if (index == app().prefs.getCategory())
            return;

        app().prefs.setCategory(index);
        getAllData();
    }

    /**
     * Fungsi sortir, konsep nya sama dengan fungsi kategori di atas
     * */
    private void sort(int index) {
        sortingLayout.setVisibility(View.GONE);
        if (index == app().prefs.getSort())
            return;

        setSortButtonState(index);
        app().prefs.setSort(index);
        getAllData();
    }

    /**
     * merubah warna tombol, jika sesuai index yang di pilih, maka akan berwarna abu"
     */
    private void setSortButtonState(int index) {
        findViewById(R.id.sorting_name_az).setBackgroundColor(
                getResources().getColor(index == 0 ? R.color.grey_light : R.color.transparent));
        findViewById(R.id.sorting_name_za).setBackgroundColor(
                getResources().getColor(index == 1 ? R.color.grey_light : R.color.transparent));
        findViewById(R.id.sorting_new).setBackgroundColor(
                getResources().getColor(index == 2 ? R.color.grey_light : R.color.transparent));
        findViewById(R.id.sorting_old).setBackgroundColor(
                getResources().getColor(index == 3 ? R.color.grey_light : R.color.transparent));
        findViewById(R.id.sorting_most_expensive).setBackgroundColor(
                getResources().getColor(index == 4 ? R.color.grey_light : R.color.transparent));
        findViewById(R.id.sorting_ceapest).setBackgroundColor(
                getResources().getColor(index == 5 ? R.color.grey_light : R.color.transparent));
    }

    /**
     * Cek tombol yang di pilih user sebelumnya, berdasarkan index yang tersimpan di preference
     * hanya dipanggil satu kali diawal, ketika aplikasi dibuka
     * */
    private int getCategory(int index) {
        switch (index) {
            case 0:
                return R.id.rb1;
            case 1:
                return R.id.rb2;
            case 2:
                return R.id.rb3;
            case 3:
                return R.id.rb4;
            default:
                return R.id.rb5;
        }
    }

    /**
     * Untuk memunculkan notifikasi, pop up hitam di bawah layar (Snackbar)
     * */
    public void showInfo(String info) {
        Snackbar.make(findViewById(R.id.coordinator), info, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Listener pendeteksi scroll di list view. Untuk show/hide tombol filter (sortir & kategori)
     * **/
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
                filterLayout.setVisibility(hide ? View.GONE : View.VISIBLE);
            }
        }
    }

    /**
     * Mengambil semua data ads di firestore
     * data akan di refresh otomatis setiap ada perubahan data di server
     * **/
    private void getAllData() {
        // netralkan terlebih dahulu listener yang akan dipakai, untuk menghidari bug (listener numpuk)
        removeEvent();
        // tentukan path data ads di firestore
        // collection untuk banyak data, sedangkan document 1 data
        // jadi path dibawah bisa diartikan, ambil list data dari collection Ads, di dalam document Teacher, di dalam com.goteacher
        // com.goteacher/Teacher/Ads
        CollectionReference colAds = firestore.collection(app().packageName).document(app().dbName)
                .collection(app().Ads);
        int category = app().prefs.getCategory();
        if (category == 0) // jika kategory nya semua
            firestoreListener = colAds.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) // jika error
                                return;
                            refreshList(queryDocumentSnapshots); // jika tidak error, refresh/reload data dari firestore
                        }
                    });
        else // jika bukan semua, tambahkan parameter kategori yang dipilih ke whereEqualTo
            firestoreListener = colAds
                    .whereArrayContains(Model.key.category.name(), app().category[category])
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null)
                                return;
                            refreshList(queryDocumentSnapshots);
                        }
                    });
    }

    /**
     * Fungsi untuk mereload tampilan list di home
     * Hanya dipanggil oleh getAllData
     * */
    private synchronized void refreshList(QuerySnapshot queryDocumentSnapshots) {
        allData.clear();
        if (queryDocumentSnapshots != null) { // jika QuerySnapshot dari getAllData tidak error
            allData = Utils.createDataModel(queryDocumentSnapshots); // buat modelnya
            allData = Utils.sortData(allData, app().prefs.getSort()); // sortir sesuai pilihan user
        }
        if (adapter == null) { // jika list baru di load pertama kali
            adapter = new Adapter(this, allData);
            mainList.setAdapter(adapter); // set adapter dan pasang ke list
        } else // jika adapter sudah di set sebelumnya, cukup meng-update adapter
            adapter.updateList(allData);
    }

    /***
     * Sama seperti getAllData,
     * tapi fungsi ini hanya mengambil data ads yang di buat sendiri oleh user (personal, based on creator email)
     * data akan di refresh otomatis setiap ada perubahan data dari personal ads di server
     * */
    private void getPersonalData() {
        firestore.collection(app().packageName).document(app().dbName)
                .collection(app().Ads)
                .whereEqualTo(Model.key.creator.name(), Objects.requireNonNull(app().account.getEmail()))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null)
                            return;
                        assert queryDocumentSnapshots != null;
                        myData = Utils.createDataModel(queryDocumentSnapshots);
                        if (myAds != null)
                            myAds.refreshList(myData);
                    }
                });
    }

    /**
     * Fungsi yang dipanggil saat tampilan fragment personal ads di buka
     * sehinggal tidak perlu memanggil data dari firestore setiap personal ads di buka
     * myData akan ter-update otomatis oleh getPersonalData
     */
    public ArrayList<Model> getMyData() {
        return myData;
    }

    /***
     * Sama seperti getAllData,
     * tapi fungsi ini hanya mengambil data profile dari user (based on creator email)
     * data akan di refresh otomatis setiap ada perubahan data profile di server
     * */
    public void updateMyProfile() {
        if (app().account == null)
            return;

        getPersonalData();

        docUser = firestore
                .collection(app().packageName).document(app().dbName)
                .collection(app().User).document(Objects.requireNonNull(app().account.getEmail()));
        docUser.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null || app().account == null)
                    return;
                try {
                    if (snapshot != null && snapshot.exists()) { // jika user sudah terdaftar di server
                        // masukkan data ke prefrence, lalu update tampilan profile
                        String phoneNumber = Objects.requireNonNull(snapshot).getString(UserModel.key.phone.name());
                        String occupation = Objects.requireNonNull(snapshot).getString(UserModel.key.occupation.name());
                        String education = Objects.requireNonNull(snapshot).getString(UserModel.key.education.name());
                        String name = Objects.requireNonNull(snapshot).getString(UserModel.key.name.name());
                        String address = Objects.requireNonNull(snapshot).getString(UserModel.key.address.name());
                        String img  = Objects.requireNonNull(snapshot).getString(UserModel.key.imgURL.name());
                        Boolean admin = Objects.requireNonNull(snapshot).getBoolean(UserModel.key.admin.name());
                        Boolean active = Objects.requireNonNull(snapshot).getBoolean(UserModel.key.active.name());
                        Boolean gender = Objects.requireNonNull(snapshot).getBoolean(UserModel.key.gender.name());

                        app().prefs.setUsername(name != null ? name : "");
                        app().prefs.setAddress(address != null ? address : "");
                        app().prefs.setEducation(education != null ? education : "");
                        app().prefs.setPhone(phoneNumber != null ? phoneNumber : "");
                        app().prefs.setOccupation(occupation != null ? occupation : "");
                        app().prefs.setAdmin(admin != null && admin);
                        app().prefs.setGender(gender != null && gender);
                        app().prefs.setActive(active != null && active);
                        app().prefs.setImage(img != null ? img : "");

                        if (myProfile != null)
                            myProfile.updateUI();
                    } else // jika belum terdaftar, tambahkan
                        addUser();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    /***
     * Sama seperti getAllData,
     * tapi fungsi ini hanya mengambil riwayat donasi user
     * data akan di refresh otomatis setiap ada perubahan data profile
     * */
    private void addUser() {
        if (app().account == null)
            return;

        String name = app().account.getDisplayName() != null && !app().account.getDisplayName().isEmpty() ?
                app().account.getDisplayName() : "";

        Map<String, Object> data = new HashMap<>();
        data.put(UserModel.key.name.name(), name);
        data.put(UserModel.key.email.name(), Objects.requireNonNull(app().account.getEmail()));
        data.put(UserModel.key.phone.name(), "");
        data.put(UserModel.key.occupation.name(), "");
        data.put(UserModel.key.education.name(), "");
        data.put(UserModel.key.address.name(), "");
        data.put(UserModel.key.imgURL.name(), app().account.getPhotoUrl() != null ?
                Objects.requireNonNull(app().account.getPhotoUrl()).toString() : "");
        data.put(UserModel.key.gender.name(), true);
        data.put(UserModel.key.admin.name(), app().account.getEmail().equalsIgnoreCase(app().adminEmail));
        data.put(UserModel.key.active.name(), true);
        data.put(UserModel.key.created.name(), System.currentTimeMillis());
        docUser.set(data).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showInfo(getResources().getString(R.string.info_connection_failure));
            }
        });
    }

    /**
     * Mengupdate data user, nama, pendidikan, pekerjaan, dll
     * */
    public void updateDataUser() {
        Map<String, Object> data = new HashMap<>();
        data.put(UserModel.key.name.name(), app().prefs.getUsername());
        data.put(UserModel.key.occupation.name(), app().prefs.getOccupation());
        data.put(UserModel.key.education.name(), app().prefs.getEducation());
        data.put(UserModel.key.address.name(), app().prefs.getAddress());
        data.put(UserModel.key.phone.name(), app().prefs.getPhone());
        data.put(UserModel.key.gender.name(), app().prefs.getGender());
        data.put(UserModel.key.imgURL.name(), app().prefs.getImage());
        docUser.update(data).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showInfo(getResources().getString(R.string.info_connection_failure));
            }
        });
    }

    /**
     * Untuk menghapus listener dari firestore
     *
     * Digunakan setiap kita memanggil getAllData, agar listener tidak saling tumpuk dan nge-bug
     * jadi pastikan listener dalam keaadaan netral, sebelum di gunakan
     * */
    private void removeEvent() {
        if (firestoreListener != null) {
            firestoreListener.remove();
            firestoreListener = null;
        }
    }

    /**
     * Dipanggil ketika user menggunakan fungsi" activity dari android
     * seperti login, kamera, file manager, permission(ijin), dst
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        app().permission.onRequestCallBack(requestCode, permissions, grantResults);
    }

    /**
     * Fungsi tombol back
     * */
    @Override
    public void onBackPressed() {
        if (mTag != Tag.HOME) // jika fragment yang terbuka, hapus fragment, kembali ke home
            loadFragment(Tag.HOME);
        else if (searchLayout.getVisibility() == View.VISIBLE) { // jika tampilan search, hide
            search.setText("");
            searchLayout.setVisibility(View.GONE);
        }
        else if ((System.currentTimeMillis() - exitTime) <= 3500) // jika sudah di home, dan tombol back di tekan 2x dalam waktu kurang dari 3.5s
            super.onBackPressed();
        else { // jika user menekan tombol back, keluarkan warning
            exitTime = System.currentTimeMillis();
            showInfo(getString(R.string.info_press_to_exit));
        }
    }
}
