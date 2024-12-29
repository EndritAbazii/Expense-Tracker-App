
package com.example.expensestrack.Activity;



import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensestrack.Db.DatabaseHelper;
import com.example.expensestrack.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    EditText etUsername, etNewPassword, etConfirmNewPassword;
    Button btnResetPassword;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etUsername = findViewById(R.id.etUsername);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        db = new DatabaseHelper(this);

        btnResetPassword.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmNewPassword.getText().toString().trim();

            if (!validateInputs(username, newPassword, confirmPassword)) return;

            boolean success = db.resetUserPassword(username, newPassword);
            if (success) {
                Toast.makeText(this, "Password reset successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to reset password. Username may not exist.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs(String username, String newPassword, String confirmPassword) {
        if (username.isEmpty()) {
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPassword.isEmpty()) {
            Toast.makeText(this, "New password cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Optionally, you can re-use your strong password check here
        if (!isStrongPassword(newPassword)) {
            Toast.makeText(this, "New password must be strong", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isStrongPassword(String password) {
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



























































































