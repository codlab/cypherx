package eu.codlab.cypherx;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import eu.codlab.cypherx.account.authenticator.AuthenticatorActivity;
import eu.codlab.cypherx.utils.Keys;

/**
 * This activity is only here to redirect to :
 * - create account / register
 * - start dashboard if account known /!\
 */
public class HomeActivity extends Activity {
    public static final String WITH_CONFIGURATION = "WITH_CONFIGURATION";
    private AccountManager mAccountManager;
    private static boolean opened_create_account = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        opened_create_account = false;
        mAccountManager = AccountManager.get(this);
        if (hasAccount()) {
            startDashboard();
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();


        if (hasAccount()) {
            startDashboard();
            finish();
        } else {
            if (opened_create_account) {
                finish();
                return;
            }
            opened_create_account = true;
            createAccount();
        }
    }

    public static void startHome(Activity activity) {
        Intent intent = new Intent(activity, HomeActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    public void startDashboard() {
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private boolean hasAccount() {
        Account[] accounts = mAccountManager.getAccountsByType(AuthenticatorActivity.ACCOUNT_TYPE);
        return accounts != null && accounts.length > 0 && Keys.areKeysPresent(this);
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
}
