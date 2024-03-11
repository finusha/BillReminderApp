package com.finusha.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class list extends AppCompatActivity {

    private DatabaseReference billsRef;
    private List<Bill> billsList = new ArrayList<>();
    private ListView listView;
    private ArrayAdapter<Bill> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Get a reference to the bills node in the Firebase database for the current user
        billsRef = FirebaseDatabase.getInstance().getReference().child("user_bills")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        listView = findViewById(R.id.listView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, billsList);
        listView.setAdapter(adapter);

        billsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                billsList.clear();

                for (DataSnapshot billSnapshot : dataSnapshot.getChildren()) {
                    // Get the Bill object from the snapshot
                    Bill bill = billSnapshot.getValue(Bill.class);
                    billsList.add(bill);
                }

                // Sort the bills list by due date in ascending order
                Collections.sort(billsList, Bill.sortByDueDateAsc);

                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ListActivity", "Database error: " + databaseError.getMessage());
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected bill from the list adapter
                Bill selectedBill = (Bill) parent.getItemAtPosition(position);
                String billId = selectedBill.getId();
                String billName = selectedBill.getBillName();
                String dueDate = selectedBill.getDueDate();
                String amountDue = selectedBill.getAmountDue();

                // Create an intent to start the BillDetailActivity and pass the selected bill details
                Intent intent = new Intent(list.this, billdetailactivity.class);
                intent.putExtra("billId", billId);
                intent.putExtra("billName", billName);
                intent.putExtra("dueDate", dueDate);
                intent.putExtra("amountDue", amountDue);

                startActivity(intent);
            }
        });
    }
}
