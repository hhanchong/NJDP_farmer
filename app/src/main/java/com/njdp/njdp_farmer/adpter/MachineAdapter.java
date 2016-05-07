package com.njdp.njdp_farmer.adpter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.njdp.njdp_farmer.MyClass.MachineInfo;
import com.njdp.njdp_farmer.R;

import java.util.List;

/**
 * expandableListView适配器
 *
 */
public class MachineAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String[]> group;
    private List<List<MachineInfo>> child;
    private MyClickListener mListener;

    public MachineAdapter(Context context, List<String[]> group,
                       List<List<MachineInfo>> child, MyClickListener listener) {
        this.context = context;
        this.group = group;
        this.child = child;
        mListener = listener;
    }

    @Override
    public int getGroupCount() {
        return group.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return child.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return group.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return child.get(childPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * 显示：group
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.list_machinegroup, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView
                    .findViewById(R.id.imageView);
            holder.tv_name = (TextView) convertView
                    .findViewById(R.id.tv_name);
            holder.tv_range = (TextView) convertView
                    .findViewById(R.id.tv_range);
            holder.tv_phone = (TextView) convertView
                    .findViewById(R.id.tv_phone);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if(!group.get(groupPosition)[0].equals("")) {
            holder.imageView.setImageURI(Uri.parse(group.get(groupPosition)[0]));
        }
        holder.tv_name.setText("机主：" + group.get(groupPosition)[1]);
        holder.tv_range.setText("距离：" + group.get(groupPosition)[2] + "km");
        holder.tv_phone.setText("电话：" + group.get(groupPosition)[3]);
        return convertView;
    }

    /**
     * 显示：child
     */
    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.list_machinechild, null);
            viewHolder = new ViewHolder();
            viewHolder.driver_name = (TextView) convertView.findViewById(R.id.driver_name);
            viewHolder.driver_phone = (TextView)convertView.findViewById(R.id.driver_phone);
            viewHolder.call = (Button)convertView.findViewById(R.id.phoneBtn);
            viewHolder.range = (TextView)convertView.findViewById(R.id.range);
            viewHolder.work_time = (TextView)convertView.findViewById(R.id.work_time);
            viewHolder.qq = (TextView)convertView.findViewById(R.id.qq);
            viewHolder.weixin = (TextView)convertView.findViewById(R.id.weixin);
            viewHolder.remark = (TextView)convertView.findViewById(R.id.remark);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.driver_name.setText("机主姓名：" + child.get(groupPosition).get(childPosition).getName());
        viewHolder.driver_phone.setText("机主电话："+ child.get(groupPosition).get(childPosition).getTelephone());
        if(viewHolder.call != null) {
            viewHolder.call.setOnClickListener(mListener);
            viewHolder.call.setTag(groupPosition);
        }
        viewHolder.range.setText("距离："+child.get(groupPosition).get(childPosition).getRange() + "km");
        viewHolder.work_time.setText("工作时间："+child.get(groupPosition).get(childPosition).getWork_time() + "小时/天");
        viewHolder.qq.setText(" QQ ："+child.get(groupPosition).get(childPosition).getQq());
        viewHolder.weixin.setText("微信："+child.get(groupPosition).get(childPosition).getWeixin());
        viewHolder.remark.setText("补充说明："+child.get(groupPosition).get(childPosition).getRemark());
        return convertView;
    }

    class ViewHolder {
        Button call;
        ImageView imageView;
        TextView tv_name, tv_range, tv_phone, driver_name, driver_phone, range, work_time, qq, weixin, remark;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    /**
     * 用于回调的抽象类
     * @author Ivan Xu
     * 2016-05-07
     * */
    public static abstract class MyClickListener implements View.OnClickListener {
        /**
         * 基类的onClick方法
         */
        @Override
        public void onClick(View v) {
             myOnClick((Integer) v.getTag(), v);
            }
        public abstract void myOnClick(int position, View v);
        }
}
