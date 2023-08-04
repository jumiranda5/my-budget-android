package com.jgm.mybudgetapp.utils;

import android.util.Log;

import com.jgm.mybudgetapp.objects.CategoryResponse;

import java.util.Comparator;

public class ListSort {

    public static Comparator<CategoryResponse> categoryResponseComparator = new Comparator<CategoryResponse>() {
        @Override
        public int compare(CategoryResponse c1, CategoryResponse c2) {

            float total1 = c1.getTotal();
            float total2 = c2.getTotal();

            // descending order
            return Float.compare(total1, total2);
        }
    };

}
