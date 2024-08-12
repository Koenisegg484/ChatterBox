package com.example.chatterbox;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailfield, passfield;
    private Button loginButton;
    private TextView newAccount, forgotPassword;
    private ImageView welcome;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onStart() {
        super.onStart();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            Intent intent = new Intent(LoginActivity.this, HomePage.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();

        emailfield = findViewById(R.id.signupemail);
        passfield = findViewById(R.id.passwordfield);
        loginButton = findViewById(R.id.loginbutton);
        newAccount = findViewById(R.id.newAccount);
        forgotPassword = findViewById(R.id.forgotpass);
        welcome = findViewById(R.id.imageView);

        welcome.setImageResource(R.drawable.welcomebg);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String useremail = emailfield.getText().toString();
                String password = passfield.getText().toString();
                if(useremail.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please Enter an Email.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Password field was left Empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                loginFunction(useremail, password);
            }
        });


        //Forwarding to the new account creation activity
        newAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, SigninActivity.class);
                startActivity(i);
            }
        });
        // Forwarding to the forgot password activity
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, ForgotPassActivity.class);
                startActivity(i);
            }
        });
    }

    public void loginFunction(String email, String password){
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent i = new Intent(LoginActivity.this, HomePage.class);
                    startActivity(i);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Couldn't log in. "+email+password, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}