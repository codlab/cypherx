package eu.codlab.cypherx.webservice;

import com.google.gson.JsonObject;

import java.util.List;

import eu.codlab.cypherx.webservice.models.Authent;
import eu.codlab.cypherx.webservice.models.DistantMessages;
import eu.codlab.cypherx.webservice.models.PostMessage;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;

/**
 * Created by kevinleperf on 11/07/15.
 */
public interface IWebInterface {
    @POST("/x/device")
    public void authenticateTemp(@Body Authent authent, Callback<JsonObject> result);

    @POST("/x/device")
    public JsonObject authenticateTempSync(@Body Authent authent);

    @PUT("/x/device")
    public void registerToServer(@Body Authent register, Callback<JsonObject> result);

    @PUT("/x/device")
    public JsonObject registerToServerSync(@Body Authent register);

    @GET("/x/messages")
    public void getMessages(Callback<List<DistantMessages>> result);

    @GET("/x/messages")
    public List<DistantMessages> getMessagesSync();

    @PUT("/x/messages")
    public void postMessages(@Body PostMessage post, Callback<List<DistantMessages>> result);

    @PUT("/x/messages")
    public List<DistantMessages> postMessagesSync(@Body PostMessage post);

}
