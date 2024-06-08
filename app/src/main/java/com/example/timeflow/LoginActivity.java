package com.example.timeflow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.timeflow.HelpersClass.Login;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private EditText signupEmail, signupPassword;
    private Button signupButton, loginButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        checkForExistingUser();
        setupButtonListeners();
    }

    private void initializeViews() {
        progressBar = findViewById(R.id.progressBar3);
        hideProgressBar();

        signupEmail = findViewById(R.id.email);
        signupPassword = findViewById(R.id.password);
        signupButton = findViewById(R.id.signupButton);
        loginButton = findViewById(R.id.loginButton);
    }

    private void checkForExistingUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userIdprefs = sharedPreferences.getString("userId", "");

        if (!userIdprefs.isEmpty()) {
            goToEventsActivity(userIdprefs);
        }
    }

    private void setupButtonListeners() {
        signupButton.setOnClickListener(view -> {
            if (validateEmail() && validatePassword()) {
                registerUser();
            }
        });

        loginButton.setOnClickListener(view -> {
            if (validateEmail() && validatePassword()) {
                loginUser();
            }
        });
    }

    private void registerUser() {
        showProgressBar();

        final String email = signupEmail.getText().toString();
        final String password = signupPassword.getText().toString();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        reference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(LoginActivity.this, "Email is already in use", Toast.LENGTH_SHORT).show();
                    hideProgressBar();
                } else {
                    createUser(reference, email, password);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToastAndHideProgressBar("Error querying the database");
            }
        });
    }

    private void createUser(DatabaseReference reference, String email, String password) {
        Login login = new Login(email, password);
        String userId = reference.push().getKey();
        reference.child(userId).setValue(login)
                .addOnSuccessListener(aVoid -> {
                    saveUserIdToPreferences(userId);
                    goToEventsActivity(userId);
                    showToastAndHideProgressBar("You have signed up successfully!");
                })
                .addOnFailureListener(e -> showToastAndHideProgressBar("Error signing up"));
    }

    private void loginUser() {
        showProgressBar();

        String userEmail = signupEmail.getText().toString().trim();
        String userPassword = signupPassword.getText().toString().trim();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("email").equalTo(userEmail);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    verifyUser(snapshot, userPassword);
                } else {
                    showToastAndHideProgressBar("User does not exist");
                    signupEmail.setError("User does not exist");
                    signupEmail.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToastAndHideProgressBar("Error querying the database");
            }
        });
    }

    private void verifyUser(DataSnapshot snapshot, String userPassword) {
        String userId = null;
        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
            userId = userSnapshot.getKey();
            break;
        }
        if (userId != null) {
            String passwordFromDB = snapshot.child(userId).child("password").getValue(String.class);
            if (passwordFromDB.equals(userPassword)) {
                saveUserIdToPreferences(userId);
                goToEventsActivity(userId);
                hideProgressBar();
            } else {
                showToastAndHideProgressBar("Invalid Credentials");
                signupPassword.setError("Invalid Credentials");
                signupPassword.requestFocus();
            }
        }
    }

    private void saveUserIdToPreferences(String userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", userId);
        editor.apply();
    }

    private void goToEventsActivity(String userId) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }

    private Boolean validatePassword() {
        String val = signupPassword.getText().toString().trim();
        if (val.isEmpty()) {
            signupPassword.setError("Password cannot be empty");
            return false;
        } else {
            signupPassword.setError(null);
            return true;
        }
    }

    private Boolean validateEmail() {
        String email = signupEmail.getText().toString().trim();
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        if (email.isEmpty()) {
            signupEmail.setError("Email cannot be empty");
            return false;
        } else if (!Pattern.compile(emailPattern).matcher(email).matches()) {
            signupEmail.setError("Invalid email format");
            return false;
        } else {
            signupEmail.setError(null);
            return true;
        }
    }

    private void showProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToastAndHideProgressBar(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
        hideProgressBar();
    }
}


