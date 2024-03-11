package com.finusha.myapplication;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class qrscanner extends AppCompatActivity {

    private String promptMessage = "Scan a QR Code";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        // Initiate the QR code scanning process
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.setPrompt(promptMessage);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Retrieve the scan result
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // QR code scanned successfully, show link in a dialog box
                showDialog(result.getContents());
            } else {
                // Scanning was canceled
                showCancelDialog();
            }
        }
    }

    private void showDialog(final String link) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("QR Code Link");
        builder.setMessage(link);
        builder.setPositiveButton("View", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Handle the "View" button click
                // Open the link in a web browser or perform any other action
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(link));
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Handle the "Cancel" button click
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void showCancelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scanning Canceled");
        builder.setMessage("Are you sure you want to cancel scanning?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Handle the "Yes" button click
                dialogInterface.dismiss();
                finish(); // Close the activity
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Handle the "No" button click
                // Re initiate the scanning process
                IntentIntegrator intentIntegrator = new IntentIntegrator(qrscanner.this);
                intentIntegrator.setOrientationLocked(false);
                intentIntegrator.setPrompt(promptMessage);
                intentIntegrator.initiateScan();
            }
        });
        builder.show();
    }
}
