package eu.codlab.cypherx.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by kevinleperf on 08/07/15.
 */
public class Switch extends android.support.v7.widget.SwitchCompat {
    private void init() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/main.ttf");
        setTypeface(font);
    }

    public Switch(Context context) {
        super(context);
        init();
    }

    public Switch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Switch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e){
        return false;
    }
}
