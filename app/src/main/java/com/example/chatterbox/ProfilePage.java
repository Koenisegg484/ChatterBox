package com.example.chatterbox;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilePage extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseUser currentuser;
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseStorage storage;
    StorageReference storageReference;
    TextView emailfield, phonefield, bdayfield;
    CircleImageView profilePic;
    FloatingActionButton changeprofile;
    Uri imageuri;
    String profileuri;
    boolean prfc;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        currentuser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        emailfield = findViewById(R.id.emaillabel);
        phonefield = findViewById(R.id.phonelabel);
        bdayfield = findViewById(R.id.bdaylabel);
        profilePic = findViewById(R.id.circleImageView);
        changeprofile = findViewById(R.id.profileChange);
        prfc = false;

        //Getting user info from the database
        getUserInfo();

        //Functions for changing the profile image
        changeprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Deleteing current users last profile
                if(!profileuri.equals("null")){
                    deleteLastProfile(profileuri);
                }

                //Updating the new profile
                if (imageuri != null) {
                    UUID randomId = UUID.randomUUID();
                    String imageName = "images/" + randomId + ".jpg";
                    StorageReference newImageRef = storageReference.child(imageName);

                    // Upload the new profile image
                    newImageRef.putFile(imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            newImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String userProfilePath = uri.toString();
                                    reference.child("Users").child(auth.getUid()).child("ProfileImage").setValue(userProfilePath);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e("Profile", "Failed to upload new profile image", exception);
                        }
                    });

                    prfc = false;
                    Toast.makeText(ProfilePage.this, "Profile Pic Updated.", Toast.LENGTH_SHORT).show();
                    changeprofile.setVisibility(View.INVISIBLE);
                }
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeprofile.setVisibility(View.VISIBLE);
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null){
            imageuri = data.getData();
            Picasso.get().load(imageuri).into(profilePic);
        }
    }

    private void getUserInfo() {

        reference.child("Users")
                .child(currentuser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = snapshot.child("UserEmail").getValue().toString();
                        String phone = snapshot.child("Phone").getValue().toString();
                        String bday = snapshot.child("BirthDate").getValue().toString();
                        profileuri = snapshot.child("ProfileImage").getValue().toString();

                        emailfield.setText(email);
                        phonefield.setText(phone);
                        bdayfield.setText(bday);

                        if(profileuri == "null"){
                            profilePic.setImageResource(R.drawable.pirate);
                        }else {
                            Picasso.get().load(profileuri).into(profilePic);
                            Log.d("imgcheck", "Loaded Image" + profileuri);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "Error Retrieving user data.", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void deleteLastProfile(String currentURI){
        URL url = null;
        try {
            url = new URL(profileuri);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        String path = url.getPath();
        path = path.replaceFirst("/v0/b/simpychat-d338f.appspot.com/o/", "");
        try {
            path = URLDecoder.decode(path, "UTF-8"); // Decode the URL encoded part of the path
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        StorageReference mystref = storageReference.child(path);

        // Delete the profile image
        mystref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("Profile", "Old profile has been deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("Profile", "Failed to delete old profile image", exception);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}