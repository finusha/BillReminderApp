package com.finusha.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class billdetailactivity extends AppCompatActivity {
    private DatabaseReference userBillsRef;
    private String mBillId;
    Button seePaymentLocationsButton;
    Button editButton;
    Button deleteButton;
    Button scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billdetailactivity);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(billdetailactivity.this, Login.class);
            startActivity(intent);
            finish();
            return;
        }

        // Retrieve bill details from intent extras
        Bundle extras = getIntent().getExtras();
        mBillId = extras.getString("billId");
        String billName = extras.getString("billName");
        String dueDate = extras.getString("dueDate");
        String amountDue = extras.getString("amountDue");

        // Set text of each TextView to the corresponding bill detail
        TextView billIdTextView = findViewById(R.id.bill_detail_id);
        billIdTextView.setText("Bill ID: " + mBillId);

        TextView billNameTextView = findViewById(R.id.bill_detail_name);
        billNameTextView.setText("Bill Name: " + billName);

        TextView dueDateTextView = findViewById(R.id.bill_detail_due_date);
        dueDateTextView.setText("Due Date: " + dueDate);

        TextView amountDueTextView = findViewById(R.id.bill_detail_amount_due);
        amountDueTextView.setText("Amount Due: " + amountDue);

        seePaymentLocationsButton = findViewById(R.id.btn_see_payment_locations);
        editButton = findViewById(R.id.btn_edit);
        deleteButton = findViewById(R.id.btn_delete);
        scanButton = findViewById(R.id.btn_scan_for_payment);

        userBillsRef = FirebaseDatabase.getInstance().getReference()
                .child("user_bills")
                .child(currentUser.getUid());

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditBill();
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(billdetailactivity.this, qrscanner.class);
                startActivity(intent);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteBill();
            }
        });

        seePaymentLocationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(billdetailactivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        // Check if the due date is near and show a popup notification
        checkDueDateReminder(dueDate);
    }

    private void EditBill() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Bill");

        // Inflate the layout for the AlertDialog
        View view = LayoutInflater.from(this).inflate(R.layout.activity_edit_bill_dialog, null);
        builder.setView(view);

        // Get the EditText fields for the new bill details
        EditText billNameEditText = view.findViewById(R.id.edit_bill_name);
        EditText dueDateEditText = view.findViewById(R.id.edit_due_date);
        EditText amountDueEditText = view.findViewById(R.id.edit_amount_due);

        // Retrieve the current bill details from the TextViews
        TextView billNameTextView = findViewById(R.id.bill_detail_name);
        String name = billNameTextView.getText().toString().substring(11);

        TextView dueDateTextView = findViewById(R.id.bill_detail_due_date);
        String dueDate = dueDateTextView.getText().toString().substring(10);

        TextView amountDueTextView = findViewById(R.id.bill_detail_amount_due);
        String amountDue = amountDueTextView.getText().toString().substring(amountDueTextView.getText().toString().indexOf(":") + 2);

        // Populate the EditText fields with the current bill details
        billNameEditText.setText(name);
        dueDateEditText.setText(dueDate);
        amountDueEditText.setText(amountDue);

        // Set up the Save button in the AlertDialog
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the new bill details from the EditText fields
                String newBillName = billNameEditText.getText().toString();
                String newDueDate = dueDateEditText.getText().toString();
                String newAmountDue = amountDueEditText.getText().toString();

                // Update the corresponding values in the Firebase Realtime Database
                DatabaseReference billRef = userBillsRef.child(mBillId);
                billRef.child("billName").setValue(newBillName);
                billRef.child("dueDate").setValue(newDueDate);
                billRef.child("amountDue").setValue(newAmountDue, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            // Error occurred while updating the data
                            Toast.makeText(billdetailactivity.this, "Failed to update bill details", Toast.LENGTH_SHORT).show();
                        } else {
                            // Data updated successfully
                            // Update the TextViews with the new bill details
                            billNameTextView.setText("Bill Name: " + newBillName);
                            dueDateTextView.setText("Due Date: " + newDueDate);
                            amountDueTextView.setText("Amount Due: " + newAmountDue);
                        }
                    }
                });
            }
        });

        // Set up the Cancel button in the AlertDialog
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });

        // Display the AlertDialog
        builder.show();
    }

    private void DeleteBill() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Bill");
        builder.setMessage("Are you sure you want to delete this bill?");

        // Set up the Yes button in the AlertDialog
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete the bill from the Firebase Realtime Database
                userBillsRef.child(mBillId).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            // Error occurred while deleting the data
                            Toast.makeText(billdetailactivity.this, "Failed to delete bill", Toast.LENGTH_SHORT).show();
                        } else {
                            // Data deleted successfully
                            Toast.makeText(billdetailactivity.this, "Bill deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
        });

        // Set up the No button in the AlertDialog
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });

        // Display the AlertDialog
        builder.show();
    }

    private boolean isDueDateNear(Date dueDate) {
        // Compare the due date with the current date
        Date currentDate = new Date();
        long difference = dueDate.getTime() - currentDate.getTime();
        long daysDifference = difference / (24 * 60 * 60 * 1000); // Convert milliseconds to days

        // Return true if the due date is today or within the next 2 days
        return daysDifference <= 2 && daysDifference >= 0;
    }

    private void checkDueDateReminder(String dueDate) {
        try {
            Date dueDateObj = new SimpleDateFormat("dd/MM/yyyy").parse(dueDate);
            if (isDueDateNear(dueDateObj)) {
                String title = "Due Date Reminder";
                String message = "";

                // Determine the message based on the due date
                Date currentDate = new Date();
                long difference = dueDateObj.getTime() - currentDate.getTime();
                long daysDifference = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS);

                if (daysDifference == 2) {
                    // Calculate the difference in terms of days without considering the time
                    long differenceInDays = TimeUnit.MILLISECONDS.toDays(difference);
                    // Get the current time in milliseconds
                    long currentTimeMillis = System.currentTimeMillis();
                    // Calculate the time of tomorrow in milliseconds
                    long tomorrowTimeMillis = TimeUnit.DAYS.toMillis(1) + currentTimeMillis;

                    // If the due date is within tomorrow (less than tomorrow's time but not today)
                    if (dueDateObj.getTime() < tomorrowTimeMillis && dueDateObj.getTime() >= currentTimeMillis) {
                        title = "Due Date Reminder";
                        message = "The bill is due soon. Please make a payment.";
                    }
                } else if (daysDifference == 1) {
                    title = "Due Date Reminder";
                    message = "The bill is due soon. Please make a payment.";
                } else if (daysDifference == 0) {
                    title = "Due Date Reminder";
                    message = "The bill is due today. Please make a payment.";
                }



                if (!message.isEmpty()) {
                    showPopupNotification(this, title, message);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void showPopupNotification(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Close the dialog
                dialog.dismiss();
            }
        });
        builder.show();
    }
}