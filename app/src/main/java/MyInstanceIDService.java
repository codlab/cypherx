import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

import eu.codlab.cypherx.gcm.GcmIntentService;

/**
 * Created by kevinleperf on 02/08/15.
 */

public class MyInstanceIDService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {

        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        GcmIntentService.start(this);

    }
}
