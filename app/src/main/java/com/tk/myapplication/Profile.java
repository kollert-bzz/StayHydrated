package com.tk.myapplication;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private ImageView profileImageBig;
    private CircleImageView profileButton;
    private SharedPreferences prefs;

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
        Button imgButton = findViewById(R.id.imgButton);
        profileImageBig = findViewById(R.id.profileImgBig);
        profileButton = findViewById(R.id.profileButton);

        EditText goalEdit = findViewById(R.id.goalEdit);
        Button goalButton = findViewById(R.id.goalButton);
        TextView goalText = findViewById(R.id.goal);

        prefs = getSharedPreferences("profile", MODE_PRIVATE);
        String savedUsername = prefs.getString("username", "");
        userHeader.setText(savedUsername.isEmpty() ? "User" : savedUsername);

        loadProfileImage();

        int savedGoal = prefs.getInt("daily_goal_ml", 2000);
        goalText.setText(savedGoal / 1000.0 + "l");

        addButton.setOnClickListener(v -> setUsername(addInput, userHeader));

        imgButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        });

        goalButton.setOnClickListener(v -> saveDailyGoal(goalEdit, goalText));
    }

    private void saveDailyGoal(EditText inputField, TextView displayField) {
        String input = inputField.getText().toString().trim().replace(",", ".");
        if (!input.isEmpty()) {
            try {
                double liters = Double.parseDouble(input);

                if (liters <= 0) {
                    inputField.setError("Nur positive Werte erlaubt");
                    return;
                }

                int milliliters = (int) (liters * 1000);
                prefs.edit().putInt("daily_goal_ml", milliliters).apply();
                displayField.setText(milliliters / 1000.0 + "l");
                inputField.setText("");
            } catch (NumberFormatException e) {
                inputField.setError("Bitte gültige Zahl eingeben");
            }
        } else {
            inputField.setError("Feld darf nicht leer sein");
        }
    }


    private void setUsername(EditText input, TextView header) {
        String newUsername = input.getText().toString().trim();
        input.setText("");
        if (!newUsername.isEmpty()) {
            prefs.edit().putString("username", newUsername).apply();
            header.setText(newUsername);
        }
    }

    private void navigateToHome() {
        startActivity(new Intent(this, Home.class));
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            if (imageBitmap != null) {
                Bitmap squareBitmap = cropToSquare(imageBitmap);

                profileImageBig.setImageBitmap(squareBitmap);
                profileButton.setImageBitmap(squareBitmap);

                try {
                    File file = new File(getFilesDir(), "profile_thumbnail.jpg");
                    FileOutputStream fos = new FileOutputStream(file);
                    squareBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    fos.close();

                    prefs.edit().putString("profile_image_path", file.getAbsolutePath()).apply();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Bitmap cropToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newSize = Math.min(width, height);
        int xOffset = (width - newSize) / 2;
        int yOffset = (height - newSize) / 2;
        return Bitmap.createBitmap(bitmap, xOffset, yOffset, newSize, newSize);
    }

    private void loadProfileImage() {
        String imagePath = prefs.getString("profile_image_path", null);
        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                profileImageBig.setImageBitmap(myBitmap);
                profileButton.setImageBitmap(myBitmap);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        }
    }
}
