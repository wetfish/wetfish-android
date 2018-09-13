package net.wetfish.wetfish.ui.viewpager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by ${Michael} on 9/12/2018.
 */
public class CustomLockingViewPager extends ViewPager {

    /* Data */
    // Boolean to track state of Pager tab switching
    private boolean mIsSwitchingEnabled;

    public CustomLockingViewPager(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mIsSwitchingEnabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mIsSwitchingEnabled) {
            return super.onTouchEvent(ev);
        }

        // If @mIsSwitchingEnabled is false then return false. This will prevent touch events for swiping
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mIsSwitchingEnabled) {
            return super.onInterceptTouchEvent(ev);
        }

        // If @mIsSwitchingEnabled is false then return false. This will prevent touch events for swiping
        return false;
    }

    public void setViewpagerSwitching(boolean swipeable) {
        mIsSwitchingEnabled = swipeable;
    }

}
