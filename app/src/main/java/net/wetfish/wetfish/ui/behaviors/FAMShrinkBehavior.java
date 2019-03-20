package net.wetfish.wetfish.ui.behaviors;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.google.android.material.snackbar.Snackbar;

import net.wetfish.wetfish.R;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

/**
 * Created by ${Michael} on 1/18/2019.
 */

public class FAMShrinkBehavior extends CoordinatorLayout.Behavior<RelativeLayout> {

    private Context mContext;
    private Animation mScaleDown;
    private Animation mScaleUp;

    public FAMShrinkBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        mScaleDown = AnimationUtils.loadAnimation(mContext, R.anim.fam_scale_down);
        mScaleUp = AnimationUtils.loadAnimation(mContext, R.anim.fam_scale_up);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, RelativeLayout child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, RelativeLayout child, View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        // Note that the RelativeLayout gets translated.
        child.setTranslationY(translationY);
        return false;
    }

    @Override
    public boolean onStartNestedScroll(final CoordinatorLayout coordinatorLayout, final RelativeLayout child,
                                       final View directTargetChild, final View target, final int nestedScrollAxes) {
        // Ensure reaction to vertical scrolling
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(coordinatorLayout,
                child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(final CoordinatorLayout coordinatorLayout, final RelativeLayout child,
                               final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (dyConsumed > 0 && child.getVisibility() != View.INVISIBLE) {
            // User scrolled down
            child.startAnimation(mScaleDown);
//            child.findViewById(R.id.fam_gallery).setClickable(false);
            child.setVisibility(View.INVISIBLE);
        } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
            // User scrolled up
            child.startAnimation(mScaleUp);
//            child.findViewById(R.id.fam_gallery).setClickable(true);
            child.setVisibility(View.VISIBLE);
        }
    }


}
