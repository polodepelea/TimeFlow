package com.example.timeflow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.timeflow.Fragment.EventsFragment;
import com.example.timeflow.Fragment.NotepadFragment;
import com.example.timeflow.Service.EventNotificationService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Calendar;
import android.net.Uri;


public class MainActivity extends AppCompatActivity {

    private NavigationBarView navigationBarView;
    private MaterialToolbar topAppBar;
    private SearchView searchView;
    private static final String PDF_URL = "https://drive.google.com/file/d/1spCLm6iNOWwNKLKk3fq5kdRyKM_gkOmL/view?usp=sharing";
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        navigationBarView = findViewById(R.id.bottom_navigation);
        topAppBar = findViewById(R.id.topAppBar);

        replaceFragment(new EventsFragment());

        setupBottomNavigationBar();
        setupTopAppBarMenu();
    }

    private void setupBottomNavigationBar() {
        navigationBarView.setOnItemSelectedListener(item -> {
            Menu menu = topAppBar.getMenu();
            if (item.getItemId() == R.id.item_1) {
                replaceFragment(new EventsFragment());
                menu.findItem(R.id.search_calendar).setVisible(true);
                menu.findItem(R.id.search).setVisible(true);

                return true;
            } else if (item.getItemId() == R.id.item_2) {
                menu.findItem(R.id.search_calendar).setVisible(false);
                menu.findItem(R.id.search).setVisible(false);

                replaceFragment(new NotepadFragment());
                return true;
            } else {
                return false;
            }
        });
    }

    private void handleSearch() {
        topAppBar.setVisibility(View.INVISIBLE);

        searchView = findViewById(R.id.searchView);
        searchView.setVisibility(View.VISIBLE);
        searchView.setIconifiedByDefault(false);

        int closeButtonId = searchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeButton = searchView.findViewById(closeButtonId);
        closeButton.setColorFilter(Color.WHITE);
        closeButton.setVisibility(View.VISIBLE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                EventsFragment eventsFragment = new EventsFragment();

                Bundle bundle = new Bundle();
                bundle.putString("wordInquiry", query);
                eventsFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, eventsFragment)
                        .commit();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        closeButton.setOnClickListener(view -> {
            topAppBar.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.INVISIBLE);
            searchView.setQuery("", false);

            EventsFragment eventsFragment = new EventsFragment();

            Bundle bundle = new Bundle();
            bundle.putString("update", "1");
            eventsFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, eventsFragment)
                    .commit();
        });
    }

    private void setupTopAppBarMenu() {
        topAppBar.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.search) {
                handleSearch();
                return true;
            } else if (itemId == R.id.logout) {
                logout();
                return true;
            } else if (itemId == R.id.search_calendar) {
                calendarSearch();
                return true;
            } else if (itemId == R.id.profile) {
                goToProfileActivity(userId);
                return true;
            } else if (itemId == R.id.manual) {
                downloadAndOpenPdf();
                return true;
            } else {
                return false;
            }
        });
    }


    private void downloadAndOpenPdf() {
        Uri uri = Uri.parse(PDF_URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.toast_message_no_pdf_viewer), Toast.LENGTH_SHORT).show();
        }
    }

    private void calendarSearch() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.CalendarView,
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    String selectedDate = selectedDayOfMonth + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    EventsFragment eventsFragment = new EventsFragment();

                    Bundle bundle = new Bundle();
                    bundle.putString("selectedDate", selectedDate);
                    eventsFragment.setArguments(bundle);

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, eventsFragment)
                            .commit();
                },
                year,
                month,
                dayOfMonth
        );

        datePickerDialog.show();
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

    private void goToProfileActivity(String userId) {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }
}



