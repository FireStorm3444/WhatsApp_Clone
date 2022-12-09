package com.project1.community;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.project1.community.Models.Users;
import com.project1.community.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {
    //to access all ids
    ActivitySignupBinding binding;
    //FireBase
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    FirebaseStorage storage;

    //to get Image from gallery
    Uri imageUri;
    //for saving Image in FirebaseStorage
    String imageURI;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        getSupportActionBar().hide();

        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        progressDialog = new ProgressDialog(SignupActivity.this);
        progressDialog.setTitle("Creating Account");
        progressDialog.setMessage("Please Wait");

        binding.btnSignUp.setOnClickListener(view -> {


            //Validations
            if(binding.txtUsername.getText().toString().trim().isEmpty()){
                binding.txtUsername.setError("Username is Required!!");
                binding.txtUsername.requestFocus();
                return;
            }
            if (binding.txtEmail.getText().toString().trim().isEmpty()) {
                binding.txtEmail.setError("Email is Required!!");
                binding.txtEmail.requestFocus();
                return;
            }
            //if Email is not valid
            if (!Patterns.EMAIL_ADDRESS.matcher(binding.txtEmail.getText().toString().trim()).matches()) {
                binding.txtEmail.setError("Enter valid Email!!");
                binding.txtEmail.requestFocus();
                return;
            }
            if (binding.txtPassword.getText().toString().trim().isEmpty()) {
                binding.txtPassword.setError("Password is Required!!");
                binding.txtPassword.requestFocus();
                return;
            }
            if (binding.txtPassword.getText().toString().trim().length() < 6) {
                binding.txtPassword.setError("Min Password length should be 6 Characters!!");
                binding.txtPassword.requestFocus();
                return;
            }
            if (binding.txtConfirmPassword.getText().toString().trim().isEmpty()) {
                binding.txtConfirmPassword.setError("Confirm Password");
                binding.txtConfirmPassword.requestFocus();
                return;
            }
            //if Password and Confirm Password does not match
            if (!binding.txtPassword.getText().toString().trim().equals(binding.txtConfirmPassword.getText().toString().trim())){
                binding.txtConfirmPassword.setError("Passwords do not match!!");
                binding.txtConfirmPassword.requestFocus();
                return;
            }

            //starting Signup Process
            progressDialog.show();
            mAuth.createUserWithEmailAndPassword(binding.txtEmail.getText().toString().trim(), binding.txtPassword.getText().toString().trim())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            progressDialog.dismiss();

                            if (task.isSuccessful()) {
                                Users user = new Users(binding.txtUsername.getText().toString().trim(),binding.txtEmail.getText().toString().trim(), binding.txtPassword.getText().toString().trim());
                                String id = task.getResult().getUser().getUid();
                                database.getReference().child("Users").child(id).setValue(user);

                                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                //when user touches back then App will close without going back to LoginActivity
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid()).child("currentStatus").setValue("Online");
                                Toast.makeText(SignupActivity.this, "SignUp Successful!!", Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(SignupActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });

        //open Login Activity
        binding.tvSignIn.setOnClickListener(view -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }
}