package com.example.expensestrack.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.expensestrack.Db.DatabaseHelper;
import com.example.expensestrack.Model.Expense;
import com.example.expensestrack.R;

public class AddExpenseActivity extends AppCompatActivity {
    private EditText etTitle, etAmount, etDate, etCategory;
    private Button btnSave;
    private DatabaseHelper db;
    private int userId;
    private int expenseId = -1; // If editing, we'll use this

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        etTitle = findViewById(R.id.etTitle);
        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate);
        etCategory = findViewById(R.id.etCategory);
        btnSave = findViewById(R.id.btnSave);

        db = new DatabaseHelper(this);
        userId = getIntent().getIntExtra("USER_ID", -1);


        if (getIntent().hasExtra("EXPENSE_ID")) {
            expenseId = getIntent().getIntExtra("EXPENSE_ID", -1);
            loadExpenseDetails(expenseId);
        }

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String category = etCategory.getText().toString().trim();

            if (title.isEmpty() || amountStr.isEmpty() || date.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success;
            if (expenseId == -1) {
                // Adding new expense
                success = db.addExpense(userId, title, amount, date, category);
            } else {
                // Updating existing expense
                success = db.updateExpense(expenseId, title, amount, date, category);
            }

            if (success) {
                Toast.makeText(this, "Expense saved", Toast.LENGTH_SHORT).show();

                // Only show a notification when adding a new expense, not updating
                if (expenseId == -1) {
                    showExpenseAddedNotification(title, amount);
                }

                finish();
            } else {
                Toast.makeText(this, "Error saving expense", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExpenseDetails(int expenseId) {
        // Query the database for this expense
        Cursor cursor = db.getUserExpenses(userId);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_ID));
            if (id == expenseId) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY));

                etTitle.setText(title);
                etAmount.setText(String.valueOf(amount));
                etDate.setText(date);
                etCategory.setText(category);
                break;
            }
        }
        cursor.close();
    }

    private void showExpenseAddedNotification(String title, double amount) {
        // For Android 13 and above, we need to check notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request the permission if not granted
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
                // You might want to handle the result in onRequestPermissionsResult
                // If permission is not granted, we won't show the notification now
                return;
            }
        }

        String channelId = "expense_notifications";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_background) // ensure this icon exists in your drawable folder
                .setContentTitle("Expense Added")
                .setContentText("You added a new expense: " + title + " - $" + amount)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If needed, handle the permission request result here
        // If permission granted after request, you could try showing the notification again
    }
}
