package eu.codlab.cypherx.ui.main;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by kevinleperf on 05/07/15.
 */
public class ShareView extends View {
    private void init(){
    }
    public ShareView(Context context) {
        super(context);
        init();
    }

    public ShareView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShareView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ShareView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
}
