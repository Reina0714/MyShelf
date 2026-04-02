package com.example.myshelf;

public class RecipeItem {

    private int recipeId;
    private String recipeName;
    private String requiredCategories; // e.g., "Noodle,Egg,Vegetable"
    private String instruction;

    public RecipeItem() {
    }

    // Constructor without ID
    public RecipeItem(int recipeId, String recipeName, String requiredCategories, String instruction) {
        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.requiredCategories = requiredCategories;
        this.instruction = instruction;
    }

    // Getters and Setters
    public int getRecipeId() { return recipeId; }
    public void setRecipeId(int recipeId) { this.recipeId = recipeId; }

    public String getRecipeName() { return recipeName; }
    public void setRecipeName(String recipeName) { this.recipeName = recipeName; }

    public String getRequiredCategories() { return requiredCategories; }
    public void setRequiredCategories(String requiredCategories) { this.requiredCategories = requiredCategories; }

    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }
}