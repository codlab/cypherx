package eu.codlab.cypherx;

import android.app.Application;
import android.content.SharedPreferences;

import com.crashlytics.android.Crashlytics;

import net.danlew.android.joda.JodaTimeAndroid;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.UUID;

import eu.codlab.crypto.core.utils.Base64Coder;
import eu.codlab.crypto.core.utils.PRNGFixes;
import eu.codlab.cypherx.database.DatabaseManager;
import eu.codlab.cypherx.proxy.ProxyController;
import eu.codlab.cypherx.webservice.models.Authent;
import eu.codlab.sharedmutex.Mutex;
import io.fabric.sdk.android.Fabric;

/**
 * Created by kevinleperf on 03/07/15.
 */
public class ApplicationController extends Application {
    private final static String GUID = "GUID";
    private final static String PASS = "PASS";
    private final static String HAS_SEEN = "HAS_SEEN";
    private static ApplicationController _instance;

    private SecureRandom _random = new SecureRandom();
    private SharedPreferences _shared_preferences;
    private String _guid = null;
    private String _pass = null;
    private ProxyController _proxy_controller;

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
        PRNGFixes.apply();
        _instance = this;
        Fabric.with(this, new Crashlytics());
        DatabaseManager.getInstance().startSession(this);

        Mutex mutex = new Mutex("cypherX", true);
        _proxy_controller = new ProxyController(this);
    }

    public static ApplicationController getInstance() {
        return _instance;
    }

    public String getGuid() {
        if (_guid == null) {
            _guid = getString(GUID, null);
            if(_guid == null) {
                _guid = UUID.randomUUID().toString();
                setString(GUID, _guid);
            }
        }
        return _guid;
    }

    private String getPass() {
        if (_pass == null) {
            _pass = getString(PASS, null);
            if (_pass == null) {
                _pass = Base64Coder.encodeString(new BigInteger(130, _random).toString(32));
                setString(PASS, _pass);
            }
        }
        return _pass;
    }

    public boolean hasSeen(){
        return getBoolean(HAS_SEEN, false);
    }

    public void setHasSeen(){
        setBoolean(HAS_SEEN, true);
    }
    public Authent getAuthenticationObject(){
        Authent authent = new Authent(getGuid(), getPass());
        return authent;
    }

    private SharedPreferences getSharedPreferences() {
        if (_shared_preferences == null) {
            _shared_preferences = getSharedPreferences(getClass().getSimpleName(), 0);
        }
        return _shared_preferences;
    }

    private boolean getBoolean(String name, boolean default_value) {
        return getSharedPreferences().getBoolean(name, default_value);
    }

    private void setBoolean(String name, boolean value) {
        getSharedPreferences().edit().putBoolean(name, value).commit();
    }

    private String getString(String name, String default_value) {
        return getSharedPreferences().getString(name, default_value);
    }

    private void setString(String name, String value) {
        getSharedPreferences().edit().putString(name, value).commit();
    }

    public ProxyController getProxyController() {
        return _proxy_controller;
    }
}
