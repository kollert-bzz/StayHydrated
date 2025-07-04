package com.tk.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import de.hdodenhof.circleimageview.CircleImageView;

public class Home extends AppCompatActivity {

    private static final String CHANNEL_ID = "reminder_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    private ProgressBar circleProgress;
    private TextView progressText;
    private LottieAnimationView sparkleEffect;
    private Handler reminderHandler = new Handler();
    private Runnable reminderRunnable;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button addWaterButton = findViewById(R.id.addButton);
        addWaterButton.setOnClickListener(v -> {
            cancelWaterReminder();
            navigateToWaterEntry();
        });

        TextView mlText = findViewById(R.id.progressText);
        SharedPreferences waterPrefs = getSharedPreferences("waterdata", MODE_PRIVATE);
        int totalWater = waterPrefs.getInt("totalWater", 0);
        mlText.setText(totalWater + " ml");

        CircleImageView profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> navigateToProfile());

        SharedPreferences profilePrefs = getSharedPreferences("profile", MODE_PRIVATE);
        String imagePath = profilePrefs.getString("profile_image_path", null);
        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                profileButton.setImageBitmap(myBitmap);
            }
        }

        circleProgress = findViewById(R.id.circleProgress);
        progressText = findViewById(R.id.progressText);
        sparkleEffect = findViewById(R.id.sparkleEffect);

        createNotificationChannel();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            } else {
                scheduleWaterReminder();
            }
        } else {
            scheduleWaterReminder();
        }

        updateProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProgress();
    }

    private void updateProgress() {
        SharedPreferences waterPrefs = getSharedPreferences("waterdata", MODE_PRIVATE);
        SharedPreferences.Editor waterEditor = waterPrefs.edit();

        SharedPreferences historyPrefs = getSharedPreferences("history", MODE_PRIVATE);
        SharedPreferences.Editor historyEditor = historyPrefs.edit();

        String lastDate = waterPrefs.getString("last_update_date", "");
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (!today.equals(lastDate)) {
            int yesterdayAmount = waterPrefs.getInt("totalWater", 0);
            historyEditor.putInt("history_" + lastDate, yesterdayAmount);
            historyEditor.apply();

            waterEditor.putInt("totalWater", 0);
            waterEditor.putString("last_update_date", today);
            waterEditor.apply();
        }

        int totalWater = waterPrefs.getInt("totalWater", 0);
        SharedPreferences profilePrefs = getSharedPreferences("profile", MODE_PRIVATE);
        int dailyGoal = profilePrefs.getInt("daily_goal_ml", 2000);

        int progressPercent = (int) ((totalWater / (float) dailyGoal) * 100);
        if (progressPercent > 100) progressPercent = 100;

        circleProgress.setProgress(progressPercent);
        progressText.setText(totalWater + " / " + dailyGoal + " ml");

        ImageView trophyIcon = findViewById(R.id.trophyIcon);
        if (totalWater >= dailyGoal) {
            trophyIcon.setVisibility(View.VISIBLE);
            if (!sparkleEffect.isAnimating()) {
                sparkleEffect.setVisibility(View.VISIBLE);
                sparkleEffect.playAnimation();
            }
        } else {
            trophyIcon.setVisibility(View.GONE);
            sparkleEffect.cancelAnimation();
            sparkleEffect.setVisibility(View.GONE);
        }

        setupBarChart();
    }

    private void setupBarChart() {
        BarChart barChart = findViewById(R.id.barChart);
        SharedPreferences historyPrefs = getSharedPreferences("history", MODE_PRIVATE);

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat labelFormat = new SimpleDateFormat("EEE\ndd.MM", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        for (int i = 6; i >= 0; i--) {
            Calendar day = (Calendar) calendar.clone();
            day.add(Calendar.DAY_OF_YEAR, -i);
            String key = keyFormat.format(day.getTime());
            int value = historyPrefs.getInt("history_" + key, 0);

            entries.add(new BarEntry(6 - i, value));
            labels.add(labelFormat.format(day.getTime()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "ml");
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-45);

        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.invalidate();
    }

    private void scheduleWaterReminder() {
        reminderRunnable = this::showWaterReminderNotification;
        reminderHandler.postDelayed(reminderRunnable, 10000);
    }

    private void cancelWaterReminder() {
        reminderHandler.removeCallbacks(reminderRunnable);
    }

    private void showWaterReminderNotification() {
        Intent intent = new Intent(this, Home.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.stayhydrated_trans)
                .setContentTitle("StayHydrated!!!")
                .setContentText("Don't forget to drink some water!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
        }
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Water Reminder";
            String description = "Reminds user to drink water";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scheduleWaterReminder();
            }
        }
    }
    private void navigateToWaterEntry() {
        Intent intent = new Intent(this, WaterEntry.class);
        startActivity(intent);
    }

    private void navigateToProfile() {
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }


}
