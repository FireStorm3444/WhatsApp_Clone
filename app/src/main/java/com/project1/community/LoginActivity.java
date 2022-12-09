package com.project1.community;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.database.FirebaseDatabase;
import com.project1.community.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;

    private FirebaseAuth mAuth;
    FirebaseDatabase database;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle("Signing In");
        progressDialog.setMessage("Please Wait");

        binding.btnSignIn.setOnClickListener(view -> {
            if (binding.txtEmail.getText().toString().trim().isEmpty()) {
                binding.txtEmail.setError("Email is Required!!");
                binding.txtEmail.requestFocus();
                return;
            }
            //if Email is not valid
            if(!Patterns.EMAIL_ADDRESS.matcher(binding.txtEmail.getText().toString().trim()).matches()) {
                binding.txtEmail.setError("Enter valid Email!!");
                binding.txtEmail.requestFocus();
                return;
            }
            if(binding.txtPassword.getText().toString().trim().isEmpty()) {
                binding.txtPassword.setError("Password is Required!!");
                binding.txtPassword.requestFocus();
                return;
            }
            if (binding.txtPassword.getText().toString().trim().length() < 6) {
                binding.txtPassword.setError("Min Password length should be 6 Characters!!");
                binding.txtPassword.requestFocus();
                return;
            }

            //starting SignIn Activity
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(binding.txtEmail.getText().toString().trim(), binding.txtPassword.getText().toString().trim())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                //when user touches back then App will close without going back to LoginActivity
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                //start MainActivity
                                startActivity(intent);

                                //changing currentStatus to Online
                                FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid()).child("currentStatus").setValue("Online");
                                Toast.makeText(LoginActivity.this, "SignIn Successful", Toast.LENGTH_LONG).show();
                            } else {
                                //if Password is incorrect
                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    binding.txtPassword.setError("Wrong Password!!");
                                    binding.txtPassword.requestFocus();
                                    return;
                                }
                                //error in any other cases
                                Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });

        //if User is already Signed in the send him directly to main Activity
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, SplashActivity.class);
            //when user touches back then App will close without going back to LoginActivity
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        }

        //Open SignUp Activity
        binding.tvSignUp.setOnClickListener(view -> {
            startActivity(new Intent(this, SignupActivity.class));
        });
    }
}