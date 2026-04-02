package com.example.myshelf;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "myshelf.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_INVENTORY = "INVENTORY_ITEM";
    public static final String COL_INV_ID = "inv_id";
    public static final String COL_ITEM_NAME = "item_name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_EXPIRY_DATE = "expiry_date";
    public static final String COL_CATEGORY = "category";
    public static final String COL_PRICE = "price";
    public static final String COL_STATUS = "status";

    public static final String TABLE_RECIPE = "RECIPE";
    public static final String COL_RECIPE_ID = "recipe_id";
    public static final String COL_RECIPE_NAME = "recipe_name";
    public static final String COL_REQUIRED_CATEGORIES = "required_categories";
    public static final String COL_INSTRUCTION = "instruction";

    public static final String TABLE_HISTORY = "HISTORY_LOG";
    public static final String COL_LOG_ID = "log_id";
    public static final String COL_ACTION_DATE = "action_date";
    public static final String COL_FINAL_STATUS = "final_status";
    public static final String COL_PRICE_LOSS = "price_loss";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createInventoryTable = "CREATE TABLE " + TABLE_INVENTORY + " (" +
                COL_INV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ITEM_NAME + " TEXT, " +
                COL_QUANTITY + " INTEGER, " +
                COL_EXPIRY_DATE + " TEXT, " +
                COL_CATEGORY + " TEXT, " +
                COL_PRICE + " REAL, " +
                COL_STATUS + " TEXT)";
        db.execSQL(createInventoryTable);

        String createRecipeTable = "CREATE TABLE " + TABLE_RECIPE + " (" +
                COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_NAME + " TEXT, " +
                COL_REQUIRED_CATEGORIES + " TEXT, " +
                COL_INSTRUCTION + " TEXT)";
        db.execSQL(createRecipeTable);

        String createHistoryTable = "CREATE TABLE " + TABLE_HISTORY + " (" +
                COL_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ITEM_NAME + " TEXT, " +
                COL_ACTION_DATE + " TEXT, " +
                COL_FINAL_STATUS + " TEXT, " +
                COL_PRICE_LOSS + " REAL, " +
                COL_INV_ID + " INTEGER, " +
                "FOREIGN KEY(" + COL_INV_ID + ") REFERENCES " + TABLE_INVENTORY + "(" + COL_INV_ID + "))";
        db.execSQL(createHistoryTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        onCreate(db);
    }

    // Add new item to inventory
    public boolean insertInventoryItem(InventoryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ITEM_NAME, item.getItemName());
        values.put(COL_QUANTITY, item.getQuantity());
        values.put(COL_EXPIRY_DATE, item.getExpiryDate());
        values.put(COL_CATEGORY, item.getCategory());
        values.put(COL_PRICE, item.getPrice());
        values.put(COL_STATUS, item.getStatus());
        long result = db.insert(TABLE_INVENTORY, null, values);
        db.close();
        return result != -1;
    }

    // Fetch all active items and sort them by closest expiry date
    public List<InventoryItem> getActiveInventory() {
        List<InventoryItem> itemList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_INVENTORY + " WHERE " + COL_STATUS + " = 'Active'";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                InventoryItem item = new InventoryItem();
                item.setInvId(cursor.getInt(0));
                item.setItemName(cursor.getString(1));
                item.setQuantity(cursor.getInt(2));
                item.setExpiryDate(cursor.getString(3));
                item.setCategory(cursor.getString(4));
                item.setPrice(cursor.getDouble(5));
                item.setStatus(cursor.getString(6));
                itemList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        // Sort list ascending by expiry date
        java.util.Collections.sort(itemList, new java.util.Comparator<InventoryItem>() {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());

            @Override
            public int compare(InventoryItem item1, InventoryItem item2) {
                try {
                    java.util.Date date1 = sdf.parse(item1.getExpiryDate());
                    java.util.Date date2 = sdf.parse(item2.getExpiryDate());
                    if (date1 != null && date2 != null) {
                        return date1.compareTo(date2); // Ascending order
                    }
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });

        return itemList;
    }

    // Mark item as consumed or expired
    public boolean markItemAs(InventoryItem item, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues updateValues = new ContentValues();
            updateValues.put(COL_STATUS, newStatus);
            db.update(TABLE_INVENTORY, updateValues, COL_INV_ID + "=?", new String[]{String.valueOf(item.getInvId())});

            ContentValues historyValues = new ContentValues();
            historyValues.put(COL_ITEM_NAME, item.getItemName());
            String todayDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            historyValues.put(COL_ACTION_DATE, todayDate);
            historyValues.put(COL_FINAL_STATUS, newStatus);
            historyValues.put(COL_PRICE_LOSS, item.getPrice());
            historyValues.put(COL_INV_ID, item.getInvId());
            db.insert(TABLE_HISTORY, null, historyValues);

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // Auto deduction of item quantity and log to history
    public void deductItemQuantity(InventoryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (item.getQuantity() > 1) {
            double unitPrice = item.getPrice() / item.getQuantity();
            int newQuantity = item.getQuantity() - 1;
            double newTotalPrice = item.getPrice() - unitPrice;

            // Update item quantity and price in inventory
            ContentValues values = new ContentValues();
            values.put(COL_QUANTITY, newQuantity);
            values.put(COL_PRICE, newTotalPrice);
            db.update(TABLE_INVENTORY, values, COL_INV_ID + " = ?",
                    new String[]{String.valueOf(item.getInvId())});

            // Add a log to History table
            ContentValues historyValues = new ContentValues();
            historyValues.put(COL_ITEM_NAME, item.getItemName() + " (1 unit)");
            String todayDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            historyValues.put(COL_ACTION_DATE, todayDate);
            historyValues.put(COL_FINAL_STATUS, "Consumed");
            historyValues.put(COL_PRICE_LOSS, unitPrice);
            historyValues.put(COL_INV_ID, item.getInvId());
            db.insert(TABLE_HISTORY, null, historyValues);

        } else {
            // If only 1 remains, use the existing method which handles both Update and History
            markItemAs(item, "Consumed");
        }
        db.close();
    }

    // Get suggested recipes
    public List<RecipeItem> getSuggestedRecipes() {
        List<RecipeItem> suggestedList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> availableCategories = new ArrayList<>();
        
        // Use TRIM to ensure no accidental spaces break the matching
        String catQuery = "SELECT DISTINCT TRIM(" + COL_CATEGORY + ") FROM " + TABLE_INVENTORY + " WHERE " + COL_STATUS + " = 'Active'";
        Cursor catCursor = db.rawQuery(catQuery, null);
        if (catCursor.moveToFirst()) {
            do {
                availableCategories.add(catCursor.getString(0).toLowerCase());
            } while (catCursor.moveToNext());
        }
        catCursor.close();

        String recipeQuery = "SELECT * FROM " + TABLE_RECIPE;
        Cursor recipeCursor = db.rawQuery(recipeQuery, null);
        if (recipeCursor.moveToFirst()) {
            do {
                int id = recipeCursor.getInt(0);
                String name = recipeCursor.getString(1);
                String reqCats = recipeCursor.getString(2);
                String instruction = recipeCursor.getString(3);
                String[] neededCategories = reqCats.split(",");
                boolean canCook = true;
                for (String needed : neededCategories) {
                    if (!availableCategories.contains(needed.trim().toLowerCase())) {
                        canCook = false;
                        break;
                    }
                }
                if (canCook) {
                    suggestedList.add(new RecipeItem(id, name, reqCats, instruction));
                }
            } while (recipeCursor.moveToNext());
        }
        recipeCursor.close();
        db.close();
        return suggestedList;
    }

    // Initialize default recipes
    public void initializeDefaultRecipes() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_RECIPE, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if (count == 0) {
            // 1. Maggi Goreng recipe
            ContentValues v1 = new ContentValues();
            v1.put(COL_RECIPE_NAME, "Maggi Goreng");
            v1.put(COL_REQUIRED_CATEGORIES, "Noodle,Egg,Vegetable");
            String maggiSteps = "1. Boil the Maggi noodles until soft, then drain the water.\n" +
                    "2. Heat some oil in a pan and stir-fry the garlic and onion.\n" +
                    "3. Add the seasoning packet and stir-fry the egg and vegetables.\n" +
                    "4. Add the boiled noodles and stir everything together. Add soy sauce if you like.";
            v1.put(COL_INSTRUCTION, maggiSteps);
            db.insert(TABLE_RECIPE, null, v1);

            // 2. Omelette recipe
            ContentValues v2 = new ContentValues();
            v2.put(COL_RECIPE_NAME, "Classic Omelette");
            v2.put(COL_REQUIRED_CATEGORIES, "Egg");
            String omeletteSteps = "1. Crack the eggs into a bowl and whisk thoroughly.\n" +
                    "2. Heat a little oil or butter in a frying pan over medium heat.\n" +
                    "3. Pour in the eggs and let them sit for a minute until the bottom sets.\n" +
                    "4. Fold the omelette in half and serve warm.";
            v2.put(COL_INSTRUCTION, omeletteSteps);
            db.insert(TABLE_RECIPE, null, v2);

            // 3. Chicken Stir-fry recipe
            ContentValues v3 = new ContentValues();
            v3.put(COL_RECIPE_NAME, "Chicken Stir-fry");
            v3.put(COL_REQUIRED_CATEGORIES, "Meat,Vegetable");
            String chickenSteps = "1. Cut the chicken into bite-sized pieces.\n" +
                    "2. Heat oil in a pan and stir-fry the chicken until brown.\n" +
                    "3. Add your favorite vegetables and a bit of soy sauce.\n" +
                    "4. Stir-fry for another 3 minutes until cooked. Serve warm!";
            v3.put(COL_INSTRUCTION, chickenSteps);
            db.insert(TABLE_RECIPE, null, v3);

            // 4. Vegetable Salad recipe
            ContentValues v4 = new ContentValues();
            v4.put(COL_RECIPE_NAME, "Vegetable Salad");
            v4.put(COL_REQUIRED_CATEGORIES, "Vegetable");
            String saladSteps = "1. Wash and chop the vegetables into small cubes.\n" +
                    "2. Place the chopped vegetables into a large bowl.\n" +
                    "3. Add your favorite dressing and mix gently.\n" +
                    "4. Chill in the fridge for 10 minutes before serving.";
            v4.put(COL_INSTRUCTION, saladSteps);
            db.insert(TABLE_RECIPE, null, v4);

        }
        db.close();
    }

    // Get all history
    public List<HistoryItem> getAllHistory() {
        List<HistoryItem> historyList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_HISTORY + " ORDER BY " + COL_LOG_ID + " DESC";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String date = cursor.getString(2);
                String status = cursor.getString(3);
                double price = cursor.getDouble(4);
                historyList.add(new HistoryItem(id, name, date, status, price));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return historyList;
    }

    // Delete item from inventory
    public boolean permanentlyDeleteItem(InventoryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedRows = db.delete(TABLE_INVENTORY, COL_INV_ID + "=?", new String[]{String.valueOf(item.getInvId())});
        db.close();
        return deletedRows > 0;
    }
}