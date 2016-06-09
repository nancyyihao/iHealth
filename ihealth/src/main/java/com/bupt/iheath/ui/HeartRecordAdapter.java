package com.bupt.iheath.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bupt.iheath.R;
import com.bupt.iheath.model.UserState;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by jumper on 2016/4/13.
 */
public class HeartRecordAdapter extends BaseAdapter {

    private LayoutInflater mInflater = null;
    private Context mContext ;
    private List<UserState> data = new ArrayList<>();

    public HeartRecordAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        mContext = context ;
    }

    public void addItem(UserState item) {
        data.add(0, item);
        if (data.size()>10) {
            data.remove(10) ;
        }
        notifyDataSetChanged();
    }

    public void addItemAtLast(UserState item) {
        data.add(data.size(), item);
        if (data.size()>10) {
            data.remove(10) ;
        }
        notifyDataSetChanged();
    }

    public void removeItem(UserState userState) {
        data.remove(userState);
        notifyDataSetChanged();
    }

    public UserState getUserStateByIndex(int position) {
        return data.get(position) ;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        //如果缓存convertView为空，则需要创建View
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.heart_record_item, null);
            holder.tv_heart = (TextView) convertView.findViewById(R.id.tv_heart);
            holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        int heart = data.get(position).getHeartRate();
        String heartText = mContext.getString(R.string.heart_record_item_heart, heart) ;
        holder.tv_heart.setText(heartText);

        //Date date = data.get(position).getWhen();
        //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //holder.tv_date.setText(format.format(date));
        holder.tv_date.setText(data.get(position).getWhen());
        return convertView;
    }
    //ViewHolder静态类
    static class ViewHolder {
        public TextView tv_heart;
        public TextView tv_date;
    }
}

