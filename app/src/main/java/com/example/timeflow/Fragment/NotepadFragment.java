package com.example.timeflow.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.timeflow.Adapter.NotepadAdapter;
import com.example.timeflow.HelpersClass.Notepad;
import com.example.timeflow.NotepadActivity;
import com.example.timeflow.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class NotepadFragment extends Fragment implements NotepadAdapter.OnItemClickListener {

    private RecyclerView mRecyclerView;
    private NotepadAdapter mAdapter;
    private List<Notepad> mNotepad;
    private ProgressBar progressBar;
    private TextView textView;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notepad, container, false);

        initializeUI(view);
        setupSharedPreferences();
        setupRecyclerView(view);
        setupCalendarButton(view);
        setupProgressBar(view);

        if (!userId.isEmpty()) {
            setupDatabaseListener();
        }

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

    private void setupRecyclerView(View view) {
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        mNotepad = new ArrayList<>();
        mAdapter = new NotepadAdapter(requireContext(), mNotepad, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setupCalendarButton(View view) {
        FloatingActionButton calendarButton = view.findViewById(R.id.calendarButton);
        calendarButton.setOnClickListener(v -> goToNotepadActivity(userId));
    }

    private void setupProgressBar(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setupDatabaseListener() {
        DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("notes");
        notesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Notepad> notes = new ArrayList<>();

                for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                    Notepad note = noteSnapshot.getValue(Notepad.class);
                    notes.add(note);
                }

                updateNoteList(notes);
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleDatabaseError(databaseError);
            }
        });
    }

    private void updateNoteList(List<Notepad> notes) {
        mNotepad.clear();
        mNotepad.addAll(notes);
        mAdapter.notifyDataSetChanged();
    }

    private void updateUI() {
        progressBar.setVisibility(View.GONE);

        if (mNotepad.isEmpty()) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
        }
    }

    private void goToNotepadActivity(String userId) {
        Intent intent = new Intent(requireActivity(), NotepadActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    @Override
    public void onItemClick(int position) {
        Notepad clickedEvent = mNotepad.get(position);
        Intent intent = new Intent(requireActivity(), NotepadActivity.class);
        intent.putExtra("clickedEvent", clickedEvent);
        startActivity(intent);
    }

    private void handleDatabaseError(DatabaseError databaseError) {
        progressBar.setVisibility(View.GONE);
        Log.e(getString(R.string.log_tag_notepad_fragment), getString(R.string.log_message_database_error, databaseError.getMessage()));
    }
}
