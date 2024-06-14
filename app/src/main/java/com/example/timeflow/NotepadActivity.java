package com.example.timeflow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.timeflow.HelpersClass.Notepad;
import com.example.timeflow.Service.EventNotificationService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotepadActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private String userId, id;
    private EditText textText, textTitle;
    private Notepad clickedNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepad);

        initialize();
        setupTopAppBar();
        loadNoteFromIntent();
    }

    private void initialize() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");
        if (userId.isEmpty()) {
            userId = getIntent().getStringExtra("userId");
        }

        textTitle = findViewById(R.id.textTitle);
        textText = findViewById(R.id.textText);
        topAppBar = findViewById(R.id.topAppBar);
    }

    private void setupTopAppBar() {
        topAppBar.setNavigationOnClickListener(v -> {
            saveNoteAndFinish();
        });

        topAppBar.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.logout) {
                logout();
                return true;
            } else if (itemId == R.id.delete) {
                deleteCurrentNote();
                return true;
            } else {
                return false;
            }
        });
    }

    private void loadNoteFromIntent() {
        Intent intent = getIntent();
        clickedNote = (Notepad) intent.getSerializableExtra("clickedEvent");

        if (clickedNote != null) {
            textText.setText(clickedNote.getText());
            textTitle.setText(clickedNote.getTitle());
            id = clickedNote.getId();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveNoteAndFinish();
    }

    private void saveNoteAndFinish() {
        saveNote();
        finish();
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("userId");
        editor.apply();

        stopEventNotificationService();

        Intent intent = new Intent(NotepadActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void stopEventNotificationService() {
        Intent serviceIntent = new Intent(NotepadActivity.this, EventNotificationService.class);
        stopService(serviceIntent);
    }

    private void saveNote() {
        String title = textTitle.getText().toString().trim();
        String text = textText.getText().toString().trim();

        DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("notes");

        if (clickedNote != null && clickedNote.getId() != null) {
            String noteId = clickedNote.getId();

            if (!title.isEmpty() && !text.isEmpty()) {
                Notepad updatedNote = new Notepad(noteId, title, text);
                notesRef.child(noteId).setValue(updatedNote)
                        .addOnSuccessListener(aVoid -> {
                        })
                        .addOnFailureListener(e -> {
                        });
            } else {
                notesRef.child(noteId).removeValue()
                        .addOnSuccessListener(aVoid -> {
                        })
                        .addOnFailureListener(e -> {
                        });
            }
        } else {
            if (!title.isEmpty() && !text.isEmpty()) {
                String newNoteId = notesRef.push().getKey();
                Notepad newNote = new Notepad(newNoteId, title, text);
                notesRef.child(newNoteId).setValue(newNote)
                        .addOnSuccessListener(aVoid -> {
                        })
                        .addOnFailureListener(e -> {
                        });
            } else {
                Toast.makeText(NotepadActivity.this, getString(R.string.toast_message_fill_all_fields), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteCurrentNote() {
        if (id != null) {
            DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId)
                    .child("notes");

            notesRef.child(id).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(NotepadActivity.this, getString(R.string.toast_message_note_deleted_successfully), Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(NotepadActivity.this, getString(R.string.toast_message_failed_to_delete_note), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(NotepadActivity.this, getString(R.string.toast_message_no_note_to_delete), Toast.LENGTH_SHORT).show();
        }
    }
}



