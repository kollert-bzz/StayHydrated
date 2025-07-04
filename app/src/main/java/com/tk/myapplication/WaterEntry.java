package com.tk.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class WaterEntry extends AppCompatActivity {

    private SharedPreferences prefs;
    private EditText addInput;
    private TextView dateText, timeText;
    private Handler handler = new Handler();

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


        SharedPreferences profilePrefs = getSharedPreferences("profile", MODE_PRIVATE);
        CircleImageView profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> navigateToProfile());

        String imagePath = profilePrefs.getString("profile_image_path", null);
        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                profileButton.setImageBitmap(myBitmap);
            }
        }

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> navigateToHome());


        prefs = getSharedPreferences("waterdata", MODE_PRIVATE);
        addInput = findViewById(R.id.addInput);
        Button addWaterButton = findViewById(R.id.addWater);
        addWaterButton.setOnClickListener(v -> getAmount());

        dateText = findViewById(R.id.date);
        timeText = findViewById(R.id.time);

        dateText.setText(getDate());
        timeText.setText(getTime());

        handler.postDelayed(updateTimeRunnable, 1000);
    }

    private void getAmount() {
        String inputText = addInput.getText().toString().trim();
        if (!inputText.isEmpty()) {
            try {
                int newAmount = Integer.parseInt(inputText);
                int currentAmount = prefs.getInt("totalWater", 0);
                int updatedAmount = currentAmount + newAmount;
                prefs.edit().putInt("totalWater", updatedAmount).apply();

                addInput.setText("");
                navigateToHome();
            } catch (NumberFormatException e) {
                addInput.setError("Bitte gueltige Zahl eingeben");
            }
        } else {
            addInput.setError("Feld darf nicht leer sein");
        }
    }

    private String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private String getTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(new Date());
    }

    private final Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            timeText.setText(getTime());
            handler.postDelayed(this, 30000);
        }
    };

    private void navigateToProfile() {
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTimeRunnable);
    }
}
