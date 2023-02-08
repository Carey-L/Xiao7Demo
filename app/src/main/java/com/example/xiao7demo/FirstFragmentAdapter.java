package com.example.xiao7demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;

import java.util.ArrayList;
import java.util.List;

public class FirstFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<FirstViewData> listData = new ArrayList<>();

    private final Context mContext;

    public FirstFragmentAdapter(Context context) {
        this.mContext = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh(List<FirstViewData> listData) {
        this.listData.clear();
        this.listData.addAll(listData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FirstViewHolder(LayoutInflater.from(mContext).inflate(R.layout.lws_first_fragment_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FirstViewHolder firstViewHolder = (FirstViewHolder) holder;
        firstViewHolder.textView.setText(listData.get(position).getName());
        firstViewHolder.button.setState(listData.get(position).getStatus());
        firstViewHolder.button.setOnClickListener(view -> {
            // 下载
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    static class FirstViewHolder extends RecyclerView.ViewHolder {

        View view;
        ImageView imageView;
        TextView textView;
        FirstItemButton button;

        public FirstViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            imageView = view.findViewById(R.id.first_item_imageview);
            textView = view.findViewById(R.id.first_item_textview);
            button = view.findViewById(R.id.first_item_mybutton);
        }
    }
}
