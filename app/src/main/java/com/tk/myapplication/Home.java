package com.tk.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;
import de.hdodenhof.circleimageview.CircleImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



public class Home extends AppCompatActivity {

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

        CircleImageView profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> navigateToProfile());
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

