package eu.codlab.cypherx.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import eu.codlab.cypherx.MainActivity;
import eu.codlab.cypherx.R;
import eu.codlab.cypherx.events.OnRegistrationIdObtainedEvent;
import eu.codlab.cypherx.webservice.WebserviceController;
import retrofit.client.Response;

/**
 * Created by kevinleperf on 01/04/2014.
 */
public class GcmIntentService extends IntentService {
    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, GcmIntentService.class);
        ctx.startService(intent);
    }

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        InstanceID instanceID = InstanceID.getInstance(this);
        try {
            String token = instanceID.getToken("85511368887",
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Log.d("GCM", "push obtained " + token);
            WebserviceController.getInstance()
                    .registerPush(token, new WebserviceController.IPush() {
                        @Override
                        public void onRegisterDone(Response response) {

                        }

                        @Override
                        public void onRegisterError() {

                        }
                    });

            EventBus.getDefault().post(new OnRegistrationIdObtainedEvent(token));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_ic_launcher)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
