package com.example.chatterbox;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserChatAdapter extends RecyclerView.Adapter<UserChatAdapter.ViewHolder> {

    List<String> userList;
    String username;
    Context context;

    FirebaseDatabase database;
    DatabaseReference reference;

    public UserChatAdapter(List<String> userList, String username, Context context) {
        this.userList = userList;
        this.username = username;
        this.context = context;

        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView useremail, lastmessage;
        private CircleImageView userprofile;
        private CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            useremail = itemView.findViewById(R.id.username);
            lastmessage = itemView.findViewById(R.id.lastmessage);
            userprofile = itemView.findViewById(R.id.circleImageView2);
            cardView = itemView.findViewById(R.id.usercardview);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        reference.child("Users").child(userList.get(position)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String othername = snapshot.child("UserEmail").getValue().toString();
                String profileurl = snapshot.child("ProfileImage").getValue().toString();

                holder.useremail.setText(othername);
                if(!profileurl.equals("null")){
                    Picasso.get().load(profileurl).into(holder.userprofile);
                }else{
                    Picasso.get().load(R.drawable.pirate).into(holder.userprofile);
                }

                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ChattingPage.class);
                        intent.putExtra("username", username);
                        intent.putExtra("otheruser", othername);
                        intent.putExtra("oprofilepic", profileurl);
                        context.startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
