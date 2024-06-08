package com.example.timeflow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.timeflow.Fragment.EventsFragment;
import com.example.timeflow.Fragment.NotepadFragment;
import com.example.timeflow.Service.EventNotificationService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationBarView navigationBarView = findViewById(R.id.bottom_navigation);
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);

        replaceFragment(new EventsFragment());

        setupBottomNavigationBar(navigationBarView);
        setupTopAppBarMenu(topAppBar);
    }

    private void setupBottomNavigationBar(NavigationBarView navigationBarView) {
        navigationBarView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.item_1) {
                replaceFragment(new EventsFragment());
                return true;
            } else if (item.getItemId() == R.id.item_2) {
                replaceFragment(new NotepadFragment());
                return true;
            } else {
                return false;
            }
        });
    }

    private void setupTopAppBarMenu(MaterialToolbar topAppBar) {
        topAppBar.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.favorite) {
                return true;
            } else if (itemId == R.id.logout) {
                logout();
                return true;
            } else {
                return false;
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("userId");
        editor.apply();

        stopEventNotificationService();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void stopEventNotificationService() {
        Intent serviceIntent = new Intent(MainActivity.this, EventNotificationService.class);
        stopService(serviceIntent);
    }
}


