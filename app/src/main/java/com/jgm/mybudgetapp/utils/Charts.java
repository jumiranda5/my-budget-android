package com.jgm.mybudgetapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.objects.Category;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Charts {

    public Charts() {}

    public static void setCategoriesChart(
            Context context,
            ArrayList<Category> categories,
            ImageView imageView,
            int size,
            int indicator) {

        ExecutorService canvasService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        canvasService.execute(() -> {

            // Get screen density to set the canvas size
            int density = (int) context.getResources().getDisplayMetrics().density;
            int chartSize = size * density;
            int indicatorSize = indicator * density;
            int indicatorBoundaries = chartSize - indicatorSize;

            // Create Pie with canvas
            Bitmap bitmap = Bitmap.createBitmap(chartSize, chartSize, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            RectF box = new RectF(0,0, chartSize, chartSize);

            // Start from the bottom
            float start = 90;

            // Loop through list to set the pie slices
            for(int i =0; i < categories.size(); i++) {

                Category category = categories.get(i);
                paint.setColor(context.getColor(category.getColorId())); // todo: create color utils

                // Set category angle (1% = 3,6°)
                float percent = category.getPercent();
                float angle = NumberUtils.roundFloat(percent * 3.6f);

                // Draw pie slice and increase next start angle
                canvas.drawArc(box, start, angle, true, paint);
                start += angle;
            }

            // Set inner circle
            paint.setColor(context.getColor(R.color.bg_01dp));
            RectF inner = new RectF(indicatorSize, indicatorSize, indicatorBoundaries, indicatorBoundaries);
            canvas.drawArc(inner,0,360,true, paint);

            handler.post(() -> {
                imageView.setImageBitmap(bitmap);
                canvasService.shutdown();
            });
        });
    }

}
