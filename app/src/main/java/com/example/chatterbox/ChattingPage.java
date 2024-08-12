package com.example.chatterbox;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatterbox.Models.MessageModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChattingPage extends AppCompatActivity {
    //O means other person the current User is chatting with
    //C means the current user
    TextView oprofilename;
    CircleImageView oprofilePic;
    RecyclerView chatmessages;
    EditText messagearea;
    FloatingActionButton sendbutton;
    String cusername, ousername;

    FirebaseDatabase database;
    DatabaseReference reference;

    MessageAdapter adapter;
    List<MessageModel> messageModelList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chatting_page);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        oprofilename = findViewById(R.id.otherUserName);
        oprofilePic = findViewById(R.id.otherprofilePic);
        chatmessages = findViewById(R.id.ChatArea);
        messagearea = findViewById(R.id.messageArea);
        sendbutton = findViewById(R.id.sendButton);

        Picasso.get().load(getIntent().getStringExtra("oprofilepic")).into(oprofilePic);


        cusername = getIntent().getStringExtra("username").replace(".com", "");
        ousername = getIntent().getStringExtra("otheruser").replace(".com", "");

        oprofilename.setText(ousername);

        chatmessages.setLayoutManager(new LinearLayoutManager(this));
        messageModelList = new ArrayList<>();

        sendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messagearea.getText().toString();
                if(!message.isEmpty()){
                    sendMessageFunction(message.strip());
                    messagearea.setText("");
                }
            }
        });
        
        getMessages();

//        oprofilePic.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Bundle bundle = new Bundle();
//                FragmentManager fragmentManager = getSupportFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                ViewOtherProfile viewOtherProfile = new ViewOtherProfile();
//                bundle.putString("oprofilepic", getIntent().getStringExtra("oprofilepic"));
//                viewOtherProfile.setArguments(bundle);
//                fragmentTransaction.add(R.id.fragmentContainerView, viewOtherProfile);
//                fragmentTransaction.commit();
//            }
//        });
    }

    private void getMessages() {
        reference.child("Messages").child(cusername).child(ousername)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        MessageModel messageModel = snapshot.getValue(MessageModel.class);
                        messageModelList.add(messageModel);
                        adapter.notifyDataSetChanged();
                        chatmessages.scrollToPosition(messageModelList.size()-1);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        adapter = new MessageAdapter(messageModelList, cusername);
        chatmessages.setAdapter(adapter);
    }

    private void sendMessageFunction(String message) {
        String key = reference.child("Messages").child(cusername).child(ousername).push().getKey();
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("message", message);
        messageMap.put("from", cusername);
        reference.child("Messages").child(cusername).child(ousername).child(key).setValue(messageMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            reference.child("Messages").child(ousername).child(cusername).child(key).setValue(messageMap);
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}