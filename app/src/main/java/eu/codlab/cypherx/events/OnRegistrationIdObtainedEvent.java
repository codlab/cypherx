package eu.codlab.cypherx.events;

/**
 * Created by kevinleperf on 02/08/15.
 */
public class OnRegistrationIdObtainedEvent {
    private String _gcm_id;

    public OnRegistrationIdObtainedEvent(String gcm_id){
        _gcm_id = gcm_id;
    }

    public String getRegistrationId(){
        return _gcm_id;
    }
}
