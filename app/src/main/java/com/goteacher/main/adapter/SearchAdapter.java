package com.goteacher.main.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.goteacher.R;
import com.goteacher.utils.model.Model;

import java.util.ArrayList;

/**
 * Created by raya on 09/06/17.
 */

public class SearchAdapter extends
        RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private ArrayList<Model> mData;
    private LayoutInflater mInflater;

    public SearchAdapter(Context context, ArrayList<Model> data) {
        mInflater = LayoutInflater.from(context);
        mData = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(
                R.layout.main_search_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Model model = mData.get(position);

        holder.title.setText(model.getTitle());

        StringBuilder category = new StringBuilder();
        for (String value : model.getCategory())
            category.append((category.length() == 0) ? value : ", " + value);
        holder.category.setText(category.toString());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView title, category;

        private ViewHolder(View v) {
            super(v);
            title    = v.findViewById(R.id.search_title);
            category = v.findViewById(R.id.search_category);
        }
    }
}
