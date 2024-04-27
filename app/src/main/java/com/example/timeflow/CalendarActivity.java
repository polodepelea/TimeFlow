package com.example.timeflow;

import androidx.appcompat.app.AppCompatActivity;


import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;

import com.example.timeflow.HelpersClass.Event;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.UUID;

public class CalendarActivity extends AppCompatActivity {
    private Button buttonHour,saveButton;
    private EditText hourText,dateText,nameText;
    String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        buttonHour = findViewById(R.id.buttonHour);
        saveButton = findViewById(R.id.saveButton);
        hourText = findViewById(R.id.hourText);
        dateText = findViewById(R.id.dateText);
        nameText = findViewById(R.id.nameText);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");

        if (userId.isEmpty()) {
            userId = getIntent().getStringExtra("userId");
        }

        buttonHour.setOnClickListener(v -> onHourButtonClick());

        saveButton.setOnClickListener(v -> onSaveButtonClick());


        getSelectedDate();
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

    private void onSaveButtonClick() {
        String eventDate = dateText.getText().toString();
        String eventTime = hourText.getText().toString();
        String eventName = nameText.getText().toString();



        if (!eventDate.isEmpty() && !eventTime.isEmpty() && !eventName.isEmpty()) {
            String eventToken = UUID.randomUUID().toString();

            DatabaseReference userEventsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("events");

            Event event = new Event(eventDate, eventTime, eventName);
            userEventsRef.child(eventToken).setValue(event);

            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
    }

}


