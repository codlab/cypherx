package eu.codlab.cypherx.ui.main;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import eu.codlab.crypto.core.keys.KeyUtil;
import eu.codlab.crypto.core.utils.Base64Coder;
import eu.codlab.cypherx.ApplicationController;
import eu.codlab.cypherx.MainActivity;
import eu.codlab.cypherx.database.DevicesController;
import eu.codlab.cypherx.utils.Keys;
import eu.codlab.cypherx.utils.UrlsHelper;

/**
 * Created by kevinleperf on 08/07/15.
 */
public class MainActivityController {
    private static KeyPair _keys;
    public final static int SAVED = 0;
    public final static int NOT_SAVED = 1;
    public final static int ERROR = 2;
    public static String LOAD_WEB_MESSAGES = "eu.codlab.cyphersend.LOAD_WEB_MESSAGES";
    private MainActivity _main_activity;

    public MainActivityController() {
        _main_activity = null;
    }

    public MainActivityController(MainActivity mainActivity) {
        _main_activity = mainActivity;

        try {
            _keys = Keys.loadKeyPair(mainActivity);
            Log.d("Keys", "having key :: _keys " + _keys);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public static String getDevicePublicKey(Activity activity) {
        return new String(Base64Coder.encode(KeyUtil.exportPublicKey(_keys.getPublic())));
    }

    public Uri createUri(Activity activity) {
        return Uri.parse(createUriString(activity));
    }

    public String createUriString(Activity activity) {
        return UrlsHelper.getPublicInfoURL(activity, getDevicePublicKey(activity));
    }

    public int onNewUri(Context context, Uri uri) {
        if (uri != null) {
            String[] splitted = uri.getPath().split("/");
            Log.d("Keys", "having splitted " + Arrays.toString(splitted));
            if (splitted.length >= 5) {
                int idx_publick_key = splitted.length - 1;
                int idx_guid = idx_publick_key - 1;

                if (idx_publick_key >= 4) {
                    String device_identifier = splitted[idx_guid];
                    String public_key = splitted[idx_publick_key];

                    Log.d("Keys", "having splitted "
                            + DevicesController.getInstance(context).hasDevice(device_identifier));
                    if (DevicesController.getInstance(context).hasDevice(device_identifier)) {
                        //TODO EVENTBUS WITH CONFIRMATION ?
                        //EventBus.getDefault().post(new OnExistingDevice(device_identifier, public_key));
                        return NOT_SAVED;
                    } else {
                        DevicesController.getInstance(context).addDevice(device_identifier, public_key);
                        return SAVED;
                    }
                }
                return ERROR;
            }
        }
        return ERROR;
    }

    public Context getListenerContext() {
        return ApplicationController.getInstance();
    }
}
