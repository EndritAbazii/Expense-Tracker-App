package com.example.expensestrack.Db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "expense_tracker.db";
    private static final int DATABASE_VERSION = 2; // Increment version after schema change

    // Users Table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD_SALT = "password_salt";
    public static final String COLUMN_PASSWORD_HASH = "password_hash";

    // Expenses Table
    public static final String TABLE_EXPENSES = "expenses";
    public static final String COLUMN_EXPENSE_ID = "id";
    public static final String COLUMN_USER_ID_FK = "user_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_CATEGORY = "category";

    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT UNIQUE, " +
                    COLUMN_PASSWORD_SALT + " BLOB, " +
                    COLUMN_PASSWORD_HASH + " BLOB );";

    private static final String CREATE_EXPENSES_TABLE =
            "CREATE TABLE " + TABLE_EXPENSES + " (" +
                    COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID_FK + " INTEGER, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_AMOUNT + " REAL, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_CATEGORY + " TEXT," +
                    "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_EXPENSES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // User Operations
    public boolean registerUser(String username, String plainPassword) {
        SQLiteDatabase db = null;
        long result = -1;
        try {
            db = this.getWritableDatabase();
            byte[] salt = PasswordUtils.generateSalt();
            byte[] hash = PasswordUtils.hashPassword(plainPassword.toCharArray(), salt);

            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, username);
            values.put(COLUMN_PASSWORD_SALT, salt);
            values.put(COLUMN_PASSWORD_HASH, hash);

            result = db.insert(TABLE_USERS, null, values);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error while trying to register user: " + e.getMessage(), e);
        }

        return result != -1; // If -1, insert failed
    }

    public boolean checkUserCredentials(String username, String plainPassword) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_PASSWORD_SALT, COLUMN_PASSWORD_HASH};
        String selection = COLUMN_USERNAME + "=?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            byte[] salt = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD_SALT));
            byte[] storedHash = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH));
            cursor.close();

            return PasswordUtils.verifyPassword(plainPassword, salt, storedHash);
        }

        if (cursor != null) cursor.close();
        return false;
    }

    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USERNAME + "=?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
        }
        cursor.close();
        return userId;
    }

    // Expense Operations
    public boolean addExpense(int userId, String title, double amount, String date, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID_FK, userId);
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_CATEGORY, category);

        long result = db.insert(TABLE_EXPENSES, null, values);
        return result != -1;
    }

    public Cursor getUserExpenses(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EXPENSES, null, COLUMN_USER_ID_FK + "=?", new String[]{String.valueOf(userId)}, null, null, COLUMN_DATE + " DESC");
    }

    public boolean updateExpense(int expenseId, String title, double amount, String date, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_CATEGORY, category);

        int rows = db.update(TABLE_EXPENSES, values, COLUMN_EXPENSE_ID + "=?", new String[]{String.valueOf(expenseId)});
        return rows > 0;
    }

    public boolean deleteExpense(int expenseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_EXPENSES, COLUMN_EXPENSE_ID + "=?", new String[]{String.valueOf(expenseId)});
        return rows > 0;
    }

    public boolean resetUserPassword(String username, String newPlainPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            byte[] newSalt = PasswordUtils.generateSalt();
            byte[] newHash = PasswordUtils.hashPassword(newPlainPassword.toCharArray(), newSalt);

            ContentValues values = new ContentValues();
            values.put(COLUMN_PASSWORD_SALT, newSalt);
            values.put(COLUMN_PASSWORD_HASH, newHash);

            int rows = db.update(TABLE_USERS, values, COLUMN_USERNAME + "=?", new String[]{username});
            return rows > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error resetting password: " + e.getMessage(), e);
            return false;
        }
    }

}
