package com.bupt.iheath.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bupt.iheath.R;
import com.bupt.iheath.model.AlarmInfo;
import com.bupt.iheath.utils.NotifyUtils;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.List;

public class RemindEventAdapter extends RecyclerView.Adapter<RemindEventAdapter.ViewHolder> {

    private final List<AlarmInfo> mValues;
    private final RemindActivity.OnItemLongClickListener mListener;

    public RemindEventAdapter(List<AlarmInfo> items, RemindActivity.OnItemLongClickListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.remindevent_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        holder.mcommentView.setText(mValues.get(position).comment);
        holder.mTimeView.setText(mValues.get(position).timeLabel);
        holder.mNameView.setText(mValues.get(position).name);

        holder.mNameView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (null != mListener) {
                    mListener.onLongClick(holder.mItem, position);
                }
                return true;
            }
        });
        holder.mTimeView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (null != mListener) {
                    mListener.onLongClick(holder.mItem, position);
                }
                return true;
            }
        });
        holder.mcommentView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (null != mListener) {
                    mListener.onLongClick(holder.mItem, position);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }


    public void addItem(AlarmInfo item) {
        mValues.add(item);
        int position = mValues.indexOf(item) ;
        notifyItemInserted(position);
    }

    public void addItem(AlarmInfo item, int position) {
        if (position <=  -1  || position>mValues.size()) {
            mValues.add(0, item);
        }
        mValues.add(position, item);
        notifyItemInserted(position);
    }

    public void removeItem(AlarmInfo item) {
        int position = mValues.indexOf(item);
        mValues.remove(position);
        notifyItemRemoved(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final MaterialEditText mcommentView;
        public final MaterialEditText mTimeView;
        public final TextView mNameView;
        public AlarmInfo mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mcommentView = (MaterialEditText) view.findViewById(R.id.event_comment);
            mTimeView = (MaterialEditText) view.findViewById(R.id.event_time);
            mNameView = (TextView) view.findViewById(R.id.event_name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTimeView.getText() + "'";
        }
    }
}
