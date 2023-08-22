package com.jgm.mybudgetapp;

import com.jgm.mybudgetapp.objects.CategoryItemResponse;
import com.jgm.mybudgetapp.objects.CategoryPercent;
import com.jgm.mybudgetapp.objects.CategoryResponse;
import com.jgm.mybudgetapp.objects.MyDate;

import java.util.List;

public interface CategoryInterface {

    void showCategoryDetails(CategoryPercent category);
    CategoryPercent getCategoryData();
    List<CategoryItemResponse> getCategoryItems();
    MyDate getSelectedDate();
}
