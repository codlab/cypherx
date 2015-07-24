package eu.codlab.cypherx.events;

import greendao.Device;

/**
 * Created by kevinleperf on 09/07/15.
 */
public class OpenDeviceActivity {
    private Device _device;

    public OpenDeviceActivity(Device device) {
        _device = device;
    }

    public Device getDevice(){
        return _device;
    }
}
