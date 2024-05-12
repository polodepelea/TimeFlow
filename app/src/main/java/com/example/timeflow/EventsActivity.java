package com.example.timeflow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.timeflow.HelpersClass.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EventsActivity extends AppCompatActivity implements EventsAdapter.OnItemClickListener {

    private RecyclerView mRecyclerView;
    private EventsAdapter mAdapter;
    private List<Event> mEventos;

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

        Intent serviceIntent = new Intent(this, EventNotificationService.class);
        startService(serviceIntent);


        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mEventos = new ArrayList<>();
        mAdapter = new EventsAdapter(this, mEventos, this);
        mRecyclerView.setAdapter(mAdapter);

        FloatingActionButton calendarButton = findViewById(R.id.calendarButton);


        calendarButton.setOnClickListener(v -> goToCalendarActivity(userId));

        if (!userId.isEmpty()) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("events");
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    TreeMap<String, List<Event>> eventosPorFecha = new TreeMap<>();

                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        Event event = eventSnapshot.getValue(Event.class);
                        String fechaEvento = event.getEventDate();

                        if (!eventosPorFecha.containsKey(fechaEvento)) {
                            eventosPorFecha.put(fechaEvento, new ArrayList<>());
                        }

                        eventosPorFecha.get(fechaEvento).add(event);
                    }

                    mEventos.clear();

                    for (Map.Entry<String, List<Event>> entry : eventosPorFecha.entrySet()) {
                        List<Event> eventosEnFecha = entry.getValue();
                        for (Event evento : eventosEnFecha) {
                            mEventos.add(evento);
                        }
                    }

                    mAdapter.notifyDataSetChanged();

                    if (mRecyclerView != null) {
                        mAdapter.scrollToBottom(mRecyclerView);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }

    }

    @Override
    public void onItemClick(int position) {
        Event clickedEvent = mEventos.get(position);
        Intent intent = new Intent(this, CalendarActivity.class);
        intent.putExtra("clickedEvent", clickedEvent);
        startActivity(intent);
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

        Intent intent = new Intent(EventsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToCalendarActivity(String userId) {
        Intent intent = new Intent(EventsActivity.this, CalendarActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

}