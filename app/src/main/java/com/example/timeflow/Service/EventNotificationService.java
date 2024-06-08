package com.example.timeflow.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.timeflow.Fragment.EventsFragment;
import com.example.timeflow.HelpersClass.Event;
import com.example.timeflow.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EventNotificationService extends Service {
    private static final String CHANNEL_ID = "EventNotifications";
    private static final int NOTIFICATION_ID = 123;
    private static final String TAG = "EventNotificationServi";

    private String userId;
    private DatabaseReference eventsRef;
    private Handler handler;
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void initializeService() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");
        if (userId.isEmpty()) {
            stopSelf();
            return;
        }

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());

        eventsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("events");

        handler = new Handler();
        runnable = () -> {
            checkEvents();
            handler.postDelayed(runnable, 1000);
        };
        handler.post(runnable);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Event Notifications";
            String description = "Notifications for upcoming events";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, EventsFragment.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Event Notifications")
                .setContentText("Running in background")
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void checkEvents() {
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    if (event != null) {
                        try {
                            if (isEventNear(event)) {
                                int notificationId = event.getEventName().hashCode();
                                showNotification(event.getEventName(), "Event is coming soon!", notificationId);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching events: " + databaseError.getMessage());
            }
        });
    }

    private boolean isEventNear(Event event) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date eventDateTime = sdf.parse(event.getEventDate() + " " + event.getEventTime());
        Date currentDateTime = new Date();
        long differenceInMillis = eventDateTime.getTime() - currentDateTime.getTime();
        return differenceInMillis > 0 && differenceInMillis <= 1000;
    }

    private void showNotification(String title, String message, int notificationId) {
        Intent notificationIntent = new Intent(this, EventsFragment.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationId, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, builder.build());
    }
}









