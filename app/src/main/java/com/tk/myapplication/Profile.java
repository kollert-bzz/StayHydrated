package com.tk.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> navigateToHome());

        EditText addInput = findViewById(R.id.addInput);
        Button addButton = findViewById(R.id.addButton);
        TextView userHeader = findViewById(R.id.userHeader);

        SharedPreferences prefs = getSharedPreferences("profile", MODE_PRIVATE);
        String savedUsername = prefs.getString("username", "");

        userHeader.setText(savedUsername.isEmpty() ? "User" : savedUsername);
        //addInput.setText(savedUsername);

        addButton.setOnClickListener(v -> setUsername(addInput, userHeader, prefs));
    }

    private void setUsername(EditText input, TextView header, SharedPreferences prefs) {
        String newUsername = input.getText().toString().trim();
        input.setText("");
        if (!newUsername.isEmpty()) {
            prefs.edit().putString("username", newUsername).apply();
            header.setText(newUsername);
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }
}
