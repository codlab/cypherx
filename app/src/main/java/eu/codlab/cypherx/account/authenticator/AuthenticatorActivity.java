package eu.codlab.cypherx.account.authenticator;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

/**
 * Created by kevinleperf on 22/05/15.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {
    public static final String ACCOUNT_TYPE = "eu.codlab.cypherx";
    public static final String ARG_ACCOUNT_TYPE = "ARG_ACCOUNT_TYPE";
    public static final String ARG_AUTH_TYPE = "ARG_AUTH_TYPE";
    public static final String ARG_IS_ADDING_NEW_ACCOUNT = "ARG_IS_ADDING_NEW_ACCOUNT";
    public static final String ARG_ACCOUNT_NAME = "ARG_ACCOUNT_NAME";

    public static final int REQUEST_CODE = 10988193;

    private AccountManager mAccountManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent login_activity = new Intent(this, GeneratingKeysActivity.class);
        startActivityForResult(login_activity, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            setAccountOK();
        } else {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {

    }

    protected void setAccountOK() {
        mAccountManager = AccountManager.get(this);
        final Account account = new Account("CypherX", ACCOUNT_TYPE);
        mAccountManager.setPassword(account, "");
        mAccountManager.addAccountExplicitly(account, null, null);
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, "CypherX");
        intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, true);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }
}
