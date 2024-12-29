package com.example.expensestrack.Model;

public class Expense {
   public int id;
    public int userId;
   public String title;
   public double amount;
   public String date;
   public String category;
    // constructor + getters/setters
    public Expense(int id, int userId, String title, double amount, String date, String category) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.category = category;
    }
}
