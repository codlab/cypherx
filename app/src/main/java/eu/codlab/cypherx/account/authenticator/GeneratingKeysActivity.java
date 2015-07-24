package eu.codlab.cypherx.account.authenticator;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import java.security.KeyPair;

import eu.codlab.crypto.core.keys.KeyUtil;
import eu.codlab.cypherx.R;
import eu.codlab.cypherx.utils.FragmentStackManager;
import eu.codlab.cypherx.utils.Keys;
import eu.codlab.cypherx.utils.SystemFittableActivity;

/**
 * Created by kevinleperf on 03/07/15.
 */
public class GeneratingKeysActivity extends SystemFittableActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        super.onCreate(savedInstanceState);

        if (Keys.areKeysPresent(this)) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        setContentView(R.layout.activity_generate);

        final Handler handler = new Handler();

        Thread t = new Thread() {
            public void run() {
                KeyPair keys = KeyUtil.generateKey(2048);
                try {
                    Keys.saveKeyPair(GeneratingKeysActivity.this, keys);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setResult(RESULT_OK);
                        GeneratingKeysActivity.this.finish();
                    }
                });
            }
        };
        t.start();
    }

    @Override
    protected FragmentStackManager getFragmentStackManager() {
        return new FragmentStackManager(this, 0) {
            @Override
            public void pop() {

            }

            @Override
            public void push(int new_index, Bundle arguments) {

            }

            @Override
            public boolean isMainView() {
                return false;
            }

            @Override
            public boolean navigationBackEnabled() {
                return false;
            }

            @Override
            public boolean isNavigationDrawerEnabled() {
                return false;
            }
        };
    }
}
