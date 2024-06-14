package com.example.timeflow.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.timeflow.CalendarActivity;
import com.example.timeflow.Service.EventNotificationService;
import com.example.timeflow.HelpersClass.Event;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;


import com.example.timeflow.Adapter.EventsAdapter;

import com.example.timeflow.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.util.Map;
import java.util.TreeMap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;




public class EventsFragment extends Fragment implements EventsAdapter.OnItemClickListener {

    private RecyclerView mRecyclerView;
    private EventsAdapter mAdapter;
    private List<Event> mEvents;
    private ProgressBar progressBar;
    private TextView textView;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        initializeUI(view);
        setupSharedPreferences();
        startEventNotificationService();
        setupRecyclerView(view);
        setupCalendarButton(view);
        setupProgressBar(view);
        if (!userId.isEmpty()) {
            setupDatabaseListener();
        }
        handleArguments();
        return view;
    }

    private void initializeUI(View view) {
        textView = view.findViewById(R.id.textView);
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");
        if (userId.isEmpty()) {
            userId = requireActivity().getIntent().getStringExtra("userId");
        }
    }

    private void startEventNotificationService() {
        Intent serviceIntent = new Intent(requireActivity(), EventNotificationService.class);
        requireActivity().startService(serviceIntent);
    }

    private void setupRecyclerView(View view) {
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mEvents = new ArrayList<>();
        mAdapter = new EventsAdapter(requireContext(), mEvents, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setupCalendarButton(View view) {
        FloatingActionButton calendarButton = view.findViewById(R.id.calendarButton);
        calendarButton.setOnClickListener(v -> goToCalendarActivity(userId));
    }

    private void setupProgressBar(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setupDatabaseListener() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("events");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Event> todayEvents = new ArrayList<>();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String todayDateString = dateFormat.format(new Date());
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    String eventDate = event.getEventDate();
                    if (eventDate.equals(todayDateString)) {
                        todayEvents.add(event);
                    }
                }
                updateEventList(todayEvents);
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleDatabaseError();
            }
        });
    }

    private void handleArguments() {
        if (getArguments() != null) {
            String wordInquiry = getArguments().getString("wordInquiry");
            fetchDataFromDatabase(wordInquiry);

            String update = getArguments().getString("update");
            if (update != null && update.equals("1")) {
                setupDatabaseListener();
            }

            String selectedDate = getArguments().getString("selectedDate");
            if (selectedDate != null && !selectedDate.isEmpty()) {
                fetchDataByDateFromDatabase(selectedDate);
            }
        }
    }

    private void updateEventList(DataSnapshot dataSnapshot) {
        TreeMap<String, List<Event>> eventosPorFecha = new TreeMap<>();
        for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
            Event event = eventSnapshot.getValue(Event.class);
            String fechaEvento = event.getEventDate();
            if (!eventosPorFecha.containsKey(fechaEvento)) {
                eventosPorFecha.put(fechaEvento, new ArrayList<>());
            }
            eventosPorFecha.get(fechaEvento).add(event);
        }
        updateEventListFromMap(eventosPorFecha);
    }

    private void updateEventList(List<Event> events) {
        TreeMap<String, List<Event>> eventosPorFecha = new TreeMap<>();
        for (Event event : events) {
            String fechaEvento = event.getEventDate();
            if (!eventosPorFecha.containsKey(fechaEvento)) {
                eventosPorFecha.put(fechaEvento, new ArrayList<>());
            }
            eventosPorFecha.get(fechaEvento).add(event);
        }
        updateEventListFromMap(eventosPorFecha);
    }

    private void updateEventListFromMap(TreeMap<String, List<Event>> eventosPorFecha) {
        mEvents.clear();
        for (Map.Entry<String, List<Event>> entry : eventosPorFecha.entrySet()) {
            List<Event> eventosEnFecha = entry.getValue();
            mEvents.addAll(eventosEnFecha);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void updateUI() {
        if (mRecyclerView != null) {
            mAdapter.scrollToBottom(mRecyclerView);
        }
        progressBar.setVisibility(View.GONE);
        textView.setVisibility(mEvents.isEmpty() ? View.VISIBLE : View.INVISIBLE);
    }

    public void fetchDataFromDatabase(String palabraConsulta) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("events");
        Query query = userRef.orderByChild("eventName").startAt(palabraConsulta).endAt(palabraConsulta + "\uf8ff");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateEventList(dataSnapshot);
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleDatabaseError();
            }
        });
    }

    public void fetchDataByDateFromDatabase(String date) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("events");
        Query query = userRef.orderByChild("eventDate").equalTo(date);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateEventList(dataSnapshot);
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleDatabaseError();
            }
        });
    }

    private void handleDatabaseError() {
        progressBar.setVisibility(View.GONE);
        Log.e(getString(R.string.log_tag_database_error), getString(R.string.log_message_query_error));
    }

    private void goToCalendarActivity(String userId) {
        Intent intent = new Intent(requireActivity(), CalendarActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    @Override
    public void onItemClick(int position) {
        Event clickedEvent = mEvents.get(position);
        Intent intent = new Intent(requireActivity(), CalendarActivity.class);
        intent.putExtra("clickedEvent", clickedEvent);
        startActivity(intent);
    }
}


