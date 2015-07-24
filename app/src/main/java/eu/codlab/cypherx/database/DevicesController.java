package eu.codlab.cypherx.database;

import android.content.Context;
import android.util.LruCache;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import greendao.Device;
import greendao.DeviceDao;

/**
 * Created by kevinleperf on 28/06/13.
 */
public class DevicesController extends AbstractController<DeviceDao> {
    private static LruCache<String, Device> _lru = new LruCache<>(20);
    public static String GUID = "guid";
    public static String PUBLIC_KEY = "public_key";

    private static DevicesController _instance;

    private Context _context;

    private DevicesController(Context context) {
        super();
        _context = context;
    }

    private static DevicesController createNewInstance(Context context) {
        return new DevicesController(context);
    }

    public static DevicesController getInstance(Context context) {
        if (_instance == null) _instance = createNewInstance(context);
        return _instance;
    }

    public boolean hasDevice(String guid) {
        return getDevice(guid) != null;
    }

    public DevicesController addDevice(String guid, String public_key) {
        addDevice(new Device(0l, guid, public_key));
        return this;
    }

    public long addDevice(Device device) {
        lock();
        Device dev = getDevice(device.getGuid());
        if (dev == null) {
            dev = new Device();
            dev.setId(null);//config.getId());
            dev.setGuid(device.getGuid());
            dev.setKey(device.getKey());
        }
        long result = getDao().insertOrReplace(dev);
        unlock();
        return result;
    }

    public Device getDevice(String guid) {
        Device cache = _lru.get(guid);
        if (cache == null) {
            cache = getDao().queryBuilder()
                    .where(DeviceDao.Properties.Guid.eq(guid)).unique();
            if (cache != null) {
                _lru.put(guid, cache);
            }
        }
        return cache;
    }

    public DeviceDao getDao() {
        return DatabaseManager.getInstance().getSession().getDeviceDao();
    }

    public List<Device> findAll() {
        List<Device> list = getDao().loadAll();
        for (Device device : list) {
            device.getLastMessage();
        }
        Collections.sort(list, new Comparator<Device>() {
            @Override
            public int compare(Device device_1, Device device_2) {
                return device_1.getCountMessages().compareTo(device_2.getCountMessages());
            }
        });

        return list;
    }
}
