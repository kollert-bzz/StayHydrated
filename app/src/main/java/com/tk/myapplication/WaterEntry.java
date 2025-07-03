package com.tk.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import de.hdodenhof.circleimageview.CircleImageView;

public class WaterEntry extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_water_entry);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        CircleImageView profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> navigateToProfile());

        Button addWaterButton = findViewById(R.id.addWater);
        addWaterButton.setOnClickListener(v -> navigateToHome());

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> navigateToHome());
    }

    private void navigateToProfile() {
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }
}
