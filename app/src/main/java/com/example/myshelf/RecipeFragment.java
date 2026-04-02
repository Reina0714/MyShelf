package com.example.myshelf;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecipeFragment extends Fragment {

    private RecyclerView rvRecipes;
    private TextView tvRecipeAlert;
    private DatabaseHelper dbHelper;
    private RecipeAdapter adapter;
    private List<RecipeItem> suggestedList = new ArrayList<>();

    public RecipeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe, container, false);

        rvRecipes = view.findViewById(R.id.rv_recipes);
        tvRecipeAlert = view.findViewById(R.id.tv_recipe_alert);

        rvRecipes.setLayoutManager(new LinearLayoutManager(requireContext()));
        dbHelper = new DatabaseHelper(requireContext());

        // initialize default recipes if not exist
        dbHelper.initializeDefaultRecipes();

        // Initialize adapter once
        adapter = new RecipeAdapter(requireContext(), suggestedList, recipe -> {
            // Directly perform deduction without a second popup
            performIngredientDeduction(recipe);
        });
        rvRecipes.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSuggestedRecipes();
    }

    private void loadSuggestedRecipes() {
        List<RecipeItem> newList = dbHelper.getSuggestedRecipes();
        suggestedList.clear();
        suggestedList.addAll(newList);

        if (suggestedList.isEmpty()) {
            tvRecipeAlert.setText("No matches found. Add more ingredients to your shelf!");
            tvRecipeAlert.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_light_grey));
        } else {
            tvRecipeAlert.setText("We found " + suggestedList.size() + " recipes based on your active ingredients.");
            tvRecipeAlert.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_green));
        }

        adapter.notifyDataSetChanged();
    }

    // Auto deduct required ingredients
    private void performIngredientDeduction(RecipeItem recipe) {
        String[] requiredCats = recipe.getRequiredCategories().split(",");
        List<InventoryItem> activeItems = dbHelper.getActiveInventory();

        for (String neededCat : requiredCats) {
            String targetCat = neededCat.trim();

            for (int i = 0; i < activeItems.size(); i++) {
                InventoryItem item = activeItems.get(i);

                if (item.getCategory().equalsIgnoreCase(targetCat)) {
                    // decrease quantity or mark as consumed
                    dbHelper.deductItemQuantity(item);
                    activeItems.remove(i);
                    break;
                }
            }
        }

        Toast.makeText(requireContext(), "Cooked successfully. Inventory updated.", Toast.LENGTH_SHORT).show();
        loadSuggestedRecipes();
    }
}