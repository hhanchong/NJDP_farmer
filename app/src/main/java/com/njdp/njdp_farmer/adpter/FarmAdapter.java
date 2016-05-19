package com.njdp.njdp_farmer.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.njdp.njdp_farmer.R;
import com.njdp.njdp_farmer.MyClass.FarmlandInfo;

import java.util.List;

/**
 * expandableListView适配器
 *
 */
public class FarmAdapter extends BaseExpandableListAdapter {
	private final String[][] cropsType = new String[][]{{"H","收割"}, {"C", "耕作"}, {"S", "播种"},
			{"WH", "小麦"}, {"CO", "玉米"}, {"RC", "水稻"}, {"GR", "谷物"}, {"OT", "其他"}, {"SS", "深松"}, {"HA", "平地"}};
	private Context context;
	private List<String> group;
	private List<List<FarmlandInfo>> child;

	public FarmAdapter(Context context, List<String> group,
					   List<List<FarmlandInfo>> child) {
		this.context = context;
		this.group = group;
		this.child = child;
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
					R.layout.list_farmgroup, null);
			holder = new ViewHolder();
			holder.textView = (TextView) convertView
					.findViewById(R.id.textView);
			holder.textView1 = (TextView) convertView
					.findViewById(R.id.textView1);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String[] temp = group.get(groupPosition).split("-");
		holder.textView.setText(temp[0]);
		holder.textView.setTextSize(18);
		holder.textView.setPadding(36, 10, 0, 6);
		holder.textView1.setText(temp[1] + "-" + temp[2] + "-" + temp[3]);
		holder.textView1.setPadding(36, 0, 0, 10);
		holder.textView.getPaint().setFakeBoldText(true); //加粗
		return convertView;
	}
	
	/**
	 * 显示：child
	 */
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.list_farmchild, null);
			viewHolder = new ViewHolder();
			viewHolder.cropkind = (TextView) convertView.findViewById(R.id.crop_kind);
			viewHolder.status = (TextView)convertView.findViewById(R.id.status);
			viewHolder.area = (TextView)convertView.findViewById(R.id.area);
			viewHolder.price = (TextView)convertView.findViewById(R.id.price);
			viewHolder.blocktype = (TextView)convertView.findViewById(R.id.block_type);
			viewHolder.address = (TextView)convertView.findViewById(R.id.address);
			viewHolder.starttime = (TextView)convertView.findViewById(R.id.start_time);
			viewHolder.endtime = (TextView)convertView.findViewById(R.id.end_time);
			viewHolder.remark = (TextView)convertView.findViewById(R.id.remark);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.cropkind.setText("作业类型：" + ConvertToCHS(child.get(groupPosition).get(childPosition).getCrops_kind()));
		viewHolder.status.setText("作业状态："+ (child.get(groupPosition).get(childPosition).getStatus().equals("0")?"未完成":"已完成"));
		viewHolder.area.setText("面积 (亩)："+child.get(groupPosition).get(childPosition).getArea());
		viewHolder.price.setText("单价 (元)："+child.get(groupPosition).get(childPosition).getUnit_price());
		viewHolder.blocktype.setText("地块类型："+child.get(groupPosition).get(childPosition).getBlock_type());
		viewHolder.address.setText(child.get(groupPosition).get(childPosition).getProvince() + child.get(groupPosition).get(childPosition).getCity() +
				child.get(groupPosition).get(childPosition).getCounty() + child.get(groupPosition).get(childPosition).getTown() +
				child.get(groupPosition).get(childPosition).getVillage());
		viewHolder.starttime.setText("开始时间："+child.get(groupPosition).get(childPosition).getStart_time_String());
		viewHolder.endtime.setText("结束时间："+child.get(groupPosition).get(childPosition).getEnd_time_String());
		viewHolder.remark.setText("补充说明："+child.get(groupPosition).get(childPosition).getRemark());
		return convertView;
	}

	class ViewHolder {
		//子节点使用
		LinearLayout layout;
		TextView cropkind, status, area, price, blocktype, address, starttime, endtime, remark;
		//父节点使用
		TextView textView, textView1;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	//类型转换为中文
	private String ConvertToCHS(String s){
		String operation = "", crop = "";

		if(s.length() == 3){
			for(int i = 0; i < cropsType.length; i++){
				if(cropsType[i][0].equals(s.substring(0,1))){
					operation = cropsType[i][1];
				}
				if(cropsType[i][0].equals(s.substring(1,3))){
					crop = cropsType[i][1];
				}
			}
			if(operation.isEmpty() || crop.isEmpty()){
				operation = "未知";
				crop = "";
			}
		}else {
			operation = "未知";
		}
		return operation + crop;
	}

}
