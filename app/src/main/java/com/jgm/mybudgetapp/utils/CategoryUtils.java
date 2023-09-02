package com.jgm.mybudgetapp.utils;

import com.jgm.mybudgetapp.objects.CategoryPercent;
import com.jgm.mybudgetapp.objects.CategoryResponse;

import java.util.ArrayList;
import java.util.List;

public class CategoryUtils {

    public static ArrayList<CategoryPercent> getCategoriesPercents(List<CategoryResponse> categories) {
        float total = 0.0f;
        ArrayList<CategoryPercent> percents = new ArrayList<>();

        // Get total from list
        for(int i =0; i < categories.size(); i++){
            total = total + categories.get(i).getTotal();
        }

        // Set percentage for each category
        for(int i =0; i < categories.size(); i++){
            float percent =
                    NumberUtils.roundFloat((categories.get(i).getTotal() * 100) / total);
            CategoryResponse category = categories.get(i);
            CategoryPercent categoryPercent = new CategoryPercent(
                    category.getId(),
                    category.getCategory(),
                    category.getColorId(),
                    category.getIconId(),
                    category.getTotal());
            categoryPercent.setPercent(percent);
            percents.add(categoryPercent);
        }

        return percents;
    }
}
