package com.example.timeflow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EventsActivity extends AppCompatActivity {

    TextView userEmailTextView;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");

        if (userId.isEmpty()) {
            userId = getIntent().getStringExtra("userId");
        }

        userEmailTextView = findViewById(R.id.userEmailTextView);
        Button logoutButton = findViewById(R.id.logoutButton);
        Button calendarButton = findViewById(R.id.calendarButton);

        logoutButton.setOnClickListener(v -> logout());

        calendarButton.setOnClickListener(v -> goToCalendarActivity(userId));


        if (!userId.isEmpty()) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String email = dataSnapshot.child("email").getValue(String.class);
                        userEmailTextView.setText(email);
                    } else {
                        userEmailTextView.setText("User data not found");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("userId");
        editor.apply();

        Intent intent = new Intent(EventsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToCalendarActivity(String userId) {
        Intent intent = new Intent(EventsActivity.this, CalendarActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }
}