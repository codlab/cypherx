package eu.codlab.cypherx.utils;

import android.app.Activity;

import eu.codlab.cypherx.ApplicationController;
import eu.codlab.cypherx.model.MessageWrite;

/**
 * Created by kevinleperf on 27/04/2014.
 */
public class UrlsHelper {

    public static String getPublicInfoURL(Activity activity, String key) {
        return "https://cypher.codlab.eu/x/me/"
                + ApplicationController.getInstance().getGuid() + "/" + key + "/";
    }

    public static String getDecodeURL(MessageWrite write) {
        return "https://cypher.codlab.eu/x/decode/" + write.getSenderIdentifier() + "/" + write.getEncodedMessage();
    }
}
