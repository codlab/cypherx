package eu.codlab.cypherx.webservice.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by kevinleperf on 11/07/15.
 */
public class DistantMessages {
    @SerializedName("message")
    private String _content;

    @SerializedName("sender")
    private String _sender;

    @SerializedName("signature")
    private String _signature;

    public DistantMessages() {

    }

    public DistantMessages(String content, String sender, String signature) {
        _content = content;
        _sender = sender;
        _signature = signature;
    }

    public String getContent() {
        return _content;
    }

    public String getSender() {
        return _sender;
    }

    public String getSignature() {
        return _signature;
    }
}
