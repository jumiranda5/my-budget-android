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

import androidx.constraintlayout.motion.widget.MotionLayout;

import com.jgm.mybudgetapp.R;

public class MotionUtils {

    public MotionUtils() {}

    public static void setTransition(MotionLayout mMotion, String from, String to) {

        switch (from) {
            case homeTag:
                // transition only content
                // unless next fragment is pending or form => transition bottom nav, toolbar and content
                if (to.equals(pendingTag)
                        || to.equals(transactionFormTag))
                    mMotion.setTransition(R.id.transition_content_and_bars);
                else mMotion.setTransition(R.id.transition_content);
                break;
            case accountsTag:
                // if nextFragment is home, expense or income => transition only content
                // if nextFragment is accounts details => transition content and bottom bar
                // if nextFragment is account form => transition content, bottom bar and toolbar
                if (to.equals(accountDetailsTag)) mMotion.setTransition(R.id.transition_content_and_bottom_bar);
                else if (to.equals(accountFormTag)) mMotion.setTransition(R.id.transition_content_and_bars);
                else mMotion.setTransition(R.id.transition_content);
                break;
            case accountDetailsTag:
                // if nextFragment is accounts => transition only content (back press)
                // if nextFragment is accounts form => transition content and toolbar (bottom bar is already gone)
                if (to.equals(accountsTag)) mMotion.setTransition(R.id.transition_content_no_bottom_bar);
                else if (to.equals(accountFormTag)) mMotion.setTransition(R.id.transition_content_and_toolbar_no_bottom_bar);
                break;
            case transactionsOutTag:
            case transactionsInTag:
                // if nextFragment is home, account or income => transition only content
                // if nextFragment is form => transition content, toolbar and bottom bar
                if (to.equals(transactionFormTag)) mMotion.setTransition(R.id.transition_content_and_bars);
                else mMotion.setTransition(R.id.transition_content);
                break;
            case accountFormTag:
            case transactionFormTag:
            case categoriesListTag:
            case categoriesFormTag:
            case pendingTag:
                mMotion.setTransition(R.id.transition_content_slide_bottom);
                break;
        }

    }
}
