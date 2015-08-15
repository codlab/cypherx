package eu.codlab.cypherx.ui.messages;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.security.PrivateKey;
import java.security.SecureRandom;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import eu.codlab.cypherx.R;
import eu.codlab.cypherx.events.OnMessageToDecryptEvent;
import eu.codlab.cypherx.utils.CursorRecyclerViewAdapter;
import greendao.Device;
import greendao.Message;
import greendao.MessageDao;

/**
 * Created by kevinleperf on 10/07/15.
 */
public class MessagesCursor extends CursorRecyclerViewAdapter<MessagesCursor.ViewHolder> {
    private Device _device;
    private PrivateKey _internal_private_key;
    private SecureRandom _random = new SecureRandom();

    public MessagesCursor(Context context, Device device, PrivateKey key, Cursor cursor) {
        super(context, cursor);
        _internal_private_key = key;
        _device = device;
    }

    public static class ViewHolderLeft extends ViewHolder {
        public ViewHolderLeft(View view) {
            super(view);
        }
    }

    public static class ViewHolderRight extends ViewHolder {
        public ViewHolderRight(View view) {
            super(view);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.message)
        public TextView _message;

        @Bind(R.id.status)
        public TextView _status;

        private long _id;
        private long _cursor_id = -1;

        public long getCursorId() {
            return _cursor_id;
        }

        public void setCursorId(long cursor_id) {
            _cursor_id = cursor_id;
        }

        public long getId() {
            return _id;
        }

        public void setId(long id) {
            _id = id;
        }

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("TYPE", viewType + " ");
        if (viewType == MessageConstants.NOT_SENT || viewType == MessageConstants.SENT
                || viewType == MessageConstants.SENDING) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.discution_message_right, parent, false);
            return new ViewHolderRight(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.discution_message_left, parent, false);
            return new ViewHolderLeft(itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        int current_position = getCursor().getPosition();
        int column_index = getCursor().getColumnIndex(MessageDao.Properties.Type.columnName);
        getCursor().moveToPosition(position);
        int result = getCursor().getInt(column_index);
        getCursor().moveToPosition(current_position);
        return result;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        Message message_entity = Message.fromCursor(cursor);
        if (viewHolder.getCursorId() != message_entity.getId()) {
            viewHolder.setId(_random.nextLong());
            OnMessageToDecryptEvent event = new OnMessageToDecryptEvent();
            event.id = viewHolder.getId();
            event.receiver = viewHolder;
            event.to_decrypt = message_entity;
            event.position = message_entity.getId();

            viewHolder._message.setText(R.string.decoding);
            viewHolder._status.setText("");

            EventBus.getDefault().post(event);
        }
    }
}
