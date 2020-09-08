package com.example.smsblocker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SMS_Adapter extends RecyclerView.Adapter<SMS_Adapter.SMS_View> {
    ArrayList<SMS_Model> messages;

    public SMS_Adapter(ArrayList<SMS_Model> names) {
        this.messages = names;
    }

    @NonNull
    @Override
    public SMS_View onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = (Context) parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.sms_list_row, parent, false);
        return new SMS_View(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SMS_View holder, int position) {
        holder.onBind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SMS_View extends RecyclerView.ViewHolder {

        TextView text_address;
        TextView text_msgBody;
        TextView text_date;

        public SMS_View(@NonNull View itemView) {
            super(itemView);
            text_address = (TextView) itemView.findViewById(R.id.txt_address);
            text_msgBody = (TextView) itemView.findViewById(R.id.txt_msg);
            text_date = (TextView) itemView.findViewById(R.id.txt_date);
        }

        public void onBind(SMS_Model sms) {
            text_address.setText(sms.getAddress());
            text_msgBody.setText(sms.getMsg());
            text_date.setText(sms.getDate());
        }
    }
}
