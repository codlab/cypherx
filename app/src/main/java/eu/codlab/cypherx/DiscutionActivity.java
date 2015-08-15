package eu.codlab.cypherx;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.gson.JsonObject;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.List;

import at.markushi.ui.RevealColorView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import eu.codlab.cypherx.database.DevicesController;
import eu.codlab.cypherx.database.MessagesController;
import eu.codlab.cypherx.events.EventProxyOk;
import eu.codlab.cypherx.events.OnMessageDecryptedEvent;
import eu.codlab.cypherx.events.OnMessageToDecryptEvent;
import eu.codlab.cypherx.events.OnSendMessage;
import eu.codlab.cypherx.ui.messages.MessageConstants;
import eu.codlab.cypherx.ui.messages.MessageDecryptHelper;
import eu.codlab.cypherx.ui.messages.MessagesCursor;
import eu.codlab.cypherx.utils.CursorRecyclerViewAdapter;
import eu.codlab.cypherx.utils.FragmentStackManager;
import eu.codlab.cypherx.utils.Keys;
import eu.codlab.cypherx.utils.SystemFittableActivity;
import eu.codlab.cypherx.webservice.WebserviceController;
import eu.codlab.cypherx.webservice.models.DistantMessages;
import greendao.Device;
import greendao.Message;

public class DiscutionActivity extends SystemFittableActivity {
    private final static String GUID = "GUID";

    public static void startDiscutionActivity(Activity activity, String guid) {
        Intent intent = new Intent(activity, DiscutionActivity.class);
        intent.putExtra(GUID, guid);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    private KeyPair _keys;
    private Device _device;

    private Device getDevice() {
        if (_device == null && getIntent() != null && getIntent().hasExtra(GUID)) {
            _device = DevicesController.getInstance(this)
                    .getDevice(getIntent().getStringExtra(GUID));
        }
        return _device;
    }

    @Bind(R.id.reveal)
    RevealColorView _reveal_color_view;

    @Bind(R.id.recycler)
    RecyclerView _recycler;

    @Bind(R.id.edit)
    EditText _edit;

    @OnClick(R.id.send)
    public void onSendClicked() {
        if (_edit.getText().length() == 0) {
            return;
        }

        PublicKey key = _keys.getPublic();
        String clear = _edit.getText().toString();
        //local stored message
        String json_local = MessageDecryptHelper.encryptForLocal(clear, key);
        //distant
        String json = MessageDecryptHelper.encryptForLocal(clear, getDevice().getPublicKey());
        //signature
        String signature = MessageDecryptHelper.encryptForSignature(clear, _keys.getPrivate());

        final Message message = new Message();
        message.setDevice_guid(getDevice().getGuid());
        message.setEncrypted_content(json);
        message.setEncrypted_content_local(json_local);
        message.setReceived_at(new Date());
        message.setType(MessageConstants.SENDING);
        MessagesController.getInstance(this).sendMessage(message);
        ((CursorRecyclerViewAdapter) _recycler.getAdapter()).changeCursor(
                MessagesController.getInstance(this).getCursor(getDevice().getGuid()));

        WebserviceController.getInstance().postMessage(getDevice().getGuid(), json, signature,
                new WebserviceController.IPost() {
                    @Override
                    public void onMessagePosted(List<DistantMessages> messages) {
                        message.setType(MessageConstants.SENT);
                        MessagesController.getInstance(DiscutionActivity.this).sendMessage(message);
                        MessagesController.getInstance(DiscutionActivity.this).onMessages(messages);

                        try {
                            ((CursorRecyclerViewAdapter) _recycler.getAdapter()).changeCursor(
                                    MessagesController.getInstance(DiscutionActivity.this)
                                            .getCursor(getDevice().getGuid()));
                        } catch (Exception exception) {

                        }
                    }

                    @Override
                    public void onMessageError() {
                        message.setType(MessageConstants.NOT_SENT);
                        MessagesController.getInstance(DiscutionActivity.this).sendMessage(message);

                        try {
                            ((CursorRecyclerViewAdapter) _recycler.getAdapter()).changeCursor(
                                    MessagesController.getInstance(DiscutionActivity.this)
                                            .getCursor(getDevice().getGuid()));
                        } catch (Exception exception) {

                        }
                    }
                });

        _edit.setText("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getToolbar().setTitle(R.string.app_name);

        setInsets();

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        manager.setReverseLayout(true);
        _recycler.setLayoutManager(manager);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);


        try {
            _keys = Keys.loadKeyPair(this);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        setupToolbar();
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                _reveal_color_view.reveal(100, 100, getResources().getColor(R.color.colorPrimaryDark), 100, 800, null);
            }
        }, 500);

        setupToolbar();

        _recycler.setAdapter(new MessagesCursor(this,
                getDevice(),
                _keys.getPrivate(),
                MessagesController.getInstance(this).getCursor(getDevice().getGuid())));

        EventBus.getDefault().postSticky(new EventProxyOk());

        Device device = getDevice();
        if (device != null) {
            device.setLast_open_at(new Date());
            DevicesController.getInstance(this)
                    .getDao().insertOrReplace(device);
        }
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
        ((CursorRecyclerViewAdapter) _recycler.getAdapter()).getCursor().close();
    }

    public int getContentView() {
        return R.layout.activity_discution;
    }

    @Override
    protected FragmentStackManager getFragmentStackManager() {
        return new FragmentStackManager(this, 0) {
            @Override
            public void pop() {

            }

            @Override
            public void push(int new_index, Bundle arguments) {

            }

            @Override
            public boolean isMainView() {
                return false;
            }

            @Override
            public boolean navigationBackEnabled() {
                return true;
            }

            @Override
            public boolean isNavigationDrawerEnabled() {
                return true;
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    onBackPressed();
                    return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        _actionbar_toggle.onConfigurationChanged(newConfig);
    }

    protected void setupToolbar() {
        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setTitleTextColor(getResources().getColor(R.color.white));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            getToolbar().setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
    }

    @Subscribe
    public void onEventMainThread(OnSendMessage event) {

    }

    @Subscribe(threadMode = ThreadMode.Async)
    public void onEventAsync(OnMessageToDecryptEvent event) {
        Message message_entity = event.to_decrypt;
        String content = message_entity.getEncrypted_content();
        String local_content = message_entity.getEncrypted_content_local();

        JsonObject local = null;
        if (local_content != null) {
            local = MessageDecryptHelper.decrypt(_keys.getPrivate(), local_content);
        } else if (content != null) {
            local = MessageDecryptHelper.decrypt(_keys.getPrivate(), content);
        }

        String final_message = local != null ? local.getAsJsonPrimitive(MessageDecryptHelper.CONTENT)
                .getAsString() : "";

        OnMessageDecryptedEvent evt = new OnMessageDecryptedEvent();
        evt.id = event.id;
        evt.status = message_entity.getStatusDateMessage();
        evt.receiver = event.receiver;
        evt.content = final_message;
        evt.position = event.position;

        if (event.receiver != null && event.receiver.getId() == event.id) {
            EventBus.getDefault().post(evt);
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEventMainThread(OnMessageDecryptedEvent event) {
        if (event.receiver != null && event.receiver.getId() == event.id) {
            event.receiver._message.setText(event.content);
            event.receiver._status.setText(event.status);
            event.receiver.setCursorId(event.position);
        }
    }
}
