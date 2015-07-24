package eu.codlab.cypherx.ui.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import eu.codlab.cypherx.R;
import eu.codlab.cypherx.events.OnShareEvent;
import eu.codlab.cypherx.events.OpenDeviceActivity;
import eu.codlab.markdown.MarkdownView;
import greendao.Device;
import greendao.Message;

/**
 * Created by kevinleperf on 04/07/15.
 */
public class RecyclerAdapter extends RecyclerView.Adapter {
    private final static SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSSZ");
    private List<Device> _devices;
    private int TEXTVIEWHOLDER = 0;
    private int SHARE_VIEW_HOLDER = 1;
    private int DESCRIPTION_VIEW_HOLDER = 2;
    private int AREA_DEVICE = 3;
    private int AREA_DEVICE_EMPTY = 4;
    private int AREA_DEVICE_LOADING = 5;

    public class AreaDevice extends RecyclerView.ViewHolder {
        private Device _device;

        @Bind(R.id.device_name)
        public TextView _name;

        @Bind(R.id.device_information)
        public TextView _information;

        @OnClick(R.id.card_view)
        public void onShareClick() {
            EventBus.getDefault().post(new OpenDeviceActivity(_device));
        }

        public AreaDevice(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setDevice(Device device) {
            _device = device;
        }
    }

    private class DescriptionViewHolder extends RecyclerView.ViewHolder {
        public DescriptionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class ShareViewHolder extends RecyclerView.ViewHolder {

        @OnClick(R.id.share)
        public void onShareClick() {
            EventBus.getDefault().post(new OnShareEvent());
        }

        @OnClick(R.id.card_view)
        public void onShareParentViewClick() {
            EventBus.getDefault().post(new OnShareEvent());
        }

        public ShareViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class ShareInformationViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.markdown_encrypted)
        MarkdownView _markdown_encrypted;

        @Bind(R.id.markdown_share_with)
        MarkdownView _markdown_share_with;

        @Bind(R.id.markdown_debug)
        MarkdownView _markdown_debug;

        @Bind(R.id.markdown_enhance)
        MarkdownView _markdown_enhance;

        public ShareInformationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


        public void setShareInformation(MarkdownView view, int asset){
            view.setAssetContent(itemView.getContext().getString(asset));
        }
    }

    private class TextViewHolder extends RecyclerView.ViewHolder {
        private TextView _view;

        public TextViewHolder(View itemView) {
            super(itemView);
            _view = (TextView) itemView;
            ButterKnife.bind(this, itemView);
        }
    }

    public RecyclerAdapter() {

    }

    public RecyclerAdapter(List<Device> devices) {
        _devices = devices;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        if (type == TEXTVIEWHOLDER) {
            return new TextViewHolder(new TextView(viewGroup.getContext()));
        } else if (type == AREA_DEVICE) {
            return new AreaDevice(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.main_view_device, viewGroup, false));
        } else if (type == AREA_DEVICE_LOADING) {
            return new DescriptionViewHolder(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.main_view_loading, viewGroup, false));
        } else if (type == AREA_DEVICE_EMPTY) {
            return new DescriptionViewHolder(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.main_view_empty, viewGroup, false));
        } else if (type == SHARE_VIEW_HOLDER) {
            return new ShareViewHolder(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.main_view_share, viewGroup, false));
        } else {
            return new ShareInformationViewHolder(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.main_view_information, viewGroup, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (_devices != null) {
            if (_devices.size() > 0) {
                if (position < _devices.size()) {
                    if (_devices.get(0) == null)
                        return AREA_DEVICE_LOADING;
                    return AREA_DEVICE;
                }
                return SHARE_VIEW_HOLDER;
            }
            return AREA_DEVICE_EMPTY;
        }

        if (position == 0)
            return SHARE_VIEW_HOLDER;
        return DESCRIPTION_VIEW_HOLDER;
    }

    private void setShareInformation(MarkdownView view, RecyclerView.ViewHolder v, int asset){
        view.setAssetContent(v.itemView.getContext().getString(asset));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ShareInformationViewHolder) {
            ShareInformationViewHolder v = (ShareInformationViewHolder) viewHolder;

            v.setShareInformation(v._markdown_encrypted, R.string.markdown_encrypted);
            v.setShareInformation(v._markdown_share_with, R.string.markdown_share_with);
            v.setShareInformation(v._markdown_debug, R.string.markdown_debug);
            v.setShareInformation(v._markdown_enhance, R.string.markdown_enhance);

        } else if (viewHolder instanceof TextViewHolder) {
            ((TextViewHolder) viewHolder)._view.setText("info " + position);
        } else if (viewHolder instanceof AreaDevice) {
            Device device = _devices.get(position);
            ((AreaDevice) viewHolder).setDevice(device);
            Message last_message = device.getLastMessage();
            String information = "";
            if (last_message != null && last_message.getReceived_at() != null) {
                information = viewHolder.itemView.getContext()
                        .getString(R.string.device_information_date,
                                format.format(last_message.getReceived_at()));
            } else {
                information = viewHolder.itemView.getContext()
                        .getString(R.string.device_information_no_message);
            }
            ((AreaDevice) viewHolder)._information.setText(information);
            ((AreaDevice) viewHolder)._name.setText(device.getGuid());
        }
    }

    @Override
    public int getItemCount() {
        if (_devices != null) {
            if (_devices.size() > 0)
                return _devices.size() + 1;
            return 1;
        }
        return 2;
    }
}
