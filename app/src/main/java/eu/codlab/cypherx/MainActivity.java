package eu.codlab.cypherx;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.markushi.ui.RevealColorView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import eu.codlab.cypherx.account.authenticator.AuthenticatorActivity;
import eu.codlab.cypherx.database.DevicesController;
import eu.codlab.cypherx.events.EventProxyOk;
import eu.codlab.cypherx.events.OnDeviceLoaded;
import eu.codlab.cypherx.events.OnGetRegistrationIdEvent;
import eu.codlab.cypherx.events.OnLoadDevices;
import eu.codlab.cypherx.events.OnRegistrationIdObtainedEvent;
import eu.codlab.cypherx.events.OnShareEvent;
import eu.codlab.cypherx.events.OpenDeviceActivity;
import eu.codlab.cypherx.gcm.GcmBroadcastReceiver;
import eu.codlab.cypherx.gcm.GcmIntentService;
import eu.codlab.cypherx.tuto.TutoHelper;
import eu.codlab.cypherx.ui.main.MainActivityController;
import eu.codlab.cypherx.ui.main.RecyclerAdapter;
import eu.codlab.cypherx.utils.FragmentStackManager;
import eu.codlab.cypherx.utils.Keys;
import eu.codlab.cypherx.utils.SystemFittableActivity;
import eu.codlab.cypherx.views.Switch;
import greendao.Device;

public class MainActivity extends SystemFittableActivity {
    private AccountManager mAccountManager;
    private static boolean opened_create_account = false;
    private boolean _should_load_devices = false;


    private MainActivityController _controller;

    private synchronized MainActivityController getController() {
        if (_controller == null) _controller = new MainActivityController(this);
        return _controller;
    }

    private boolean _can_load;
    private TutoHelper _tuto_helper;

    @Nullable
    @Bind(R.id.collapsing)
    CollapsingToolbarLayout _collapsing_toolbar_layout;

    @Bind(R.id.reveal)
    RevealColorView _reveal_color_view;

    @Bind(R.id.recycler)
    RecyclerView _recycler;

    protected View mMenuView;

    @Nullable
    @Bind(R.id.drawerLayout)
    protected DrawerLayout mDrawerLayout;

    @Bind(R.id.tor_switch)
    protected Switch _tor_switch;

    @Bind(R.id.push_switch)
    protected Switch _push_switch;

    @OnClick(R.id.main)
    public void onClickMain() {
        _can_load = false;
        _recycler.setAdapter(new RecyclerAdapter(this));
        closeDrawer();
    }

    @OnClick(R.id.tor)
    public void onClickTor() {
        if (_tor_switch.isChecked()) {
            _tor_switch.setChecked(false);
        } else {
            //_tor_switch.setChecked(true);
            if (ApplicationController.getInstance().getProxyController().isOrbotInstalled()) {
                ApplicationController.getInstance().getProxyController().requestOrbotStart(this);
            } else {
                ApplicationController.getInstance().getProxyController().promptToInstall(this);
            }
        }
    }

    @OnClick(R.id.chat)
    public void onClickChat() {
        _can_load = true;
        List<Device> devices = new ArrayList<>();
        devices.add(null);
        _recycler.setAdapter(new RecyclerAdapter(this, devices));
        EventBus.getDefault().post(new OnLoadDevices());
        ApplicationController.getInstance().setHasSeen();
    }

    @OnClick(R.id.push)
    public void onPushSwitch() {
        if (_push_switch.isChecked()) {
            ApplicationController.getInstance().setPushAuthorized(false);
            _push_switch.setChecked(false);
        } else {
            ApplicationController.getInstance().setPushAuthorized(true);
            GcmIntentService.start(this);
        }
    }

