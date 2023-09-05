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
import com.jgm.mybudgetapp.objects.CategoryPercent;
import com.jgm.mybudgetapp.objects.MonthTotal;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Charts {

    public static void setCategoriesChart(
            Context context,
            ArrayList<CategoryPercent> categories,
            ImageView imageView,
            int size,
            int indicator,
            boolean isCardView) {

        ExecutorService canvasService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        canvasService.execute(() -> {

            // Get screen density to set the canvas size
            float density = context.getResources().getDisplayMetrics().density;
            int chartSize = (int) (size * density);
            int indicatorSize = (int) (indicator * density);
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

            // Draw empty chart if categories.size() = 0
            if (categories.size() == 0) {
                paint.setColor(context.getColor(R.color.bg_progress_track));
                canvas.drawArc(box, start, 360.0f, true, paint);
            }

            // Loop through list to set the pie slices
            for(int i = 0; i < categories.size(); i++) {

                CategoryPercent category = categories.get(i);
                int color = ColorUtils.getColor(category.getColorId()).getColor();
                paint.setColor(context.getColor(color));

                // Set category angle (1% = 3,6°)
                float percent = category.getPercent();
                float angle = NumberUtils.roundFloat(percent * 3.6f);

                // Draw pie slice and increase next start angle
                canvas.drawArc(box, start, angle, true, paint);
                start += angle;
            }

            // Set inner circle
            if (isCardView) paint.setColor(context.getColor(R.color.bg_01dp));
            else paint.setColor(context.getColor(R.color.bg_00dp));
            RectF inner = new RectF(indicatorSize, indicatorSize, indicatorBoundaries, indicatorBoundaries);
            canvas.drawArc(inner,0,360,true, paint);

            handler.post(() -> {
                imageView.setImageBitmap(bitmap);
                canvasService.shutdown();
            });
        });
    }

    public static void setYearTotalChart(Context context, ImageView imageView,
                                         ArrayList<MonthTotal> monthList, float highestBar,
                                         int width, int height) {

        ExecutorService yearCanvasService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        yearCanvasService.execute(() -> {

            // Get screen density to set the canvas size
            int density = (int) context.getResources().getDisplayMetrics().density;
            int chartWidth = width * density;
            int chartHeight = height * density;
            int textSize = 12 * density;
            int textMarginBottom = 4 * density;
            int barMarginBottom = 20 * density;
            int barPadding = 2 * density;

            int sw = context.getResources().getConfiguration().smallestScreenWidthDp;
            if (sw >= 600) textSize = 10 * density;

            // Create year labels with canvas
            Bitmap bitmap = Bitmap.createBitmap(chartWidth, chartHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            // Init paint
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setTextSize(textSize);

            /*
                highest bar = 100% = chartHeight
                1% value = highest bar / 100

                ex:
                highest bar = 1000
                1% = 1000/100 = 10 (barPercentValue)
                another bar = 500
                500 / 10 = 50 (incomeBarPercent)
                height = 50% * chartHeight
            */

            // Month space vars
            int start = density; // (+ 1dp);
            int monthWidth = (chartWidth / 16);
            int monthGap =  (chartWidth - (monthWidth * 12)) / 11;
            int nextStart = monthWidth + monthGap;

            // Month bars vars
            float barPercentValue = NumberUtils.roundFloat(highestBar / 100);

            for(int i = 0; i < monthList.size(); i++) {
                // draw text
                String monthName = monthList.get(i).getMonthNameSmall();
                paint.setColor(context.getColor(R.color.high_emphasis_text));
                canvas.drawText(monthName, start, chartHeight - textMarginBottom, paint);

                int barWidth = (monthWidth / 2);

                // draw income bar
                float income = monthList.get(i).getIncome();
                float[] bar1 = getBarMeasures(income, barPercentValue, chartHeight,
                        start, barWidth, barPadding, barMarginBottom);

                paint.setColor(context.getColor(R.color.income_chart));
                canvas.drawRect(bar1[0], bar1[1], bar1[2], bar1[3], paint);

                // draw expenses bar
                float expenses = monthList.get(i).getExpenses();
                int expensesBarStart = start + barWidth;
                float[] bar2 = getBarMeasures(expenses, barPercentValue,chartHeight,
                        expensesBarStart, barWidth, barPadding, barMarginBottom);

                paint.setColor(context.getColor(R.color.expense_chart));
                canvas.drawRect(bar2[0], bar2[1], bar2[2], bar2[3], paint);

                // Increase start
                start += nextStart;
            }

            handler.post(() -> {
                imageView.setImageBitmap(bitmap);
                yearCanvasService.shutdown();
            });
        });

    }

    public static float[] getBarMeasures(float value, float barPercentValue, int chartHeight,
                                   int start, int barWidth, int barPadding, int barMarginBottom) {

        float barPercent = NumberUtils.roundFloat(value / barPercentValue);
        float barHeight = NumberUtils.roundFloat((barPercent/100) * (chartHeight - barMarginBottom));
        if (barHeight == 0.0f) barHeight = 2f;
        float barStart = start + barPadding;
        float barTop = (chartHeight - barHeight) - barMarginBottom;
        int barEnd = start + barWidth;
        int barBottom = chartHeight - barMarginBottom;

        return new float[] {barStart, barTop, barEnd, barBottom};
    }

}
