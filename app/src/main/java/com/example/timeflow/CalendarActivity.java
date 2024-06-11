package com.example.timeflow;

import androidx.appcompat.app.AppCompatActivity;


import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.timeflow.HelpersClass.Event;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class CalendarActivity extends AppCompatActivity {
    private Button buttonHour, saveButton, deleteButton, editButton;
    private EditText hourText, dateText, nameText;
    private String userId;
    private ProgressBar progressBar;
    private Event clickedEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        initializeViews();
        setupButtons();
        retrieveUserId();
        getSelectedDate();
    }

    private void initializeViews() {
        progressBar = findViewById(R.id.progressBar2);
        hideProgressBar();

        dateText = findViewById(R.id.dateText);
        hourText = findViewById(R.id.hourText);
        nameText = findViewById(R.id.nameText);

        deleteButton = findViewById(R.id.buttonDelete);
        saveButton = findViewById(R.id.deleteButton);
        editButton = findViewById(R.id.editButton);
        buttonHour = findViewById(R.id.buttonHour);
    }

    private void setupButtons() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("clickedEvent")) {
            setupForEditEvent(intent);
        } else {
            setupForNewEvent();
        }

        saveButton.setOnClickListener(v -> onSaveButtonClick());
        buttonHour.setOnClickListener(v -> onHourButtonClick());
        deleteButton.setOnClickListener(v -> deleteEvent());
        editButton.setOnClickListener(v -> editEvent());
    }

    private void setupForEditEvent(Intent intent) {
        deleteButton.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.INVISIBLE);
        editButton.setVisibility(View.VISIBLE);

        clickedEvent = (Event) intent.getSerializableExtra("clickedEvent");

        dateText.setText(clickedEvent.getEventDate());
        hourText.setText(clickedEvent.getEventTime());
        nameText.setText(clickedEvent.getEventName());
        nameText.setEnabled(false);
    }

    private void setupForNewEvent() {
        deleteButton.setVisibility(View.INVISIBLE);
        saveButton.setVisibility(View.VISIBLE);
        editButton.setVisibility(View.INVISIBLE);
    }

    private void retrieveUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");
        if (userId.isEmpty()) {
            userId = getIntent().getStringExtra("userId");
        }
    }


    private void getSelectedDate() {
        CalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String formattedMonth = String.format("%02d", month + 1);
            String formattedDayOfMonth = String.format("%02d", dayOfMonth);
            String selectedDate = year + "-" + formattedMonth + "-" + formattedDayOfMonth;
            dateText.setText(selectedDate);
        });
    }

    private void onHourButtonClick() {
        final Calendar c = Calendar.getInstance();
        int hours = c.get(Calendar.DAY_OF_MONTH);
        int minutes = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, R.style.TimePicker, (view, hourOfDay, minute) -> {
            String formattedMinute = String.format("%02d", minute);
            hourText.setText(hourOfDay + ":" + formattedMinute);
        }, hours, minutes, false);
        timePickerDialog.show();
    }

    public void deleteEvent() {
        showProgressBar();

        DatabaseReference userEventsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("events");

        userEventsRef.orderByChild("eventName").equalTo(clickedEvent.getEventName()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        eventSnapshot.getRef().removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    finish();
                                    hideProgressBar();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(CalendarActivity.this, "Error deleting event", Toast.LENGTH_SHORT).show();
                                    hideProgressBar();
                                });
                        break;
                    }
                } else {
                    Toast.makeText(CalendarActivity.this, "No event found to delete", Toast.LENGTH_SHORT).show();
                    hideProgressBar();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CalendarActivity.this, "Error querying the database", Toast.LENGTH_SHORT).show();
                hideProgressBar();
            }
        });
    }

    public void editEvent() {
        showProgressBar();

        final String newDate = dateText.getText().toString();
        final String newHour = hourText.getText().toString();

        if (!newDate.isEmpty() && !newHour.isEmpty()) {
            DatabaseReference userEventsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("events");

            userEventsRef.orderByChild("eventName").equalTo(clickedEvent.getEventName()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                            eventSnapshot.getRef().child("eventDate").setValue(newDate);
                            eventSnapshot.getRef().child("eventTime").setValue(newHour)
                                    .addOnSuccessListener(aVoid -> {
                                        finish();
                                        hideProgressBar();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(CalendarActivity.this, "Error editing event", Toast.LENGTH_SHORT).show();
                                        hideProgressBar();
                                    });
                            break;
                        }
                    } else {
                        Toast.makeText(CalendarActivity.this, "No event found to edit", Toast.LENGTH_SHORT).show();
                        hideProgressBar();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(CalendarActivity.this, "Error querying the database", Toast.LENGTH_SHORT).show();
                    hideProgressBar();
                }
            });
        } else {
            Toast.makeText(CalendarActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            hideProgressBar();
        }
    }

    private void onSaveButtonClick() {
        showProgressBar();

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
                            hideProgressBar();
                        } else {
                            Event event = new Event(eventDate, eventTime, eventName);
                            String eventId = userEventsRef.push().getKey();
                            userEventsRef.child(eventId).setValue(event)
                                    .addOnSuccessListener(aVoid -> {
                                        finish();
                                        hideProgressBar();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(CalendarActivity.this, "Error adding event", Toast.LENGTH_SHORT).show();
                                        hideProgressBar();
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(CalendarActivity.this, "Error querying the database", Toast.LENGTH_SHORT).show();
                        hideProgressBar();
                    }
                });

            } else {
                Toast.makeText(getApplicationContext(), "Event name must not exceed 11 characters", Toast.LENGTH_SHORT).show();
                hideProgressBar();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            hideProgressBar();
        }
    }

    private void showProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }
}



