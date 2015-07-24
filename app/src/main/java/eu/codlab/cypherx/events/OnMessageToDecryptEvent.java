package eu.codlab.cypherx.events;

import eu.codlab.cypherx.ui.messages.MessagesCursor;
import greendao.Message;

/**
 * Created by kevinleperf on 14/07/15.
 */
public class OnMessageToDecryptEvent {
    public MessagesCursor.ViewHolder receiver;
    public Message to_decrypt;
    public long id;
}
