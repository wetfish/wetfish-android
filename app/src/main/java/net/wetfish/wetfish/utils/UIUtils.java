package net.wetfish.wetfish.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.snackbar.Snackbar;

import net.wetfish.wetfish.R;

import androidx.core.content.ContextCompat;

/**
 * Created by ${Michael} on 11/15/2017.
 */

public class UIUtils {

    public static void generateSnackbar(Context context, View view, String text, Integer length) {
        Snackbar sb = Snackbar.make(view, text, length);
        sb.getView().setBackgroundColor(ContextCompat.getColor(context, R.color.card_dialog_dark));
        sb.show();
    }

    public static void hideKeyboard(View view, Context context) {
        InputMethodManager inputMethodManager =(InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
