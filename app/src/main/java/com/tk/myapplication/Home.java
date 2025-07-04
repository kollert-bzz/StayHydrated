package com.tk.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Home extends AppCompatActivity {

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

        TextView mlText = findViewById(R.id.ml);
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
