package com.goteacher.main.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class PersonalAdapter extends
        RecyclerView.Adapter<PersonalAdapter.ViewHolder> {

    private ArrayList<Model> mData;
    private LayoutInflater mInflater;
    private Context mContext;
    private Listener listener;

    public PersonalAdapter(Context context, ArrayList<Model> data, Listener _listener) {
        mInflater = LayoutInflater.from(context);
        mData = data;
        mContext = context;
        listener = _listener;
    }

    public void updateList(ArrayList<Model> data) {
        mData = data;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(
                R.layout.main_personal_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Model model = mData.get(position);

        holder.title.setText(model.getTitle());
        holder.rates.setText(getCurrency(model.getRates()));
        Glide.with(mContext).load(model.getImgURL()).into(holder.thumbnail);

        final int pos = position;
        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onView(pos);
            }
        });

        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onView(pos);
            }
        });

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDelete(pos);
            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEdit(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView title, rates;
        private ImageView thumbnail;
        private Button remove, edit;

        private ViewHolder(View v) {
            super(v);
            title     = v.findViewById(R.id.ads_title);
            rates = v.findViewById(R.id.ads_rates);
            thumbnail = v.findViewById(R.id.ads_image);
            remove = v.findViewById(R.id.ads_remove);
            edit = v.findViewById(R.id.ads_edit);
        }
    }

    public interface Listener {

        void onView(int pos);

        void onEdit(int pos);

        void onDelete(int pos);
    }


}
