package com.example.javris.myapplication;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;


public class DeviceAdapter extends BaseAdapter {

    private Context mContext;
    private List<DevInfo> mInfos;

    public DeviceAdapter(Context context, List<DevInfo> infos) {
        mContext = context;
        mInfos = infos;
    }

    public void changeData(List<DevInfo> infos) {
        if (infos != null && infos.size() != 0) {
            mInfos = infos;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.list_item, null);
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        DevInfo info = mInfos.get(position);
        vh.mDeviceName.setText(info.getName());
        vh.mDeviceMac.setText(info.getMacAddress());
        return convertView;
    }

    class ViewHolder {
        TextView mDeviceName;
        TextView mDeviceMac;

        public ViewHolder(View convertView) {
            mDeviceName = (TextView) convertView.findViewById(R.id.tv_name);
            mDeviceMac = (TextView) convertView.findViewById(R.id.tv_mac);
        }
    }
}
