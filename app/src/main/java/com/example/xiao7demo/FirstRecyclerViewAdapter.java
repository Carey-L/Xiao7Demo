package com.example.xiao7demo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;

import java.util.ArrayList;
import java.util.List;

public class FirstRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<FirstData> firstData = new ArrayList<>();

    private final Context mContext;

    public FirstRecyclerViewAdapter(Context context, List<FirstData> listData) {
        this.mContext = context;
        firstData.addAll(listData);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh(List<FirstData> listData) {
        firstData.clear();
        this.firstData.addAll(listData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FirstViewHolder(LayoutInflater.from(mContext).inflate(R.layout.lws_first_view_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FirstViewHolder viewHolder = (FirstViewHolder) holder;
        FirstData data = firstData.get(position);
        viewHolder.view.setOnClickListener(v -> {
            if ("切换语言".equals(data.getName())) {
                new AlertDialog.Builder(mContext)
                        .setTitle("切换语言")
                        .setMessage("确定要切换语言吗？")
                        .setPositiveButton("确定", (dialog, which) -> {
                            // 存储用户选择的语言
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                            SharedPreferences.Editor editor = preferences.edit();
                            if ("zh_CN".equals(preferences.getString("language", "zh_CN"))) {
                                editor.putString("language", "en").commit();
                            } else {
                                editor.putString("language", "zh_CN").commit();
                            }
                            Toast.makeText(mContext, "切换语言成功", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .setNegativeButton("取消", (dialog, which) -> {
                            Toast.makeText(mContext, "切换语言取消", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.last_month), Toast.LENGTH_SHORT).show();
            }
        });
        ((FirstViewHolder) holder).textView.setText(firstData.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return firstData.size();
    }

    static class FirstViewHolder extends RecyclerView.ViewHolder {

        View view;
        ImageView imageView;
        TextView textView;

        public FirstViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view = itemView;
            imageView = itemView.findViewById(R.id.first_view_tool_image);
            textView = itemView.findViewById(R.id.first_view_tool_name);
        }
    }
}
