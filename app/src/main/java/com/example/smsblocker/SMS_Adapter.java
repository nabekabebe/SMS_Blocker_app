package com.example.smsblocker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class SMS_Adapter  extends RecyclerView.Adapter<SMS_Adapter.SMS_View>{
    ArrayList<String> names;

    public SMS_Adapter(ArrayList<String> names) {
        this.names = names;
    }

    @NonNull
    @Override
    public SMS_View onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = (Context) parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.sms_list_row, parent,false);
        return new SMS_View(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SMS_View holder, int position) {
        holder.onBind(names.get(position));
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public class SMS_View extends RecyclerView.ViewHolder{

        TextView title_view;
        public SMS_View(@NonNull View itemView) {
            super(itemView);
            title_view = (TextView) itemView.findViewById(R.id.title_txt);
        }

        public void onBind(String title){
            title_view.setText(title);
        }
    }
}
