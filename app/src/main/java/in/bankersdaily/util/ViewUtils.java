package in.bankersdaily.util;

/*
 * Copyright 2012 Kevin Sawicki <kevinsawicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.graphics.Typeface;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import in.bankersdaily.R;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Utilities for working with the {@link View} class
 */
public class ViewUtils {

    /**
     * Set visibility of given view to be gone or visible
     * <p/>
     * This method has no effect if the view visibility is currently invisible
     *
     * @param view
     * @param gone
     * @return view
     */
    public static <V extends View> V setGone(final V view, final boolean gone) {
        if (view != null)
            if (gone) {
                if (GONE != view.getVisibility())
                    view.setVisibility(GONE);
            } else {
                if (VISIBLE != view.getVisibility())
                    view.setVisibility(VISIBLE);
            }
        return view;
    }

    /**
     * Set visibility of given view to be invisible or visible
     * <p/>
     * This method has no effect if the view visibility is currently gone
     *
     * @param view
     * @param invisible
     * @return view
     */
    public static <V extends View> V setInvisible(final V view,
                                                  final boolean invisible) {
        if (view != null)
            if (invisible) {
                if (INVISIBLE != view.getVisibility())
                    view.setVisibility(INVISIBLE);
            } else {
                if (VISIBLE != view.getVisibility())
                    view.setVisibility(VISIBLE);
            }
        return view;
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static void setTypeface(TextView[] textViews, Typeface typeface) {
        for (TextView view : textViews) {
            view.setTypeface(typeface);
        }
    }

    public static AlertDialog.Builder getAlertDialog(final Activity activity, @StringRes int title,
                                                     @StringRes int messageRes) {

        return new AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle)
                .setTitle(title)
                .setMessage(messageRes)
                .setNegativeButton(R.string.ok, null);
    }

    private ViewUtils() {
    }
}
