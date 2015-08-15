package eu.codlab.cypherx.events;

import eu.codlab.cypherx.ui.main.RecyclerAdapter;
import greendao.Device;

/**
 * Created by kevinleperf on 09/07/15.
 */
public class OpenDeviceActivity {
    private Device _device;
    private RecyclerAdapter _adapter;

    public OpenDeviceActivity(Device device, RecyclerAdapter adapter) {
        _device = device;
        _adapter = adapter;
    }

    public Device getDevice(){
        return _device;
    }

    public RecyclerAdapter getAdapter(){
        return _adapter;
    }
}
