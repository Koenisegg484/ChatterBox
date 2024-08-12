package com.example.chatterbox;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatterbox.Models.MessageModel;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    List<MessageModel> list;
    String username;
    boolean status;
    int sent;
    int recieved;

    public MessageAdapter(List<MessageModel> list, String username) {
        this.list = list;
        this.username = username;

        status = false;
        sent = 1;
        recieved = 2;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if(viewType == sent){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_send_card, parent,false);
        } else if (viewType == recieved) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_recieve_card, parent,false);
        }

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.textView.setText(list.get(position).getMessage());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            if (status){
                textView= itemView.findViewById(R.id.textViewSent);
            }else{
                textView= itemView.findViewById(R.id.textViewRecieve);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(list.get(position).getFrom().equals(username)){
            status = true;
            return  sent;
        }else {
            status = false;
            return recieved;
        }
    }
}
