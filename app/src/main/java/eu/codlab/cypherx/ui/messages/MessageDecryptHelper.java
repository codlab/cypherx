package eu.codlab.cypherx.ui.messages;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.security.PrivateKey;
import java.security.PublicKey;

import eu.codlab.crypto.core.stream.CypherRSA;
import eu.codlab.crypto.core.utils.Base64Coder;
import eu.codlab.cypherx.utils.Bytes;
import eu.codlab.cypherx.webservice.WebserviceController;

/**
 * Created by kevinleperf on 11/07/15.
 */
public class MessageDecryptHelper {
    public static final String CONTENT = "CONTENT";
    public static final String LENGTH = "LENGTH";

    public static JsonObject decrypt(PrivateKey _my_key, String encrypted) {
        byte[] res = CypherRSA.decrypt(Base64Coder.decode(encrypted), _my_key);
        res = Bytes.removeTrailingZeroes(res);
        String json = new String(res);
        Log.e("JSON", json);
        return new JsonParser().parse(json).getAsJsonObject();
    }

    public static JsonObject decrypt(PublicKey _my_key, String encrypted) {
        byte[] res = CypherRSA.decrypt(Base64Coder.decode(encrypted), _my_key);
        res = Bytes.removeTrailingZeroes(res);
        String json = new String(res);
        Log.e("JSON", json);
        return new JsonParser().parse(json).getAsJsonObject();
    }

    public static String encryptForLocal(String clear, PublicKey key) {
        JsonObject result = new JsonObject();
        result.addProperty(CONTENT, clear);
        result.addProperty(LENGTH, clear.length());
        clear = result.toString();
        return new String(Base64Coder.encode(CypherRSA.encrypt(clear, key)));
    }

    public static String encryptForSignature(String clear, PrivateKey own_private_key) {
        clear = WebserviceController.hashPassword(null, clear);
        JsonObject result = new JsonObject();
        result.addProperty(CONTENT, clear);
        result.addProperty(LENGTH, clear.length());
        clear = result.toString();
        return new String(Base64Coder.encode(CypherRSA.encrypt(clear, own_private_key)));
    }

    public static boolean equals(String clear, String signature_decoded) {
        return WebserviceController.hashPassword(null, clear).equals(clear);
    }
}
