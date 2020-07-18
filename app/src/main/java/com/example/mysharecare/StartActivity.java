package com.example.mysharecare;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class StartActivity extends AppCompatActivity {
    Button sendButton;
    Button receiveButton;
    LinearLayout startingLinearLayout;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        sendButton = findViewById(R.id.sendButton);
        receiveButton = findViewById(R.id.receiveButton);
        startingLinearLayout = findViewById(R.id.startingLinearLayout);
        if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }


    }

    public void sendreceive(View view) {
        startingLinearLayout.setVisibility(View.GONE);
        if (view.getId() == R.id.sendButton)

            getSupportFragmentManager().beginTransaction().replace(R.id.startcontainer, new SelectItemsToSendFragment()).commit();
        else {
            Intent intent = new Intent(this, MainActivity.class);

            if (view.getId() == R.id.receiveButton) {
                intent.putExtra(AppConstants.SENDRECEIVEEXTRA, AppConstants.RECEIVE);
            }
            startActivity(intent);
        }
    }
}