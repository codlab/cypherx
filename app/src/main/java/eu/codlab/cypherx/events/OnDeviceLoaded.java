package eu.codlab.cypherx.events;

import java.util.List;

import greendao.Device;

/**
 * Created by kevinleperf on 09/07/15.
 */
public class OnDeviceLoaded {
    private List<Device> _devices;

    public OnDeviceLoaded(List<Device> devices) {
        _devices = devices;
    }

    public List<Device> getDevices() {
        return _devices;
    }
}
