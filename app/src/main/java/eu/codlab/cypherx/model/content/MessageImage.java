package eu.codlab.cypherx.model.content;

import org.json.JSONObject;

/**
 * Created by kevinleperf on 01/04/2014.
 */
public class MessageImage extends MessageContent {
    protected MessageImage(boolean incognito) {
        super(incognito);
    }

    @Override
    public void fromJSON(JSONObject object) {

    }

    @Override
    public JSONObject toJSON() {
        try{
            JSONObject obj = new JSONObject();
            obj.put("type",MessageString.STRING);
            obj.put("incognito", _incognito);

            return obj;
        }catch(Exception e){

        }
        return null;
    }
}
