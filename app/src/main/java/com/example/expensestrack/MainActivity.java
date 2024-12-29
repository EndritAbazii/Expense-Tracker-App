package com.example.expensestrack;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensestrack.Activity.AddExpenseActivity;
import com.example.expensestrack.Adapters.ExpenseAdapter;
import com.example.expensestrack.Db.DatabaseHelper;
import com.example.expensestrack.Model.Expense;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    int userId;
    DatabaseHelper db;
    RecyclerView recyclerView;
    ExpenseAdapter adapter;
    TextView tvTotal; // TextView to display total amount

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userId = getIntent().getIntExtra("USER_ID", -1);
        db = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        tvTotal = findViewById(R.id.tvTotal);

        loadExpenses();

        FloatingActionButton fabAdd = findViewById(R.id.fabAddExpense);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });
    }

    private void loadExpenses() {
        Cursor cursor = db.getUserExpenses(userId);
        ArrayList<Expense> expenseList = new ArrayList<>();
        double totalAmount = 0.0; // Initialize sum

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE));
            double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY));

            // Add to the list
            expenseList.add(new Expense(id, userId, title, amount, date, category));
            // Accumulate total
            totalAmount += amount;
        }
        cursor.close();

        // Update the adapter
        adapter = new ExpenseAdapter(expenseList, this, db);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Display total
        tvTotal.setText("Total: " + totalAmount);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpenses();
    }
}
