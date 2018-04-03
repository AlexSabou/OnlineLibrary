package com.example.ali.biblioteca.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ali.biblioteca.R;
import com.example.ali.biblioteca.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPass;
    private Button btnLogin;
    private TextView tvRegister;
    private FirebaseAuth mAuth;
    private FirebaseUser fbUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loadElements();
        mAuth = FirebaseAuth.getInstance();
        fbUser = mAuth.getCurrentUser();
        if(fbUser != null) {
            Intent goToHome = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(goToHome);
        }
    }

    private void loadElements() {
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPass = (EditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        tvRegister = (TextView) findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareLogin();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToRegister = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(goToRegister);
            }
        });
    }

    private void prepareLogin() {
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        if(email.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please insert your email.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(pass.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please insert your password.", Toast.LENGTH_SHORT).show();
            return;
        }
        loginUser(email, pass);
    }

    private void loginUser(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    fbUser = mAuth.getCurrentUser();
                    if(fbUser != null) {
                        FirebaseDatabase.getInstance().getReference("user").child(fbUser.getUid()).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                if(user != null) {
                                    if(user.isBlocked())
                                        Toast.makeText(LoginActivity.this, "Your account is blocked.", Toast.LENGTH_SHORT).show();
                                    else {
                                        Toast.makeText(getApplicationContext(), "Login successful: " + fbUser.getDisplayName(), Toast.LENGTH_SHORT).show();
                                        Intent goToHome = new Intent(LoginActivity.this, HomeActivity.class);
                                        startActivity(goToHome);
                                    }
                                }
                                else {
                                    Toast.makeText(LoginActivity.this, "Your account doesn't exist.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
                else {
                    Exception ex = task.getException();
                    String errorMessage = ex.getMessage().toString();
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
