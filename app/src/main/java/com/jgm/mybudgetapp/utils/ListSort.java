package com.jgm.mybudgetapp.utils;

import com.jgm.mybudgetapp.objects.CategoryResponse;

import java.util.Comparator;

public class ListSort {

    public static Comparator<CategoryResponse> categoryResponseComparator = new Comparator<CategoryResponse>() {
        @Override
        public int compare(CategoryResponse c1, CategoryResponse c2) {

            float total1 = Math.abs(c1.getTotal());
            float total2 = Math.abs(c2.getTotal());

            return Float.compare(total2, total1);
        }
    };

}
