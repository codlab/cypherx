package eu.codlab.cypherx.proxy;

import android.app.Activity;
import android.content.Context;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import info.guardianproject.onionkit.ui.OrbotHelper;

public class ProxyController {
    private String CONSTANT_URL = "127.0.0.1";
    private int CONSTANT_PORT = 8118;
    private OrbotHelper _helper;
    private Context _context;

    public ProxyController(Context context) {
        _context = context;
        _helper = new OrbotHelper(context);
    }
    public Proxy getProxy(){
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(CONSTANT_URL, CONSTANT_PORT));
        return proxy;
    }
    public URLConnection openConnection(URL url) throws IOException {
        if(_helper.isOrbotRunning()){
            return url.openConnection(getProxy());
        } else {
            return url.openConnection();
        }
    }

    public boolean isOrbotInstalled(){
        return _helper.isOrbotInstalled();
    }

    public boolean isOrbotRunning(){
        return _helper.isOrbotRunning();
    }

    public void promptToInstall(Activity activity) {
        _helper.promptToInstall(activity);
    }

    public void requestOrbotStart(Activity activity) {
        _helper.requestOrbotStart(activity);
    }
}