    @OnClick(R.id.clone)
    public void onClone() {
        Uri uri = Uri.parse("https://github.com/codlab/cypherx");
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(browserIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _tuto_helper = new TutoHelper();

        opened_create_account = false;
        mAccountManager = AccountManager.get(this);
        _can_load = false;

        getToolbar().setTitle(R.string.app_name);
        if (_collapsing_toolbar_layout != null) {
            _collapsing_toolbar_layout.setTitle(getString(R.string.app_name));
            _collapsing_toolbar_layout.setCollapsedTitleTextColor(Color.WHITE);
            _collapsing_toolbar_layout.setExpandedTitleColor(Color.WHITE);
        }

        setInsets();

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        _recycler.setLayoutManager(manager);
        if (ApplicationController.getInstance().hasSeen()) {
            _recycler.setAdapter(new RecyclerAdapter(this, new ArrayList<Device>()));
            _should_load_devices = true;
        } else {
            _recycler.setAdapter(new RecyclerAdapter(this));
        }

        if (mDrawerLayout != null) {
            _actionbar_toggle = new ActionBarDrawerToggle(
                    this,
                    mDrawerLayout,         /* DrawerLayout object */
                    getToolbar(),
                    0,
                    0) {

                /**
                 * Called when a drawer has settled in a completely closed state.
                 */
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                }

                /**
                 * Called when a drawer has settled in a completely open state.
                 */
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    hideKeyboard(mDrawerLayout);

                    getTutoHelper().openTuto(MainActivity.this, "TUTORIAL_SHARE2",
                            R.string.tutorial_tor_title, R.string.tutorial_tor_text,
                            _tor_switch);

                    getTutoHelper().openTuto(MainActivity.this, "TUTORIAL_SHARE3",
                            R.string.tutorial_push_title, R.string.tutorial_push_text,
                            _push_switch);

                }
            };
            mDrawerLayout.setDrawerListener(_actionbar_toggle);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (ApplicationController.getInstance().hasPushAuthorized()) {
            GcmIntentService.start(this);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        setupToolbar();
        setupDrawer();
    }

    @Override
    public void onResume() {
        super.onResume();

        GcmBroadcastReceiver.removeNotification(this);

        if (hasAccount()) {

            EventBus.getDefault().register(this);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    _reveal_color_view.reveal(100, 100, Color.parseColor("#ffffff"), 100, 800, null);
                }
            }, 500);

            setupToolbar();
            setupDrawer();

