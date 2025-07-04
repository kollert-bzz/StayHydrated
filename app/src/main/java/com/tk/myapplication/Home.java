package com.tk.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Home extends AppCompatActivity {

    private ProgressBar circleProgress;
    private TextView progressText;
    private LottieAnimationView sparkleEffect;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button addWaterButton = findViewById(R.id.addButton);
        addWaterButton.setOnClickListener(v -> navigateToWaterEntry());

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

        updateProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProgress();
    }

    private void updateProgress() {
        SharedPreferences waterPrefs = getSharedPreferences("waterdata", MODE_PRIVATE);
        SharedPreferences profilePrefs = getSharedPreferences("profile", MODE_PRIVATE);

        int totalWater = waterPrefs.getInt("totalWater", 0);
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
