package com.jgm.mybudgetapp.utils;

import com.jgm.mybudgetapp.objects.CategoryResponse;

import java.util.Comparator;

public class ListSort {

    public static Comparator<CategoryResponse> categoryResponseComparator = new Comparator<CategoryResponse>() {
        @Override
        public int compare(CategoryResponse c1, CategoryResponse c2) {
            String total1 = String.valueOf(c1.getTotal());
            String total2 = String.valueOf(c2.getTotal());

            // descending order
            return total2.compareTo(total1);
        }
    };

}
