package com.example.expensestrack.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensestrack.Activity.AddExpenseActivity;
import com.example.expensestrack.Db.DatabaseHelper;
import com.example.expensestrack.Model.Expense;
import com.example.expensestrack.R;

import java.util.ArrayList;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    ArrayList<Expense> expenseList;
    Context context;
    DatabaseHelper db;

    public ExpenseAdapter(ArrayList<Expense> expenseList, Context context, DatabaseHelper db) {
        this.expenseList = expenseList;
        this.context = context;
        this.db = db;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        holder.tvTitle.setText(expense.title);
        holder.tvAmount.setText(String.valueOf(expense.amount));
        holder.tvDate.setText(expense.date);
        holder.tvCategory.setText(expense.category);

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddExpenseActivity.class);
            intent.putExtra("USER_ID", expense.userId);
            intent.putExtra("EXPENSE_ID", expense.id);
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            boolean success = db.deleteExpense(expense.id);
            if (success) {
                expenseList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, expenseList.size());
            } else {
                Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAmount, tvDate, tvCategory;
        ImageButton btnEdit, btnDelete;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

