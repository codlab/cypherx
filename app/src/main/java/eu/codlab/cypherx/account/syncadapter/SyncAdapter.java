package eu.codlab.cypherx.account.syncadapter;

/**
 * Created by kevinleperf on 22/05/15.
 */

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import java.util.List;

import eu.codlab.cypherx.database.MessagesController;
import eu.codlab.cypherx.webservice.WebserviceController;
import eu.codlab.cypherx.webservice.models.DistantMessages;
import greendao.Message;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        List<DistantMessages> messages = WebserviceController.getInstance().getMessagesSync();
        manageMessages(messages);

        List<Message> messages_to_post = MessagesController.getInstance(getContext())
                .getMessagesToPost();
        for (Message message : messages_to_post) {
            messages = WebserviceController.getInstance().postMessageSync(message);
            manageMessages(messages);
        }

    }

    private void manageMessages(List<DistantMessages> messages) {
        if (messages != null) {
            MessagesController.getInstance(getContext())
                    .onMessages(messages);
        }
    }
}