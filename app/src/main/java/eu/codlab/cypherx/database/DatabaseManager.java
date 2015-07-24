package eu.codlab.cypherx.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

import de.greenrobot.dao.query.QueryBuilder;
import greendao.DaoMaster;
import greendao.DaoSession;

/**
 * Created by kevinleperf on 04/07/15.
 */
public class DatabaseManager {
    private static final String DATABASE_NAME = "db";
    private static DaoSession sDaoSession;

    private static DatabaseManager ourInstance = new DatabaseManager();

    public static DatabaseManager getInstance() {
        return ourInstance;
    }

    private DatabaseManager() {

    }

    public void startSession(final Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DATABASE_NAME, null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        sDaoSession = daoMaster.newSession();
        QueryBuilder.LOG_SQL = false;
        QueryBuilder.LOG_VALUES = false;
    }

    protected DaoSession getSession() {
        return sDaoSession;
    }

    /**
     * Make this be return by greendao
     *
     * @param context
     * @param name
     * @param password
     * @return
     */
    public net.sqlcipher.database.SQLiteDatabase createDatabase(Context context, String name, String password) {
        File path = context.getDatabasePath(name);
        if (!path.getParentFile().exists())
            path.getParentFile().mkdirs();
        return net.sqlcipher.database.SQLiteDatabase.openOrCreateDatabase(path, password, null);
    }
}
