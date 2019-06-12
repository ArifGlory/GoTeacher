package com.goteacher.main.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.goteacher.R;
import com.goteacher.utils.model.Model;

import java.util.ArrayList;

import static com.goteacher.utils.Utils.getCurrency;

/**
 * Created by raya on 09/06/17.
 */

public class Adapter extends
        RecyclerView.Adapter<Adapter.ViewHolder> {

    private ArrayList<Model> mData;
    private LayoutInflater mInflater;
    private Context mContext;

    public Adapter(Context context, ArrayList<Model> data) {
        mInflater = LayoutInflater.from(context);
        mData = data;
        mContext = context;
    }

    public void updateList(ArrayList<Model> data) {
        mData = data;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(
                R.layout.main_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Model model = mData.get(position);


        holder.title.setText(model.getTitle());
        holder.rates.setText(getCurrency(model.getRates()));
        Glide.with(mContext).load(model.getImgURL()).into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView title, rates;
        private ImageView thumbnail;

        private ViewHolder(View v) {
            super(v);
            title     = v.findViewById(R.id.ads_title);
            rates = v.findViewById(R.id.ads_rates);
            thumbnail = v.findViewById(R.id.ads_image);
        }
    }
}
