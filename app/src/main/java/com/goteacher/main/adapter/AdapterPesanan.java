package com.goteacher.main.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.goteacher.R;
import com.goteacher.admin.DetailPesananActivity;
import com.goteacher.utils.SharedVariable;
import com.goteacher.utils.model.Pesanan;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;


/**
 * Created by Ravi Tamada on 18/05/16.
 */
public class AdapterPesanan extends RecyclerView.Adapter<AdapterPesanan.MyViewHolder> {

    private Context mContext;
    private List<Pesanan> pesananList;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNamaLayanan,tvHarga;
        public ImageView imgLayanan;
        public Button btnBuktiBayar;
        public LinearLayout linePesanan;


        public MyViewHolder(View view) {
            super(view);
            tvNamaLayanan = view.findViewById(R.id.tvNamaLayanan);
            tvHarga= view.findViewById(R.id.tvHarga);
            imgLayanan = view.findViewById(R.id.ivLayanan);
            linePesanan = view.findViewById(R.id.linePesanan);
      //      btnBuktiBayar = view.findViewById(R.id.btnUploadBukti);


        }
    }

    public AdapterPesanan(Context mContext, List<Pesanan> pesananList) {
        this.mContext = mContext;
        this.pesananList = pesananList;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pesanan, parent,false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        if (pesananList.isEmpty()){

            Log.d("isi Layanan: ",""+pesananList.size());
        }else {

            Resources res = mContext.getResources();

            final Pesanan pesanan  = pesananList.get(position);

            holder.tvNamaLayanan.setText(pesanan.getTitleCourse());
            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.ENGLISH);
            Locale localeID = new Locale("in", "ID");
            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
            int harga = Integer.parseInt(pesanan.getHargaCourse());
            holder.tvHarga.setText(formatRupiah.format((double) harga));

            String buktiBayar = pesanan.getImgBuktiBayar();
            if (buktiBayar.equals("no")){
                holder.imgLayanan.setImageResource(R.drawable.belum_upload);
            }else {
                Glide.with(mContext).load(pesanan.getImgBuktiBayar())
                        //.fitCenter()
                        .into(holder.imgLayanan);
            }

            holder.linePesanan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SharedVariable.nama.equals("Admin")){
                        //Toast.makeText(mContext,"admin klik",Toast.LENGTH_SHORT).show();
                        Log.d("adapterpesanan:","admin klik");
                        Intent intent = new Intent(mContext, DetailPesananActivity.class);
                        intent.putExtra("isAdmin","true");
                        intent.putExtra("pesanan",pesanan);
                        mContext.startActivity(intent);
                    }else {
                        Log.d("adapterpesanan:","user klik");
                        Intent intent = new Intent(mContext, DetailPesananActivity.class);
                        intent.putExtra("isAdmin","false");
                        intent.putExtra("pesanan",pesanan);
                        mContext.startActivity(intent);
                    }
                }
            });



        }

    }


    @Override
    public int getItemCount() {
        //return namaWisata.length;
        return pesananList.size();
    }
}
