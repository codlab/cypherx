package eu.codlab.cypherx.webservice;

import android.util.Base64;

import com.google.gson.JsonObject;
import com.squareup.okhttp.OkHttpClient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import eu.codlab.cypherx.ApplicationController;
import eu.codlab.cypherx.BuildConfig;
import eu.codlab.cypherx.events.EventProxyOk;
import eu.codlab.cypherx.proxy.ProxyController;
import eu.codlab.cypherx.utils.WsseToken;
import eu.codlab.cypherx.webservice.models.Authent;
import eu.codlab.cypherx.webservice.models.DistantMessages;
import eu.codlab.cypherx.webservice.models.PostMessage;
import eu.codlab.cypherx.webservice.models.PutPush;
import greendao.Message;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

/**
 * Created by kevinleperf on 11/07/15.
 */
public class WebserviceController {
    public interface IAuthenticate {
        void onSessionOk(String session);

        void onSessionError();
    }

    public interface IRegister {
        void onRegisterOk(String session);

        void onRegisterError();
    }

    public interface IPost {
        void onMessagePosted(List<DistantMessages> messages);

        void onMessageError();
    }

    public interface IPush {
        void onRegisterDone(Response response);

        void onRegisterError();
    }

    public interface IGet {
        void onMessagesRetrieved(List<DistantMessages> messages);

        void onMessagesError();
    }


    private static WebserviceController _instance = new WebserviceController();

    public static WebserviceController getInstance() {
        return _instance;
    }

    private RestAdapter _adapter;
    private IWebInterface _web_interface;
    private WsseToken _token;

    private WebserviceController() {
        recreateRestAdapter();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEventMainThread(EventProxyOk proxy) {
        recreateRestAdapter();
    }

    private void recreateRestAdapter() {
        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(BuildConfig.ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        if (_token != null) {
                            request.addHeader(WsseToken.HEADER_AUTHORIZATION, _token.getAuthorizationHeader());
                            request.addHeader(WsseToken.HEADER_WSSE, _token.getWsseHeader());
                        }
                    }
                });

