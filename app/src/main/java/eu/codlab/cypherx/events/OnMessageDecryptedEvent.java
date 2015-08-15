package eu.codlab.cypherx.events;

import eu.codlab.cypherx.ui.messages.MessagesCursor;

/**
 * Created by kevinleperf on 14/07/15.
 */
public class OnMessageDecryptedEvent {
    public MessagesCursor.ViewHolder receiver;
    public String content;
    public String status;
    public long id;
    public long position;
}
