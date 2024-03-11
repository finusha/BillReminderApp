package com.finusha.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class home extends AppCompatActivity {

    private DatabaseReference userBillsRef;
    private List<Bill> billsList = new ArrayList<>();
    private ListView listView;
    private ArrayAdapter<Bill> adapter;
    Button addBillButton;
    ImageButton btnBell;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User not authenticated, handle this case
            Intent intent = new Intent(home.this, Login.class);
            startActivity(intent);
            finish();
            return;
        }

        userBillsRef = FirebaseDatabase.getInstance().getReference()
                .child("user_bills")
                .child(currentUser.getUid());
        btnBell = findViewById(R.id.btn_bell);
        listView = findViewById(R.id.listView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, billsList);
        listView.setAdapter(adapter);

        Query query = userBillsRef.orderByChild("dueDate");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                billsList.clear();
                Date currentDate = new Date();

                for (DataSnapshot billSnapshot : dataSnapshot.getChildren()) {
                    Bill bill = billSnapshot.getValue(Bill.class);
                    Date dueDate;
                    try {
                        dueDate = bill.getDueDateAsDate();
                    } catch (ParseException e) {
                        e.printStackTrace();
                        continue;
                    }
                    if (isSameDay(dueDate, currentDate) || dueDate.after(currentDate) || isDueToday(dueDate)) {
                        billsList.add(bill);
                    }
                }

                Collections.sort(billsList, new Comparator<Bill>() {
                    @Override
                    public int compare(Bill bill1, Bill bill2) {
                        Date dueDate1 = null;
                        Date dueDate2 = null;
                        try {
                            dueDate1 = bill1.getDueDateAsDate();
                            dueDate2 = bill2.getDueDateAsDate();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (dueDate1 != null && dueDate2 != null) {
                            return dueDate1.compareTo(dueDate2);
                        }
                        return 0;
                    }
                });

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ListActivity", "Database error: " + databaseError.getMessage());
            }
        });

        addBillButton = findViewById(R.id.button);
        addBillButton.setOnClickListener(v -> {
            Intent intent = new Intent(home.this, newbill.class);
            startActivity(intent);
        });

        // Set an OnClickListener to the btn_bell button
        btnBell.setOnClickListener(v -> {
            Intent intent = new Intent(home.this, notifications.class);
            startActivity(intent);
        });

        // Set up the navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        navigationView.setNavigationItemSelectedListener(item -> {
            // Handle navigation drawer item clicks here
            int id = item.getItemId();

            if (id == R.id.nav_settings) {
                Intent intent = new Intent(home.this, user.class);
                startActivity(intent);

            } else if (id == R.id.nav_logout) {
                // Handle Logout item click
                logout();
            }

            drawerLayout.closeDrawers();
            return true;
        });

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // Define the logout method
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(home.this, Login.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Activate the navigation drawer toggle when the hamburger icon is clicked
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isDueToday(Date dueDate) {
        Date currentDate = new Date();
        return isSameDay(dueDate, currentDate);
    }

    private boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return fmt.format(date1).equals(fmt.format(date2));
    }
}
