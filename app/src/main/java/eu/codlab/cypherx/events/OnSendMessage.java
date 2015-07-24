package eu.codlab.cypherx.events;

import greendao.Device;

/**
 * Created by kevinleperf on 10/07/15.
 */
public class OnSendMessage {
    private Device _receiver;
    private String _message;

    public OnSendMessage(Device receiver, String message) {
        _receiver = receiver;
        _message = message;
    }

    public Device getReceiver() {
        return _receiver;
    }

    public String getMessage() {
        return _message;
    }
}
