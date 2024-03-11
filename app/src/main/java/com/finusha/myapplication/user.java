package com.finusha.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class user extends AppCompatActivity {

    private TextView textViewName;
    private TextView textViewEmail;
    private Button buttonEdit;
    private ImageView imageViewProfile;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        storageRef = FirebaseStorage.getInstance().getReference().child("ProfilePictures");

        textViewName = findViewById(R.id.textViewName);
        textViewEmail = findViewById(R.id.textViewEmail);
        buttonEdit = findViewById(R.id.buttonEdit);
        imageViewProfile = findViewById(R.id.imageViewProfile);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            String email = user.getEmail();

            if (displayName != null) {
                textViewName.setText(displayName);
            }
            if (email != null) {
                textViewEmail.setText(email);
            }

            // Retrieve and display the current profile picture
            userRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("profilePictureUrl")) {
                        String profilePictureUrl = dataSnapshot.child("profilePictureUrl").getValue().toString();

                        RequestOptions requestOptions = new RequestOptions()
                                .placeholder(R.drawable.default_pro)
                                .error(R.drawable.default_pro);

                        Glide.with(user.this)
                                .load(profilePictureUrl)
                                .apply(requestOptions)
                                .into(imageViewProfile);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database error
                }
            });
        }

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate back to the Settings activity
                Intent settingsIntent = new Intent(user.this, settings.class);
                startActivity(settingsIntent);
            }
        });
    }
}
