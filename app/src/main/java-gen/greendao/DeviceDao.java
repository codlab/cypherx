package greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import greendao.Device;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table DEVICE.
*/
public class DeviceDao extends AbstractDao<Device, Long> {

    public static final String TABLENAME = "DEVICE";

    /**
     * Properties of entity Device.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Guid = new Property(1, String.class, "guid", false, "GUID");
        public final static Property Key = new Property(2, String.class, "key", false, "KEY");
        public final static Property Last_open_at = new Property(3, java.util.Date.class, "last_open_at", false, "LAST_OPEN_AT");
        public final static Property Last_message_at = new Property(4, java.util.Date.class, "last_message_at", false, "LAST_MESSAGE_AT");
    };


    public DeviceDao(DaoConfig config) {
        super(config);
    }
    
    public DeviceDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'DEVICE' (" + //
                "'_id' INTEGER PRIMARY KEY ," + // 0: id
                "'GUID' TEXT NOT NULL ," + // 1: guid
                "'KEY' TEXT NOT NULL ," + // 2: key
                "'LAST_OPEN_AT' INTEGER," + // 3: last_open_at
                "'LAST_MESSAGE_AT' INTEGER);"); // 4: last_message_at
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'DEVICE'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Device entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getGuid());
        stmt.bindString(3, entity.getKey());
 
        java.util.Date last_open_at = entity.getLast_open_at();
        if (last_open_at != null) {
            stmt.bindLong(4, last_open_at.getTime());
        }
 
        java.util.Date last_message_at = entity.getLast_message_at();
        if (last_message_at != null) {
            stmt.bindLong(5, last_message_at.getTime());
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Device readEntity(Cursor cursor, int offset) {
        Device entity = new Device( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // guid
            cursor.getString(offset + 2), // key
            cursor.isNull(offset + 3) ? null : new java.util.Date(cursor.getLong(offset + 3)), // last_open_at
            cursor.isNull(offset + 4) ? null : new java.util.Date(cursor.getLong(offset + 4)) // last_message_at
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Device entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setGuid(cursor.getString(offset + 1));
        entity.setKey(cursor.getString(offset + 2));
        entity.setLast_open_at(cursor.isNull(offset + 3) ? null : new java.util.Date(cursor.getLong(offset + 3)));
        entity.setLast_message_at(cursor.isNull(offset + 4) ? null : new java.util.Date(cursor.getLong(offset + 4)));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Device entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Device entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