        if (ApplicationController.getInstance().getProxyController().isOrbotRunning()) {
            OkHttpClient client = new OkHttpClient();
            client.setProxy(new ProxyController(ApplicationController.getInstance()).getProxy());
            builder.setClient(new OkClient(client));
        }
        _adapter = builder.build();
        _web_interface = _adapter.create(IWebInterface.class);

    }

    public String registerSync() {
        final Authent authent = ApplicationController.getInstance().getAuthenticationObject();

        JsonObject object = null;

        try {
            object = _web_interface.authenticateTempSync(authent);
        } catch (RetrofitError error) {

        }

        if (object != null && object.has("session")) {
            _token = new WsseToken(object.getAsJsonPrimitive("session").getAsString(),
                    hashPassword(null, authent.pass));
            return object.getAsJsonPrimitive("session").getAsString();
        } else {
        }

        return null;
    }

    public void register(final IRegister callback) {
        final Authent authent = ApplicationController.getInstance().getAuthenticationObject();

        _web_interface.registerToServer(authent, new Callback<JsonObject>() {
            @Override
            public void success(JsonObject jsonObject, Response response) {
                if (jsonObject.has("session")) {
                    _token = new WsseToken(jsonObject.getAsJsonPrimitive("session").getAsString(),
                            hashPassword(null, authent.pass));
                    callback.onRegisterOk(jsonObject.getAsJsonPrimitive("session").getAsString());
                } else {
                    callback.onRegisterError();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onRegisterError();
            }
        });
    }

    public String authenticateSync() {
        final Authent authent = ApplicationController.getInstance().getAuthenticationObject();

        JsonObject jsonObject = null;

        try {
            jsonObject = _web_interface.authenticateTempSync(authent);
        } catch (RetrofitError error) {

        }
        if (jsonObject.has("session")) {
            _token = new WsseToken(jsonObject.getAsJsonPrimitive("session").getAsString(),
                    hashPassword(null, authent.pass));
            return _token.getPassword();
        } else {
        }
        return null;
    }

    public void authenticate(final IAuthenticate callback) {
        final Authent authent = ApplicationController.getInstance().getAuthenticationObject();

        _web_interface.authenticateTemp(authent, new Callback<JsonObject>() {
            @Override
            public void success(JsonObject jsonObject, Response response) {
                if (jsonObject.has("session")) {
                    _token = new WsseToken(jsonObject.getAsJsonPrimitive("session").getAsString(),
                            hashPassword(null, authent.pass));
                    callback.onSessionOk(_token.getPassword());
                } else {
                    callback.onSessionError();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onSessionError();
            }
        });
    }

    public void getMessages(String receiver, final IGet callback) {
        getMessages(callback, true);
    }

    public void getMessages(final IGet callback, final boolean try_authenticate) {

        Callback<List<DistantMessages>> first_call = new Callback<List<DistantMessages>>() {
            @Override
            public void success(List<DistantMessages> answer, Response response) {
                int code = response.getStatus();
                if ((code & 200) == 200) {
                    callback.onMessagesRetrieved(answer);
                } else {
                    onError(code);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                int code = error.getResponse() != null ? error.getResponse().getStatus() : 0;
                callback.onMessagesError();

                onError(code);
            }

            private void onError(int code) {
                if (try_authenticate) {
                    if (code == 401) {
                        authenticate(new IAuthenticate() {
                            @Override
                            public void onSessionOk(String session) {
                                getMessages(callback, false);
                            }

                            @Override
                            public void onSessionError() {
                                callback.onMessagesError();
                            }
                        });
                    } else if (code == 403) {
                        register(new IRegister() {
                            @Override
                            public void onRegisterOk(String session) {
                                getMessages(callback, false);
                            }

                            @Override
                            public void onRegisterError() {
                                callback.onMessagesError();
                            }
                        });
                    }
                }
            }
        };

        _web_interface.getMessages(first_call);
    }

    public List<DistantMessages> getMessagesSync() {
        return getMessagesSync(true);
    }

    private List<DistantMessages> onGetMessagesError(int code, boolean try_authenticate) {
        if (try_authenticate) {
            if (code == 401) {
                String session = authenticateSync();
                if (session != null) {
                    return getMessagesSync(false);
                }
                return null;

            } else if (code == 403) {
                String session = registerSync();
                if (session != null) {
                    return getMessagesSync(false);
                }
                return null;
            }
        }
        return null;
    }

    public List<DistantMessages> getMessagesSync(final boolean try_authenticate) {

        List<DistantMessages> messages = null;

        try {
            messages = _web_interface.getMessagesSync();
        } catch (RetrofitError error) {
            error.printStackTrace();
            int code = error.getResponse() != null ? error.getResponse().getStatus() : 0;
            return onGetMessagesError(code, try_authenticate);
        }
        return messages;
    }

    public List<DistantMessages> postMessageSync(String receiver, String content, String signature) {
        final PostMessage message_to_post = new PostMessage();
        message_to_post.content = content;
        message_to_post.receiver = receiver;
        message_to_post.signature = signature;

        return postMessageSync(message_to_post, true);
    }

    public List<DistantMessages> postMessageSync(Message message) {
        PostMessage post_message = new PostMessage();
        post_message.content = message.getEncrypted_content();
        post_message.receiver = message.getDevice_guid();
        post_message.signature = message.getSignature();
        return postMessageSync(post_message, true);
    }

    public void postMessage(String receiver, String content, String signature,
                            final IPost callback) {
        final PostMessage message_to_post = new PostMessage();
        message_to_post.content = content;
        message_to_post.receiver = receiver;
        message_to_post.signature = signature;

        postMessage(message_to_post, callback, true);
    }

    private void postMessage(final PostMessage message_to_post,
                             final IPost callback, final boolean try_authenticate) {

        Callback<List<DistantMessages>> first_call = new Callback<List<DistantMessages>>() {
            @Override
            public void success(List<DistantMessages> answer, Response response) {
                int code = response.getStatus();
                android.util.Log.d("answer", "" + answer);
                if (answer != null) {
                    callback.onMessagePosted(answer);
                } else {
                    onError(code);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
                int code = error.getResponse() != null ? error.getResponse().getStatus() : 0;
                callback.onMessageError();

                onError(code);
            }

            private void onError(int code) {
                if (try_authenticate) {
                    if (code == 401) {
                        authenticate(new IAuthenticate() {
                            @Override
                            public void onSessionOk(String session) {
                                postMessage(message_to_post, callback, false);
                            }

                            @Override
                            public void onSessionError() {
                                callback.onMessageError();
                            }
                        });
                    } else if (code == 403) {
                        register(new IRegister() {
                            @Override
                            public void onRegisterOk(String session) {
                                postMessage(message_to_post, callback, false);
                            }

                            @Override
                            public void onRegisterError() {
                                callback.onMessageError();
                            }
                        });
                    }
                }
            }
        };

        _web_interface.postMessages(message_to_post, first_call);
    }

    public void registerPush(String gcm,
                            final IPush callback) {
        final PutPush to_push = new PutPush();
        to_push.gcm = gcm;

        registerPush(to_push, callback, true);
    }

    private void registerPush(final PutPush to_push,
                             final IPush callback, final boolean try_authenticate) {

        Callback<Response> first_call = new Callback<Response>() {
            @Override
            public void success(Response answer, Response response) {
                int code = response.getStatus();
                android.util.Log.d("answer", "" + answer);
                if (answer != null) {
                    callback.onRegisterDone(answer);
                } else {
                    onError(code);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
                int code = error.getResponse() != null ? error.getResponse().getStatus() : 0;
                callback.onRegisterError();

                onError(code);
            }

            private void onError(int code) {
                if (try_authenticate) {
                    if (code == 401) {
                        authenticate(new IAuthenticate() {
                            @Override
                            public void onSessionOk(String session) {
                                registerPush(to_push, callback, false);
                            }

                            @Override
                            public void onSessionError() {
                                callback.onRegisterError();
                            }
                        });
                    } else if (code == 403) {
                        register(new IRegister() {
                            @Override
                            public void onRegisterOk(String session) {
                                registerPush(to_push, callback, false);
                            }

                            @Override
                            public void onRegisterError() {
                                callback.onRegisterError();
                            }
                        });
                    }
                }
            }
        };

        _web_interface.registerPush(to_push, first_call);
    }

    private List<DistantMessages> postMessageSync(PostMessage message_to_post, boolean try_authenticate) {

        List<DistantMessages> messages = null;
        try {
            messages = _web_interface.postMessagesSync(message_to_post);
            if (messages != null) {
            }
        } catch (RetrofitError error) {
            int code = error.getResponse() != null ? error.getResponse().getStatus() : 0;
            return onPostMessagesError(message_to_post, code, try_authenticate);
        }
        return messages;
    }

    private List<DistantMessages> onPostMessagesError(PostMessage message, int code,
                                                      boolean try_authenticate) {
        if (try_authenticate) {
            if (code == 401) {
                String session = authenticateSync();
                if (session != null) {
                    return postMessageSync(message, false);
                }
                return null;

            } else if (code == 403) {
                String session = registerSync();
                if (session != null) {
                    return postMessageSync(message, false);
                }
                return null;
            }
        }
        return null;
    }

    public static String hashPassword(String salt, String clearPassword) {
        String hash = "";
        try {
            String salted = null;
            if (salt == null || "".equals(salt)) {
                salted = clearPassword;
            } else {
                salted = clearPassword + "{" + salt + "}";
            }
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte sha[] = md.digest(salted.getBytes());
            for (int i = 1; i < 5000; i++) {
                byte c[] = new byte[sha.length + salted.getBytes().length];
                System.arraycopy(sha, 0, c, 0, sha.length);
                System.arraycopy(salted.getBytes(), 0, c, sha.length, salted.getBytes().length);
                sha = md.digest(c);
            }
            hash = new String(Base64.encode(sha, Base64.NO_WRAP));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }
}
