package com.project1.community;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.project1.community.Models.Users;
import com.project1.community.databinding.ActivitySettingsBinding;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;

    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().hide();

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        //back Button
        binding.backArrow.setOnClickListener(view -> {
            startActivity(new Intent(SettingsActivity.this, MainActivity.class));
        });

        //AddImage Button
        binding.plus.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), 69);
        });

        //Save Button

        binding.btnSave.setOnClickListener(view -> {

            String username = binding.txtUsername.getText().toString().trim();
            String about = binding.txtAbout.getText().toString().trim();
            if (username.isEmpty()) {
                binding.txtUsername.setError("Username Required");
                binding.txtUsername.requestFocus();
                return;
            }

            HashMap<String, Object> obj = new HashMap<>();
            obj.put("userName",username);
            obj.put("about", about);

            database.getReference().child("Users").child(auth.getUid())
                    .updateChildren(obj);

            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
        });

        //get and set Profile Pic
        database.getReference().child("Users").child(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users users = snapshot.getValue(Users.class);
                        //get and set image
                        Picasso.get().load(users.getProfilePic())
                                .placeholder(R.drawable.avatar3)
                                .into(binding.profileImage);

                        binding.txtUsername.setText(users.getUserName());
                        binding.txtAbout.setText(users.getAbout());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.txtViewPP.setOnClickListener(view -> Toast.makeText(this, "Koi Privacy Policy nahi hai :(", Toast.LENGTH_SHORT).show());
        binding.txtViewAboutUs.setOnClickListener(view -> Toast.makeText(this, "Batao kya jaanna hai", Toast.LENGTH_SHORT).show());
        binding.txtViewHelp.setOnClickListener(view -> Toast.makeText(this, "Bataiye kya help kr sakta hu", Toast.LENGTH_SHORT).show());
        binding.txtViewInvite.setOnClickListener(view -> Toast.makeText(this, "Koi jarurat nahi hai Invite krne ki", Toast.LENGTH_SHORT).show());
        binding.txtViewNoti.setOnClickListener(view -> Toast.makeText(this, "Nahi Ayenge.. Kya kr loge?", Toast.LENGTH_SHORT).show());
    }

    //getting Image From user and saving imageUrl in database and image in Storage
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 69) {
            if(data.getData() != null){

                Uri sFile = data.getData();
                binding.profileImage.setImageURI(sFile);  //This profileImage is in SettingsActivity

                final StorageReference reference = storage.getReference().child("profile_pic")
                        .child(FirebaseAuth.getInstance().getUid());

                //storing Download Url in "Users"
                reference.putFile(sFile).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageURI = uri.toString();
                                    database.getReference().child("Users").child(auth.getUid())
                                            .child("profilePic").setValue(imageURI);
                                }
                            });
                        } else {
                            Toast.makeText(SettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
    }
}