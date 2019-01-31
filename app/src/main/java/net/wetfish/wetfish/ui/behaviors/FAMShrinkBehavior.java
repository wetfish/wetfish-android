package net.wetfish.wetfish.ui.behaviors;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.github.clans.fab.FloatingActionMenu;

import net.wetfish.wetfish.R;

import java.util.List;

/**
 * Created by ${Michael} on 1/18/2019.
 */

public class FAMShrinkBehavior extends CoordinatorLayout.Behavior<FloatingActionMenu> {

    private Context mContext;
    private Animation mScaleDown;
    private Animation mScaleUp;

    public FAMShrinkBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        //TODO: Change the animation to fit the middle of the view, and also make a new animation for it
        mScaleDown = AnimationUtils.loadAnimation(mContext, R.anim.rl_scale_down);
        mScaleUp = AnimationUtils.loadAnimation(mContext, R.anim.rl_scale_up);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionMenu child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionMenu child, View dependency) {
        float translationY = getFabTranslationYForSnackbar(parent, child);
        float percentComplete = -translationY / dependency.getHeight();
        float scaleFactor = 1 - percentComplete;

        child.setScaleX(scaleFactor);
        child.setScaleY(scaleFactor);
        return false;
    }

    private float getFabTranslationYForSnackbar(CoordinatorLayout parent,
                                                FloatingActionMenu fab) {
        float minOffset = 0;
        final List<View> dependencies = parent.getDependencies(fab);
        for (int i = 0, z = dependencies.size(); i < z; i++) {
            final View view = dependencies.get(i);
            if (view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(fab, view)) {
                minOffset = Math.min(minOffset,
                        ViewCompat.getTranslationY(view) - view.getHeight());
            }
        }

        return minOffset;
    }


    @Override
    public boolean onStartNestedScroll(final CoordinatorLayout coordinatorLayout, final FloatingActionMenu child,
                                       final View directTargetChild, final View target, final int nestedScrollAxes) {
        // Ensure reaction to vertical scrolling
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(coordinatorLayout,
                child, directTargetChild, target, nestedScrollAxes);
    }

    // TODO: Well this is strange behavior. Potentially look more into this depending on the desired Material Design
    @Override
    public void onNestedScroll(final CoordinatorLayout coordinatorLayout, final FloatingActionMenu child,
                               final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (dyConsumed > 0 && child.getVisibility() != View.INVISIBLE) {
            // User scrolled down
            child.startAnimation(mScaleDown);
            child.setClickable(false);
            child.setVisibility(View.INVISIBLE);
        } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
            // User scrolled up
            child.startAnimation(mScaleUp);
            child.setClickable(true);
            child.setVisibility(View.VISIBLE);
        }
    }
}
