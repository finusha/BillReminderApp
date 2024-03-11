package com.finusha.myapplication;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class settings extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonSave;
    private Button buttonChangePicture;
    private CircleImageView imageViewProfile;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        storageRef = FirebaseStorage.getInstance().getReference().child("ProfilePictures");

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSave = findViewById(R.id.buttonSave);
        buttonChangePicture = findViewById(R.id.buttonChangePicture);
        imageViewProfile = findViewById(R.id.imageViewProfile);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            String email = user.getEmail();

            if (displayName != null) {
                editTextName.setText(displayName);
            }
            if (email != null) {
                editTextEmail.setText(email);
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

                        Glide.with(settings.this)
                                .load(profilePictureUrl)
                                .apply(requestOptions)
                                .into(imageViewProfile);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(settings.this, "Failed to retrieve profile picture", Toast.LENGTH_SHORT).show();
                }
            });
        }

        buttonChangePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            // Set the selected image to the CircleImageView
            imageViewProfile.setImageURI(imageUri);
        }
    }

    private void saveChanges() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) && TextUtils.isEmpty(email) && TextUtils.isEmpty(password) && imageUri == null) {
            Toast.makeText(this, "No changes made", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (!TextUtils.isEmpty(name)) {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build();

                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(settings.this, "Display name updated", Toast.LENGTH_SHORT).show();
                                    userRef.child(user.getUid()).child("name").setValue(name);
                                } else {
                                    Toast.makeText(settings.this, "Failed to update display name", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            if (!TextUtils.isEmpty(email)) {
                user.updateEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(settings.this, "Email updated", Toast.LENGTH_SHORT).show();
                                    userRef.child(user.getUid()).child("email").setValue(email);
                                } else {
                                    Toast.makeText(settings.this, "Failed to update email", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            if (!TextUtils.isEmpty(password)) {
                user.updatePassword(password)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(settings.this, "Password updated", Toast.LENGTH_SHORT).show();
                                    userRef.child(user.getUid()).child("password").setValue(password);
                                } else {
                                    Toast.makeText(settings.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            if (imageUri != null) {
                final StorageReference imageRef = storageRef.child(user.getUid() + ".png");
                UploadTask uploadTask = imageRef.putFile(imageUri);
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return imageRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            if (downloadUri != null) {
                                String profilePictureUrl = downloadUri.toString();
                                userRef.child(user.getUid()).child("profilePictureUrl").setValue(profilePictureUrl);
                                Toast.makeText(settings.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(settings.this, "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }

        // Start User activity
        Intent intent = new Intent(settings.this, user.class);
        startActivity(intent);
    }
}
