package com.finusha.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;

public class newbill extends AppCompatActivity {

    EditText billNameInput, amountDueInput, dueDateInput;
    TextView dueDateLabel;
    Button saveButton, listButton;
    DatePickerDialog datePickerDialog;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newbill);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize UI elements
        billNameInput = findViewById(R.id.bill_name_input);
        amountDueInput = findViewById(R.id.amount_due_input);
        dueDateInput = findViewById(R.id.due_date_input);
        dueDateLabel = findViewById(R.id.due_date_label);
        saveButton = findViewById(R.id.save_button);
        listButton = findViewById(R.id.list_button);

        //For dueDateInput to show a date picker dialog
        dueDateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                datePickerDialog = new DatePickerDialog(newbill.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                dueDateInput.setText(date);
                            }
                        }, year, month, dayOfMonth);
                datePickerDialog.show();
            }
        });

        //For saveButton to save the bill
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBill();
            }
        });

        // Set click listener for listButton to navigate to the bill list screen
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(newbill.this, list.class);
                startActivity(intent);
            }
        });
    }

    // Save the bill to Firebase Database
    private void saveBill() {
        String billName = billNameInput.getText().toString().trim();
        String amountDue = amountDueInput.getText().toString().trim();
        String dueDate = dueDateInput.getText().toString().trim();

        // Check if any field is empty
        if (billName.isEmpty() || amountDue.isEmpty() || dueDate.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show();
            return;
        }

        // Get the current authenticated user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show();
            return;
        }

        // Create a separate node for each user's bills
        DatabaseReference userBillsRef = databaseReference.child("user_bills")
                .child(currentUser.getUid());

        // Generate a unique key for the bill
        String id = userBillsRef.push().getKey();

        // Create a Bill object
        Bill bill = new Bill(id, billName, amountDue, dueDate);

        // Save the bill to the database
        userBillsRef.child(id).setValue(bill);

        Toast.makeText(this, "Bill saved successfully", Toast.LENGTH_LONG).show();

        // Clear input fields
        billNameInput.setText("");
        amountDueInput.setText("");
        dueDateInput.setText("");
    }
}
