package com.example.myshelf;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private Context context;
    private List<RecipeItem> recipeList;
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onCookClick(RecipeItem recipe);
    }

    public RecipeAdapter(Context context, List<RecipeItem> recipeList, OnRecipeClickListener listener) {
        this.context = context;
        this.recipeList = recipeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeItem currentRecipe = recipeList.get(position);

        // show recipe details
        holder.tvRecipeName.setText(currentRecipe.getRecipeName());
        holder.tvRecipeTags.setText(currentRecipe.getRequiredCategories());

        // show recipe image
        String name = currentRecipe.getRecipeName();
        if (name.equalsIgnoreCase("Maggi Goreng")) {
            holder.ivRecipeImage.setImageResource(R.drawable.img_maggi);
        } else if (name.equalsIgnoreCase("Classic Omelette")) {
            holder.ivRecipeImage.setImageResource(R.drawable.img_omelette);
        } else if (name.equalsIgnoreCase("Chicken Stir-fry")) {
            holder.ivRecipeImage.setImageResource(R.drawable.img_chickensf);
        } else if (name.equalsIgnoreCase("Vegetable Salad")) {
            holder.ivRecipeImage.setImageResource(R.drawable.img_salad);
        } else {
            // default recipe bg
            holder.ivRecipeImage.setImageResource(R.drawable.bg_recipe_default);
        }

        // show cook button
        holder.btnViewCook.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Cook " + currentRecipe.getRecipeName() + "?");

            String steps = currentRecipe.getInstruction();
            String requiredItems = currentRecipe.getRequiredCategories();

            // Format ingredients list with bullet points
            String formattedIngredients = "- " + requiredItems.replace(",", "\n- ");

            String dialogMessage = "Cooking Steps:\n\n"
                    + steps
                    + "\n\n----------------------------------\n"
                    + "Confirm cooking? The following ingredients will be deducted from your inventory:\n\n"
                    + formattedIngredients;

            builder.setMessage(dialogMessage);

            builder.setPositiveButton("COOK", (dialog, which) -> {
                // callback RecipeFragment to handle database deduction
                if (listener != null) {
                    listener.onCookClick(currentRecipe);
                }
            });

            builder.setNegativeButton("CANCEL", null);
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecipeName, tvRecipeTags;
        Button btnViewCook;
        ImageView ivRecipeImage;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecipeName = itemView.findViewById(R.id.tv_recipe_name);
            tvRecipeTags = itemView.findViewById(R.id.tv_recipe_tags);
            btnViewCook = itemView.findViewById(R.id.btn_view_cook);
            ivRecipeImage = itemView.findViewById(R.id.iv_recipe_image);
        }
    }
}