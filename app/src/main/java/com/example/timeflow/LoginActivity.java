package com.example.timeflow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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

    EditText signupEmail, signupPassword;
    Button signupButton, loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userIdprefs = sharedPreferences.getString("userId", "");

        if (!userIdprefs.isEmpty()) {
            goToEventsActivity(userIdprefs);
        }

        signupEmail = findViewById(R.id.email);
        signupPassword = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);

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
        final String email = signupEmail.getText().toString();
        final String password = signupPassword.getText().toString();

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        reference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(LoginActivity.this, "Email is already in use", Toast.LENGTH_SHORT).show();
                } else {
                    Login helperClass = new Login(email, password);
                    String userId = reference.push().getKey();
                    reference.child(userId).setValue(helperClass);

                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userId", userId);
                    editor.apply();

                    goToEventsActivity(userId);

                    Toast.makeText(LoginActivity.this, "You have signed up successfully!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Error querying the database", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loginUser() {
        String userEmail = signupEmail.getText().toString().trim();
        String userPassword = signupPassword.getText().toString().trim();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("email").equalTo(userEmail);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userId = null;
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        userId = userSnapshot.getKey();
                        break;
                    }
                    if (userId != null) {
                        String passwordFromDB = snapshot.child(userId).child("password").getValue(String.class);
                        if (passwordFromDB.equals(userPassword)) {
                            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("userId", userId);
                            editor.apply();

                            goToEventsActivity(userId);
                        } else {
                            signupPassword.setError("Invalid Credentials");
                            signupPassword.requestFocus();
                        }
                    }
                } else {
                    signupEmail.setError("User does not exist");
                    signupEmail.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Error querying the database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToEventsActivity(String userId) {
        Intent intent = new Intent(LoginActivity.this, EventsActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }

    public Boolean validatePassword() {
        String val = signupPassword.getText().toString().trim();

        if (val.isEmpty()) {
            signupPassword.setError("Password cannot be empty");
            return false;
        } else {
            signupPassword.setError(null);
            return true;
        }
    }

    public Boolean validateEmail() {
        String email = signupEmail.getText().toString().trim();
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);

        if (email.isEmpty()) {
            signupEmail.setError("Email cannot be empty");
            return false;
        } else if (!matcher.matches()) {
            signupEmail.setError("Invalid email format");
            return false;
        } else {
            signupEmail.setError(null);
            return true;
        }
    }
}

