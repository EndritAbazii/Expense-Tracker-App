package com.example.expensestrack.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensestrack.Db.DatabaseHelper;
import com.example.expensestrack.R;

public class RegisterActivity extends AppCompatActivity {
    EditText etUsername, etPassword, etConfirmPassword;
    Button btnRegister;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword); // Add this field in XML
        btnRegister = findViewById(R.id.btnRegister);
        TextView tvUsernameError = findViewById(R.id.tvUsernameError);
        TextView tvPasswordError = findViewById(R.id.tvPasswordError);
        TextView tvConfirmPasswordError = findViewById(R.id.tvConfirmPasswordError);

        db = new DatabaseHelper(this);

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            tvUsernameError.setVisibility(View.GONE);
            tvPasswordError.setVisibility(View.GONE);
            tvConfirmPasswordError.setVisibility(View.GONE);

            if (!validateInputs(username, password, confirmPassword, tvUsernameError, tvPasswordError, tvConfirmPasswordError)) {
                return; // If validation fails, do not proceed with registration.
            }

            try {
                boolean success = db.registerUser(username, password);
                if (success) {
                    Toast.makeText(this, "User Registered Successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Return to the login screen
                } else {
                    // Registration failed for some reason (e.g., DB insert failure)
                    Toast.makeText(this, "Registration Failed, please try again.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                // This catch block might never be reached if we handled it inside registerUser,
                // but it's here as an extra safety measure or if other code throws an exception.
                Toast.makeText(this, "An unexpected error occurred. Please try again later.", Toast.LENGTH_LONG).show();
                Log.e("RegisterActivity", "Error during registration: " + e.getMessage(), e);
            }
        });
    }

    private boolean validateInputs(String username, String password, String confirmPassword,
                                   TextView tvUsernameError, TextView tvPasswordError, TextView tvConfirmPasswordError) {
        boolean isValid = true;

        // Validate username
        if (username.isEmpty()) {
            tvUsernameError.setText("Username cannot be empty");
            tvUsernameError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!isValidEmail(username)) {
            tvUsernameError.setText("Invalid email format");
            tvUsernameError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Validate password
        if (password.isEmpty()) {
            tvPasswordError.setText("Password cannot be empty");
            tvPasswordError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!isStrongPassword(password)) {
            tvPasswordError.setText("Password must be at least 8 chars long, contain a digit, and a special character");
            tvPasswordError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Validate confirm password
        if (!password.equals(confirmPassword)) {
            tvConfirmPasswordError.setText("Passwords do not match");
            tvConfirmPasswordError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidEmail(String email) {
        // Regular expression for validating an email address
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    private boolean isStrongPassword(String password) {
        // Example rule:
        // - At least 8 characters
        // - At least one digit
        // - At least one special character (!@#$%^&* for example)
        if (password.length() < 8) return false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;
        String specialChars = "!@#$%^&*()_+{}|:<>?-=[]\\;',./`~";

        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) hasDigit = true;
            if (specialChars.indexOf(c) >= 0) hasSpecialChar = true;
            if (hasDigit && hasSpecialChar) return true;
        }
        return false;
    }

}
