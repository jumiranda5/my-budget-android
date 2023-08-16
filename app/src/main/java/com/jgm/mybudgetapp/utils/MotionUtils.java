package com.jgm.mybudgetapp.utils;

import static com.jgm.mybudgetapp.utils.Tags.accountDetailsTag;
import static com.jgm.mybudgetapp.utils.Tags.accountFormTag;
import static com.jgm.mybudgetapp.utils.Tags.accountsTag;
import static com.jgm.mybudgetapp.utils.Tags.categoriesFormTag;
import static com.jgm.mybudgetapp.utils.Tags.categoriesListTag;
import static com.jgm.mybudgetapp.utils.Tags.homeTag;
import static com.jgm.mybudgetapp.utils.Tags.pendingTag;
import static com.jgm.mybudgetapp.utils.Tags.transactionFormTag;
import static com.jgm.mybudgetapp.utils.Tags.transactionsInTag;
import static com.jgm.mybudgetapp.utils.Tags.transactionsOutTag;

import android.util.Log;

import androidx.constraintlayout.motion.widget.MotionLayout;

import com.jgm.mybudgetapp.R;

public class MotionUtils {

    public MotionUtils() {}

    public static void setTransition(MotionLayout mMotion, String from, String to) {

        Log.w("debug-animation", "transition from: " + from + " => to: " + to);

        switch (from) {
            case homeTag:
                if (to.equals(pendingTag)
                        || to.equals(transactionFormTag)){
                    mMotion.setTransition(R.id.transition_content_and_bars);
                    Log.d("debug-animation", "CONTENT AND BARS");
                }

                else {
                    mMotion.setTransition(R.id.transition_content);
                    Log.d("debug-animation", "CONTENT");
                }
                break;
            case accountsTag:
                if (to.equals(accountDetailsTag)) {
                    mMotion.setTransition(R.id.transition_content_and_bottom_bar);
                    Log.d("debug-animation", "CONTENT AND BOTTOM BAR");
                }
                else if (to.equals(accountFormTag)) {
                    mMotion.setTransition(R.id.transition_content_and_bars);
                    Log.d("debug-animation", "CONTENT AND BARS");
                }
                else {
                    mMotion.setTransition(R.id.transition_content);
                    Log.d("debug-animation", "CONTENT");
                }
                break;
            case accountDetailsTag:
                if (to.equals(accountsTag)) {
                    mMotion.setTransition(R.id.transition_content_no_bottom_bar);
                    Log.d("debug-animation", "CONTENT NO BOTTOM BAR");
                }
                else if (to.equals(accountFormTag)) {
                    mMotion.setTransition(R.id.transition_content_and_toolbar_no_bottom_bar);
                    Log.d("debug-animation", "CONTENT AND TOOLBAR NO BOTTOM BAR");
                }
                break;
            case transactionsOutTag:
            case transactionsInTag:
                if (to.equals(transactionFormTag)) {
                    mMotion.setTransition(R.id.transition_content_and_bars);
                    Log.d("debug-animation", "CONTENT AND BARS");
                }
                else {
                    mMotion.setTransition(R.id.transition_content);
                    Log.d("debug-animation", "CONTENT");
                }
                break;
            case accountFormTag:
            case transactionFormTag:
            case categoriesListTag:
            case categoriesFormTag:
            case pendingTag:
                mMotion.setTransition(R.id.transition_content_slide_bottom);
                Log.d("debug-animation", "SLIDE");
                break;
        }
    }
}
