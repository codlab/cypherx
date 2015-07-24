package eu.codlab.cypherx.database;

import android.content.Context;
import android.util.Log;
import android.util.LruCache;

import de.greenrobot.dao.AbstractDao;
import greendao.Config;
import greendao.ConfigDao;

/**
 * Created by kevinleperf on 28/06/13.
 */
public class ConfigController extends AbstractController<ConfigDao>{
    private static LruCache<String, Config> _lru = new LruCache<>(20);
    public static String PUBLIC_KEY = "public_key";
    public static String PRIVATE_KEY = "private_key";

    private static ConfigController _instance;

    private Context _context;

    private ConfigController(Context context) {
        super();
        _context = context;
    }

    private static ConfigController createNewInstance(Context context) {
        return new ConfigController(context);
    }

    public static ConfigController getInstance(Context context) {
        if (_instance == null) _instance = createNewInstance(context);
        return _instance;
    }

    public ConfigController setConfig(String title, String content) {
        setConfig(new Config(0l, title, content));
        return this;
    }

    public long setConfig(Config config) {
        lock();
        Log.e("E","e1");
        Config conf = getConfig(config.getName());
        Log.e("E","e2");
        if (conf == null) {
            conf = new Config();
            Log.e("E","e3");
            conf.setId(null);//config.getId());
            Log.e("E","e4");
            conf.setName(config.getName());
            Log.e("E","e5");
            conf.setContent(config.getContent());
            Log.e("E","e6");
        }
        Log.e("E","e7");
        long result = getDao().insertOrReplace(conf);
        unlock();
        return result;
    }

    public Config getConfig(String title) {
        Config cache = _lru.get(title);
        if (cache == null) {
            cache = getDao().queryBuilder()
                    .where(ConfigDao.Properties.Name.eq(title)).unique();
            if (cache != null) {
                _lru.put(title, cache);
            }
        }
        return cache;
    }

    public ConfigDao getDao() {
        return DatabaseManager.getInstance().getSession().getConfigDao();
    }
}
