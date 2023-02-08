package com.example.xiao7demo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        // viewHolder.button.setText(firstData.get(position).getName());
        // 为设置中心添加清理缓存功能
        /*if ("设置中心".equals(firstData.get(position).getName())) {
            viewHolder.button.setOnClickListener(view -> {
                mContext.getSharedPreferences("data", Context.MODE_PRIVATE).edit().clear().apply();
                // mContext.deleteSharedPreferences("data");
                // 清理缓存退出应用
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setTitle("退出应用");
                dialog.setMessage("缓存已清理，请重启应用！");
                dialog.setCancelable(false);
                dialog.setPositiveButton("确定", (dialog1, which) -> {
                    MyApplication.getInstance().exit();
                });
                dialog.show();
            });
        } else {
            viewHolder.button.setOnClickListener(view -> {
                Toast.makeText(mContext, firstData.get(position).getActivity(), Toast.LENGTH_SHORT).show();
            });
        }*/
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
