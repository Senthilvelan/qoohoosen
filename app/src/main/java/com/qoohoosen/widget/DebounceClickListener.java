package com.qoohoosen.widget;

import static com.qoohoosen.utils.Constable.DEBOUNCE_INTERVAL_DEFAULT;

import android.os.SystemClock;
import android.view.View;

public class DebounceClickListener implements View.OnClickListener {

    private long debounceInterval;
    private long lastClickTime;
    private View.OnClickListener clickListener;

    public DebounceClickListener(final View.OnClickListener clickListener) {
        this(clickListener, DEBOUNCE_INTERVAL_DEFAULT);
    }

    public DebounceClickListener(final View.OnClickListener clickListener, final long debounceInterval) {
        this.clickListener = clickListener;
        this.debounceInterval = debounceInterval;
    }

    @Override
    public void onClick(final View v) {
        if ((SystemClock.elapsedRealtime() - lastClickTime) < debounceInterval) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
        clickListener.onClick(v);
    }
}