package eu.codlab.cypherx.database;

import android.content.Context;
import android.database.Cursor;

import java.util.Date;
import java.util.List;

import eu.codlab.cypherx.ui.messages.MessageConstants;
import eu.codlab.cypherx.webservice.models.DistantMessages;
import greendao.Device;
import greendao.Message;
import greendao.MessageDao;

/**
 * Created by kevinleperf on 28/06/13.
 */
public class MessagesController extends AbstractController<MessageDao> {
    public static String GUID = "guid";

    private static MessagesController _instance;

    private Context _context;

    private MessagesController(Context context) {
        super();
        _context = context;
    }

    private static MessagesController createNewInstance(Context context) {
        return new MessagesController(context);
    }

    public static MessagesController getInstance(Context context) {
        if (_instance == null) _instance = createNewInstance(context);
        return _instance;
    }

    public void sendMessage(final Message message) {
        lock();
        getDao().insertOrReplace(message);
        unlock();
    }

    public void onMessages(List<DistantMessages> messages) {
        if (messages != null) {
            DevicesController ctrl = DevicesController.getInstance(null);
            Date received_at = new Date();
            lock();
            for (DistantMessages distant_message : messages) {
                Message message = new Message();
                message.setDevice_guid(distant_message.getSender());
                message.setEncrypted_content_local(null);
                message.setEncrypted_content(distant_message.getContent());
                message.setSignature(distant_message.getSignature());
                message.setReceived_at(received_at);
                message.setType(MessageConstants.RECEIVED);
                getDao().insertOrReplace(message);

                Device device = ctrl.getDevice(distant_message.getSender());
                if (device != null){
                    device.setLast_message_at(received_at);
                    ctrl.getDao().insertOrReplace(device);
                }
            }
            unlock();
        }
    }

    public MessageDao getDao() {
        return DatabaseManager.getInstance().getSession().getMessageDao();
    }

    public Cursor getCursor(String guid) {
        return getDao().getDatabase().query(MessageDao.TABLENAME, new String[]{
                        MessageDao.Properties.Id.columnName,
                        MessageDao.Properties.Device_guid.columnName,
                        MessageDao.Properties.Encrypted_content.columnName,
                        MessageDao.Properties.Encrypted_content_local.columnName,
                        MessageDao.Properties.Received_at.columnName,
                        MessageDao.Properties.Type.columnName
                },
                MessageDao.Properties.Device_guid.columnName + " = ?",
                new String[]{guid}, null, null, MessageDao.Properties.Received_at.columnName + " DESC", null);
    }

    public Message getLastMessage(Device device) {
        List<Message> message_list = getDao().queryBuilder()
                .where(MessageDao.Properties.Device_guid.eq(device.getGuid()))
                .orderDesc(MessageDao.Properties.Received_at)
                .limit(1).list();

        if (message_list != null && message_list.size() == 1) return message_list.get(0);
        return null;
    }

    public Long getMessagesCount(Device device) {
        return getDao().queryBuilder()
                .where(MessageDao.Properties.Device_guid.eq(device.getGuid()))
                .count();
    }

    public List<Message> getMessagesToPost() {
        return getDao().queryBuilder()
                .where(MessageDao.Properties.Type.eq(MessageConstants.SENDING))
                .list();
    }
}
