package eu.codlab.cypherx;

import android.app.Application;
import android.content.SharedPreferences;

import com.crashlytics.android.Crashlytics;

import net.danlew.android.joda.JodaTimeAndroid;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.UUID;

import eu.codlab.crypto.core.utils.Base64Coder;
import eu.codlab.crypto.core.utils.PRNGFixes;
import eu.codlab.cypherx.database.DatabaseManager;
import eu.codlab.cypherx.proxy.ProxyController;
import eu.codlab.cypherx.tuto.TutoHelper;
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
    private final static String PUSH_AUTHORIZED = "PUSH_AUTHORIZED";
    private static ApplicationController _instance;

    private SecureRandom _random = new SecureRandom();
    private SharedPreferences _shared_preferences;
    private String _guid = null;
    private String _pass = null;
    private ProxyController _proxy_controller;

    private HashMap<String, Boolean> _values = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
        PRNGFixes.apply();
        _instance = this;
        Fabric.with(this, new Crashlytics());
        DatabaseManager.getInstance().startSession(this);

        Mutex.initAll();
        _proxy_controller = new ProxyController(this);
    }

    public static ApplicationController getInstance() {
        return _instance;
    }

    public String getGuid() {
        if (_guid == null) {
            _guid = getString(GUID, null);
            if (_guid == null) {
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

    public boolean hasPushAuthorized() {
        return getBoolean(PUSH_AUTHORIZED, false);
    }

    public void setPushAuthorized(boolean value) {
        setBoolean(PUSH_AUTHORIZED, value);
    }

    public boolean hasSeen() {
        return getBoolean(HAS_SEEN, false);
    }

    public void setHasSeen() {
        setBoolean(HAS_SEEN, true);
    }

    public Authent getAuthenticationObject() {
        Authent authent = new Authent(getGuid(), getPass());
        return authent;
    }

    private SharedPreferences getSharedPreferences() {
        if (_shared_preferences == null) {
            _shared_preferences = getSharedPreferences(getClass().getSimpleName(), 0);
        }
        return _shared_preferences;
    }

    public boolean getBoolean(String name, boolean default_value) {
        if (_values.get(name) == null) {
            boolean value = getSharedPreferences().getBoolean(name, default_value);
            _values.put(name, value);
            return value;
        } else {
            return _values.get(name);
        }
    }

    public void setBoolean(String name, boolean value) {
        getSharedPreferences().edit().putBoolean(name, value).commit();
        _values.put(name, value);
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
