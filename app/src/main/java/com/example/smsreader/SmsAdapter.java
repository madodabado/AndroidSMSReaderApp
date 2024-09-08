package com.example.smsreader;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.SmsViewHolder> {

    private final List<SmsMessage> smsMessages;
    private final MainActivity activity;

    public SmsAdapter(List<SmsMessage> smsMessages, MainActivity activity) {
        this.smsMessages = smsMessages;
        this.activity = activity;
    }

    @NonNull
    @Override
    public SmsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sms_item, parent, false);
        return new SmsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SmsViewHolder holder, int position) {
        SmsMessage smsMessage = smsMessages.get(position);
        holder.messageBody.setText(smsMessage.getBody());
        holder.messageDate.setText(smsMessage.getDate());

        holder.deleteButton.setOnClickListener(v -> activity.deleteSms(smsMessage.getId()));
    }

    @Override
    public int getItemCount() {
        return smsMessages.size();
    }

    static class SmsViewHolder extends RecyclerView.ViewHolder {
        TextView messageBody;
        TextView messageDate;
        Button deleteButton;

        SmsViewHolder(View itemView) {
            super(itemView);
            messageBody = itemView.findViewById(R.id.messageBody);
            messageDate = itemView.findViewById(R.id.messageDate);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
