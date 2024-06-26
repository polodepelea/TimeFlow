package com.example.timeflow;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.timeflow.Service.EventNotificationService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.appcompat.app.AlertDialog;


import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class ProfileActivity extends AppCompatActivity {

    private EditText emailText, passwordText;
    private ImageButton imageButton;
    private Button deleteButton, changeButton;
    private ProgressBar progressBar;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private DatabaseReference imageRef;
    private DatabaseReference userRef;
    private String userId;

    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();
        setupTopAppBar();
        setupUserProfile();
        setupButtons();
    }

    private void initializeViews() {
        topAppBar = findViewById(R.id.topAppBar);
        progressBar = findViewById(R.id.progressBar4);
        emailText = findViewById(R.id.emailText);
        emailText.setEnabled(false);
        passwordText = findViewById(R.id.passwordText);
        imageButton = findViewById(R.id.imageButton);
        deleteButton = findViewById(R.id.deleteButton);
        changeButton = findViewById(R.id.changeButton);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        imageRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("images");
    }

    private void setupTopAppBar() {
        topAppBar.setNavigationOnClickListener(v -> {
            finish();
        });

        topAppBar.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.logout) {
                logout();
                return true;
            } else {
                return false;
            }
        });
    }

    private void setupUserProfile() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showProgressBar();

                if (dataSnapshot.exists()) {
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String password = dataSnapshot.child("password").getValue(String.class);

                    emailText.setText(email);
                    passwordText.setText(password);

                    if (dataSnapshot.hasChild("images")) {
                        String imageString = dataSnapshot.child("images").getValue(String.class);
                        Bitmap bitmap = base64ToBitmap(imageString);
                        imageButton.setImageBitmap(bitmap);
                    }
                }

                hideProgressBar();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressBar();
            }
        });
    }

    private void setupButtons() {
        deleteButton.setOnClickListener(view -> deleteUser());
        changeButton.setOnClickListener(view -> changePassword());
        imageButton.setOnClickListener(view -> openGallery());
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("userId");
        editor.apply();

        stopEventNotificationService();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void stopEventNotificationService() {
        Intent serviceIntent = new Intent(ProfileActivity.this, EventNotificationService.class);
        stopService(serviceIntent);
    }

    private void deleteUser() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_title_delete_account))
                .setMessage(getString(R.string.dialog_message_delete_account))
                .setPositiveButton("Yes", (dialog, which) -> {
                    userRef.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, getString(R.string.toast_message_account_deleted), Toast.LENGTH_SHORT).show();

                            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove("userId");
                            editor.apply();

                            stopEventNotificationService();
                            Toast.makeText(ProfileActivity.this, getString(R.string.toast_message_password_cannot_be_empty), Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ProfileActivity.this, getString(R.string.toast_message_failed_to_delete_account_data), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void changePassword() {
        String newPassword = passwordText.getText().toString();
        if (!newPassword.isEmpty()) {
            userRef.child("password").setValue(newPassword).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, getString(R.string.toast_message_password_updated), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, getString(R.string.toast_message_failed_to_update_password), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageButton.setImageBitmap(bitmap);
                String imageString = bitmapToBase64(bitmap);
                imageRef.setValue(imageString);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, getString(R.string.toast_message_failed_to_load_image), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap base64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
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




