package eu.codlab.cypherx.utils;

import android.content.Context;
import android.util.Log;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import eu.codlab.crypto.core.utils.Base64Coder;
import eu.codlab.crypto.core.utils.Constants;
import eu.codlab.cypherx.database.ConfigController;
import greendao.Config;

/**
 * Created by kevinleperf on 04/07/15.
 */
public class Keys {

    public static void saveKeyPublicPrivate(Context context, PublicKey publicKey, PrivateKey privateKey) {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
        ConfigController.getInstance(context)
                .setConfig(ConfigController.PUBLIC_KEY, new String(Base64Coder.encode(x509EncodedKeySpec.getEncoded())))
                .setConfig(ConfigController.PRIVATE_KEY, new String(Base64Coder.encode(pkcs8EncodedKeySpec.getEncoded())));
    }

    public static void saveKeyPair(Context context, KeyPair keyPair) {
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        saveKeyPublicPrivate(context, publicKey, privateKey);
    }

    public static KeyPair loadKeyPair(Context context)
            throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        if (!areKeysPresent(context)) {
            Log.d("Keys", "having key :: absent");
            return null;
        }

        Config pub = ConfigController.getInstance(context).getConfig(ConfigController.PUBLIC_KEY);
        Config priv = ConfigController.getInstance(context).getConfig(ConfigController.PRIVATE_KEY);
        byte[] encodedPublicKey = Base64Coder.decode(pub.getContent());
        byte[] encodedPrivateKey = Base64Coder.decode(priv.getContent());

        KeyFactory keyFactory = KeyFactory.getInstance(Constants.ALGORITHM);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
        Log.d("Keys", "having key :: ok");

        return new KeyPair(publicKey, privateKey);
    }

    public static boolean areKeysPresent(Context context) {
        Config pub = ConfigController.getInstance(context).getConfig(ConfigController.PUBLIC_KEY);
        Config priv = ConfigController.getInstance(context).getConfig(ConfigController.PRIVATE_KEY);
        return pub != null && pub.isContentSet() && priv != null && priv.isContentSet();
    }

}
