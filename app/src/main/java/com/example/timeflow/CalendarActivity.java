package com.example.timeflow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;

import com.example.timeflow.HelpersClass.Event;
import com.example.timeflow.HelpersClass.Login;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.UUID;

public class CalendarActivity extends AppCompatActivity {
    private Button buttonHour,saveButton,deleteButton;
    private EditText hourText,dateText,nameText;
    String userId;
    String eventId;

    Event clickedEvent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);


        dateText = findViewById(R.id.dateText);
        hourText = findViewById(R.id.hourText);
        nameText = findViewById(R.id.nameText);

        deleteButton = findViewById(R.id.buttonDelete);
        saveButton = findViewById(R.id.saveButton);
        buttonHour = findViewById(R.id.buttonHour);


        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("clickedEvent")) {
            deleteButton.setVisibility(View.VISIBLE);
            saveButton.setText("Edit Event");

            clickedEvent = (Event) intent.getSerializableExtra("clickedEvent");

            dateText.setText(clickedEvent.getEventDate());
            hourText.setText(clickedEvent.getEventTime());
            nameText.setText(clickedEvent.getEventName());


        } else {
            deleteButton.setVisibility(View.INVISIBLE);
            saveButton.setText("Save Event");

        }

        saveButton.setOnClickListener(v -> onSaveButtonClick());
        buttonHour.setOnClickListener(v -> onHourButtonClick());
        deleteButton.setOnClickListener(v -> deleteEvent());


        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");

        if (userId.isEmpty()) {
            userId = getIntent().getStringExtra("userId");
        }

        getSelectedDate();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            if(R.id.logout == item.getItemId()){
                logout();
                return true;
            }
            return super.onOptionsItemSelected(item);
    }

    public void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("userId");
        editor.apply();

        Intent intent = new Intent(CalendarActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    private void getSelectedDate() {
        CalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String formattedMonth = String.format("%02d", month + 1);
            String formattedDayOfMonth = String.format("%02d", dayOfMonth);

            String selectedDate = year + "-" + (formattedMonth ) + "-" + formattedDayOfMonth;
            dateText.setText(selectedDate);
        });
    }


    private void onHourButtonClick() {
        final Calendar c = Calendar.getInstance();
        int hours = c.get(Calendar.DAY_OF_MONTH);
        int minutes = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,R.style.TimePicker,(view, hourOfDay, minute) -> {
            String formattedMinute = String.format("%02d", minute);
            hourText.setText(hourOfDay + ":" + formattedMinute);
        }, hours, minutes, false);
        timePickerDialog.show();
    }


    public void deleteEvent() {
        DatabaseReference userEventsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("events");

        userEventsRef.orderByChild("eventName").equalTo(clickedEvent.getEventName()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        eventSnapshot.getRef().removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(CalendarActivity.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(CalendarActivity.this, "Error deleting event", Toast.LENGTH_SHORT).show();
                                });
                        break;
                    }
                } else {
                    Toast.makeText(CalendarActivity.this, "No event found to delete", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CalendarActivity.this, "Error querying the database", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void onSaveButtonClick() {
        String eventDate = dateText.getText().toString();
        String eventTime = hourText.getText().toString();
        String eventName = nameText.getText().toString();

        if (!eventDate.isEmpty() && !eventTime.isEmpty() && !eventName.isEmpty()) {
            if (eventName.length() <= 11) {
                DatabaseReference userEventsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("events");

                userEventsRef.orderByChild("eventName").equalTo(eventName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(CalendarActivity.this, "Event is already in use", Toast.LENGTH_SHORT).show();
                        } else {
                            Event event = new Event(eventDate, eventTime, eventName);
                            String eventId = userEventsRef.push().getKey();
                            userEventsRef.child(eventId).setValue(event);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(CalendarActivity.this, "Error querying the database", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(getApplicationContext(), "Event name must not exceed 11 characters", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
    }


}