            if (getIntent() != null && getIntent().getData() != null) {

                onNewUri(getIntent().getData());
            }

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (ApplicationController.getInstance().getProxyController().isOrbotInstalled()
                            && ApplicationController.getInstance().getProxyController().isOrbotRunning()) {
                        EventBus.getDefault().postSticky(new EventProxyOk());
                        _tor_switch.setChecked(true);
                    }
                }
            }, 600);

            requestSync();


            if (_should_load_devices) {
                _can_load = true;
                EventBus.getDefault().post(new OnLoadDevices());
                _should_load_devices = false;
            }
        } else {
            if (opened_create_account) {
                finish();
                return;
            }
            opened_create_account = true;
            createAccount();
        }
    }

    public void onNewUri(Uri uri) {

        int saved = getController().onNewUri(this, uri);

        if (MainActivityController.SAVED == saved) {
            _can_load = true;
            Toast.makeText(getApplicationContext(), getString(R.string.device_added), Toast.LENGTH_LONG).show();
            EventBus.getDefault().post(new OnLoadDevices());
        } else if (MainActivityController.NOT_SAVED == saved) {
            //nothing
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.device_add_error), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    public int getContentView() {
        return R.layout.activity_main;
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
        Log.d("MainActivity", "isDrawerOpen() :: " + isDrawerOpen());
        Log.d("MainActivity", "getFragmentStackManager().isNavigationDrawerEnabled() :: " + getFragmentStackManager().isNavigationDrawerEnabled());

        if (item != null) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    if (mDrawerLayout != null && getFragmentStackManager().isNavigationDrawerEnabled()) {
                        mDrawerLayout.openDrawer(GravityCompat.START);
                    } else {
                        onBackPressed();
                    }
                    return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (isDrawerOpen()) {
            closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        boolean changed = newConfig.orientation != getResources().getConfiguration().orientation;
        super.onConfigurationChanged(newConfig);
        _actionbar_toggle.onConfigurationChanged(newConfig);

        if (changed) {
            if (_tuto_helper != null)
                _tuto_helper.clean();
            _tuto_helper = new TutoHelper();
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEventMainThread(OnShareEvent share_event) {
        Intent shared_intent = getShareIntent();
        startActivity(Intent.createChooser(shared_intent, getString(R.string.share_via)));
    }

    private boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    private void setupDrawer() {
        mMenuView = new View(this);
        mMenuView.setBackgroundColor(Color.TRANSPARENT);
        mMenuView.setPadding(0, getPaddingInsetTop(false), 0, 0);


        if (mDrawerLayout != null) {
            getToolbar().setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                    } else {
                        mDrawerLayout.openDrawer(GravityCompat.START);
                    }
                }
            });
        }
    }

    protected void setupToolbar() {
        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (mDrawerLayout != null) {
                toolbar.setNavigationIcon(R.drawable.ic_menu_white);
            }
            toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        }
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
    }

    private Intent getShareIntent() {
        String share_string = getController().createUriString(this);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.personnal_data_subject));
        intent.putExtra(android.content.Intent.EXTRA_TEXT, share_string);
        return intent;
    }

    @Subscribe(threadMode = ThreadMode.Async)
    public void onAsync(OnLoadDevices load) {
        OnDeviceLoaded event = EventBus.getDefault().getStickyEvent(OnDeviceLoaded.class);
        if (event == null) {
            List<Device> list = new ArrayList<Device>();
            list.add(null);
            event = new OnDeviceLoaded(list);
        }
        EventBus.getDefault().postSticky(event);
        List<Device> devices = DevicesController.getInstance(this).findAllOrderByLastAtDesc();
        event = new OnDeviceLoaded(devices);
        EventBus.getDefault().postSticky(event);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEventMainThread(OnDeviceLoaded devices) {
        if (_can_load) {
            _recycler.setAdapter(new RecyclerAdapter(this, devices.getDevices()));
            closeDrawer();
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEventMainThread(OpenDeviceActivity event) {

        event.getDevice().setLast_open_at(new Date());
        DevicesController.getInstance(this).getDao().insertOrReplace(event.getDevice());
        DiscutionActivity.startDiscutionActivity(this, event.getDevice().getGuid());
        if (event.getAdapter() != null)
            event.getAdapter().notifyDataSetChanged();
    }


    private void closeDrawer() {
        if (mDrawerLayout != null)
            mDrawerLayout.closeDrawer(GravityCompat.START);
    }


    private boolean hasAccount() {
        Account[] accounts = mAccountManager.getAccountsByType(AuthenticatorActivity.ACCOUNT_TYPE);
        return accounts != null && accounts.length > 0 && Keys.areKeysPresent(this);
    }

    private void requestSync() {
        Account[] accounts = mAccountManager.getAccountsByType(AuthenticatorActivity.ACCOUNT_TYPE);
        for (Account account : accounts) {
            ContentResolver.requestSync(account, "eu.codlab.cypherx.sync.provider", new Bundle());
        }
    }

    private void createAccount() {
        final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(AuthenticatorActivity.ACCOUNT_TYPE,
                AuthenticatorActivity.ACCOUNT_TYPE, null, null, this, new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            Bundle bnd = future.getResult();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, null);
    }

    @Subscribe(threadMode = ThreadMode.Async)
    public void onGetRegistrationIdEvent(OnGetRegistrationIdEvent event) {
        if (event != null) {
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onRegistrationIdObtained(OnRegistrationIdObtainedEvent event) {
        if (event != null && event.getRegistrationId() != null) {
            _push_switch.setChecked(true);
        }
    }


    public TutoHelper getTutoHelper() {
        return _tuto_helper;
    }
}
