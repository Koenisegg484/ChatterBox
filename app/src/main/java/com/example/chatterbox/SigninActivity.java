package com.example.chatterbox;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatterbox.Models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SigninActivity extends AppCompatActivity {
    CircleImageView imageviewCircle;
    TextInputEditText emailfield, passfield, confirmpassfield, phonefield, birthdayfield;
    Button createAccount;
    TextView login;
    boolean imageflag;
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    Uri imageuri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Firebase objects intitialisation
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        imageviewCircle = findViewById(R.id.circleImageView);
        emailfield = findViewById(R.id.signupemail);
        phonefield = findViewById(R.id.signupphone);
        passfield = findViewById(R.id.signuppass);
        confirmpassfield = findViewById(R.id.signupconfirmpass);
        birthdayfield = findViewById(R.id.signupbirthdate);
        login = findViewById(R.id.login);
        createAccount = findViewById(R.id.createAccount);

        imageviewCircle.setImageResource(R.drawable.pirate);

        imageviewCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileImageChooser();
            }
        });

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String useremail = emailfield.getText().toString();
                String pass = passfield.getText().toString();
                String confirmpass = confirmpassfield.getText().toString();
                String phone = phonefield.getText().toString();
                String birthday = birthdayfield.getText().toString();

                if (validateInputs(useremail, pass, confirmpass, phone, birthday)) {
                    // Proceed with your logic if all inputs are valid
                    UserModel newUser = new UserModel(useremail, birthday, phone);
                    signupFunction(pass, newUser);

                }

            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SigninActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });
    }

    private void profileImageChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null){
            imageuri = data.getData();
            Picasso.get().load(imageuri).into(imageviewCircle);
            imageflag = true;
        }else{
            imageflag = false;
        }
    }

    private void signupFunction(String pass, UserModel user){
        String email = user.getEmail();
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    reference.child("Users").child(auth.getUid()).child("UserEmail").setValue(email);
                    reference.child("Users").child(auth.getUid()).child("Phone").setValue(user.getPhone());
                    reference.child("Users").child(auth.getUid()).child("BirthDate").setValue(user.getBirthDay());
                    //Check if user has a custom profile image
                    if(imageflag){
                        UUID randomId = UUID.randomUUID();
                        String imageName = "images/"+randomId+".jpg";
                        storageReference.child(imageName).putFile(imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                StorageReference mystorageReference = firebaseStorage.getReference(imageName);
                                mystorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String userProfilePath = uri.toString();
                                        reference.child("Users").child(auth.getUid()).child("ProfileImage").setValue(userProfilePath);
                                    }
                                });
                            }
                        });
                    }else{
                        reference.child("Users").child(auth.getUid()).child("ProfileImage").setValue("null");
                    }

                    Intent intent = new Intent(SigninActivity.this, LoginActivity.class);
                    //intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                }else {
                    Toast.makeText(getApplicationContext(), "Account could not be created, please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateInputs(String email, String password, String confirmPassword, String phone, String birthday) {
        if (!UserInputValidator.isValidEmail(email)) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!UserInputValidator.isValidPassword(password)) {
            Toast.makeText(this, "Password must be at least 8 characters long and contain    " + password.length(), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!UserInputValidator.doPasswordsMatch(password, confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Add additional checks for phone and birthday if needed
        if (phone.isEmpty() || phone.length() < 10) {
            Toast.makeText(this, "Invalid phone number"+phone+ "0l", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (birthday.isEmpty()) {
            Toast.makeText(this, "Birthday field cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}