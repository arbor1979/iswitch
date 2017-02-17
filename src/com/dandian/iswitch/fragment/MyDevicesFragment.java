package com.dandian.iswitch.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.dandian.iswitch.R;
import com.dandian.iswitch.activity.DeviceDetailActivity;
import com.dandian.iswitch.activity.LineChartActivity;
import com.dandian.iswitch.activity.PieChartActivity;
import com.dandian.iswitch.activity.WifiSetupActivity;
import com.dandian.iswitch.entity.Device;
import com.dandian.iswitch.utility.DateHelper;
import com.dandian.iswitch.utility.DateTimePickDialogUtil1;
import com.dandian.iswitch.utility.MyRunnable;
import com.dandian.iswitch.utility.MyUtility;
import com.dandian.iswitch.utility.SegmentedGroup;
import com.dandian.iswitch.utility.ThreadPoolUtils;

public class MyDevicesFragment extends Fragment {

	private ListView list1;
	private List<Device> mData;
	private AQuery aq;
	private MyAdapter adapter;
	private ProgressBar pb1;
	private TextView addDev,refresh,tv_fromtime,tv_totime,privilege,usersNum;
	private RadioButton btn21,btn22;
	private Button fromtime,totime,quxianbutton,bingtubutton;
	private LinearLayout layfromto;
	private Timer timer;
	private TimerTask task;
	private Comparator<Device> comparator;
	private RadioGroup ordergroup;
	public MyDevicesFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);
		addDev=(TextView)rootView.findViewById(R.id.rightButton);
		refresh=(TextView)rootView.findViewById(R.id.leftButton);
		TextView title=(TextView)rootView.findViewById(R.id.titleView);
		list1=(ListView)rootView.findViewById(R.id.userList);
		pb1=(ProgressBar)rootView.findViewById(R.id.progressBar1);
		SegmentedGroup segGroup=(SegmentedGroup)rootView.findViewById(R.id.segmentedGroup2);
		segGroup.setVisibility(View.VISIBLE);
		title.setVisibility(View.GONE);
		segGroup.setTintColor(Color.DKGRAY);
		
		btn21 = (RadioButton) rootView.findViewById(R.id.button21);
		btn22 = (RadioButton) rootView.findViewById(R.id.button22);
		if(!btn21.isChecked() && !btn22.isChecked())
			btn21.setChecked(true);
		
		fromtime=(Button)rootView.findViewById(R.id.fromtime);
		totime=(Button)rootView.findViewById(R.id.totime);
		tv_fromtime=(TextView)rootView.findViewById(R.id.tv_fromtime);
		tv_totime=(TextView)rootView.findViewById(R.id.tv_totime);
		layfromto=(LinearLayout)rootView.findViewById(R.id.linearfromto);
		
		//title.setText(R.string.title_activity_wifi_list);
		addDev.setText(R.string.action_settings);
		refresh.setText(R.string.refresh);
		tv_fromtime.setText(DateHelper.getToday("yyyy-MM-dd 00:00"));
		tv_totime.setText(DateHelper.getToday("yyyy-MM-dd 23:59"));
		
		ordergroup = (RadioGroup)rootView.findViewById(R.id.radioGroup1);
        //绑定一个匿名监听器
        
		aq = new AQuery(getActivity());
		
		View headerView = getActivity().getLayoutInflater().inflate( 
		        R.layout.mydeviceslisthead, null); 
		quxianbutton=(Button)headerView.findViewById(R.id.quxianbutton);
		bingtubutton=(Button)headerView.findViewById(R.id.bingtubutton);
		privilege=(TextView)headerView.findViewById(R.id.devicePrivilege);
		privilege.setText(R.string.manager);
		usersNum=(TextView)headerView.findViewById(R.id.usersNum);
		usersNum.setText(R.string.users);
		list1.addHeaderView(headerView);
		
		mData= new ArrayList<Device>();
		adapter = new MyAdapter(getActivity());
        list1.setAdapter(adapter);
        comparator = new MyComparator();
        
		addDev.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(!MyUtility.isConnect(getActivity()))
				{
					Toast.makeText(getActivity(),  R.string.checkinternet, Toast.LENGTH_LONG).show();
					return;
				}
				Intent intent = new Intent(getActivity(),WifiSetupActivity.class);
				startActivity(intent);
			     
			    }
		} );
		refresh.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(!MyUtility.isConnect(getActivity()))
				{
					Toast.makeText(getActivity(),  R.string.checkinternet, Toast.LENGTH_LONG).show();
					return;
				}
				getMyDevices();
			     
			}
		} );
		list1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Device device=mData.get(position-1);
				/*
				if(device.getStatus().equals("在线"))
				{
					Toast.makeText(getActivity(),  R.string.waitforstatus, Toast.LENGTH_LONG).show();
					return;
				}
				*/
				Intent intent =new Intent(getActivity(),DeviceDetailActivity.class);
				intent.putExtra("device", device);
				startActivity(intent);
				
			}
		});
		segGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch (checkedId) {
		         case R.id.button21:
		        	 layfromto.setVisibility(View.GONE);
		        	 privilege.setVisibility(View.VISIBLE);
		        	 usersNum.setVisibility(View.VISIBLE);
		        	
		        	 getMyDevices();
		             return;
		         case R.id.button22:
		        	 layfromto.setVisibility(View.VISIBLE);
		        	 privilege.setVisibility(View.GONE);
		        	 usersNum.setVisibility(View.GONE);
		        	 
		        	 getMyDevices();
		             return;
				 }
			}
			
		});
		fromtime.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				DateTimePickDialogUtil1 dialog = new DateTimePickDialogUtil1(getActivity(),tv_fromtime.getText().toString(),"yyyy-MM-dd HH:mm",getString(R.string.fromtime));
				Button bt=(Button)v;
				bt.setTag(tv_fromtime);
				dialog.dateTimePicKDialog(bt,mHandler);
				
			}});
		totime.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				DateTimePickDialogUtil1 dialog = new DateTimePickDialogUtil1(getActivity(),tv_totime.getText().toString(),"yyyy-MM-dd HH:mm",getString(R.string.totime));
				Button bt=(Button)v;
				bt.setTag(tv_totime);
				dialog.dateTimePicKDialog(bt,mHandler);
				
			}});
		
		ordergroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				getMyDevices();
			}
			

			});
		quxianbutton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent=new Intent(getActivity(),LineChartActivity.class);
				startActivity(intent);
			}
			
		});
		bingtubutton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(getActivity(),PieChartActivity.class);
				startActivity(intent);
			}
			
		});
		return rootView;
	}
	
	@Override
	public  void onResume()
	{
		super.onResume();
		if(MyUtility.isConnect(getActivity()))
			getMyDevices();
		
		if(timer==null)
			timer = new Timer(true);
		task = new MyTimerTask();
		timer.schedule(task,0, 10000);
		
	}
	
	 @Override
	public  void onPause()
	{
		super.onPause();
		if(task!=null)
			task.cancel();
		
	}
	class MyTimerTask extends TimerTask
	{  
	      public void run() {  
	      Message message = new Message();      
	      message.what = 2;      
	      mHandler.sendMessage(message);    
	   }  
	};
	
	
	
	private void getMyDevices()
	{
		//pb1.setVisibility(View.VISIBLE);
		String url="http://school.eduyun.la/wulian/functions.php?action=getMyDevices";
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String userName=pref.getString("userName","");
		String password=pref.getString("password","");
	 	JSONObject json=new JSONObject();
	 	try {
			json.put("username",userName);
			json.put("password",password);
			if(btn22.isChecked())
			{
				json.put("fromtime", tv_fromtime.getText().toString());
				json.put("totime", tv_totime.getText().toString());
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        ThreadPoolUtils.execute(new MyRunnable(url,"post",MyUtility.jsonToEntity(json),mHandler,1));
	}
	
	private Handler mHandler = new Handler() {

		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			case -1:
				pb1.setVisibility(View.INVISIBLE);
				//Toast.makeText(getActivity(), "失败:"+msg.obj, Toast.LENGTH_LONG).show();
				break;
			case 1:
				pb1.setVisibility(View.INVISIBLE);
				JSONObject jo = null;
				try 
				{
					jo = new JSONObject((String)msg.obj);
				
					if(jo!=null)
					{
						String tips = jo.optString("result");
						if(tips.equals("suc"))
						{
							mData.clear();
							JSONArray jarr=jo.getJSONArray("deviceList");
							for(int i=0;i<jarr.length();i++)
							{
								Device dev=new Device((JSONObject)jarr.get(i));
								if(dev!=null)
									mData.add(dev);
							}
							float dianliang=0;
							for(Device item:mData)
							{
								if(item.getDianLiang()>dianliang)
									dianliang=item.getDianLiang();
							}
							if(dianliang>0)
							{
								if(dianliang<10)
									dianliang=10;
								for(int i=0;i<mData.size();i++)
								{
									mData.get(i).setPercentDianliang((float)(mData.get(i).getDianLiang())/dianliang);
								}
							}
							
							if(btn22.isChecked() && ordergroup.getCheckedRadioButtonId()==R.id.rdOrder1)
							{
								Collections.sort(mData, comparator); 
							}
							adapter.notifyDataSetChanged();
							//getSwitchStatusOpenOrClose();
						}
						else
							Toast.makeText(getActivity(), "失败:"+tips, Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			
			case 2:
				if(MyUtility.isConnect(getActivity()))
					getMyDevices();
				break;
			
			case 3:
				String result=(String)msg.obj;
				String[] resultArray=result.split(":");
				if(resultArray.length==3 && resultArray[0]!=null)
				{
					String status=resultArray[1];
					String mac=resultArray[2];
					if(status.equals("断开") || status.equals("吸合"))
					{
						for(Device item:mData)
						{
							if(item.getMacAddress().equals(mac))
							{
								if(status.equals("吸合"))
								{
									item.setStatus("开启");
								}
								else
								{
									item.setStatus("关闭");
								}
							}
						}
						adapter.notifyDataSetChanged();
					}
				}
				break;
			}
		}
	};
	public final class ViewHolder{
        public ImageView img;
        public TextView title;
        public TextView info;
        
        public CheckBox privilege;
        public TextView usersNum;
        public TextView dianliang;
        public TextView dianliangbar;
    }
	public class MyAdapter extends BaseAdapter
	{
		 
        private LayoutInflater mInflater;
         
         
        public MyAdapter(Context context){
            this.mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mData.size();
        }
 
        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }
 
        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }
 
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
             
            ViewHolder holder = null;
            if (convertView == null) {
                 
                holder=new ViewHolder();  
                 
                convertView = mInflater.inflate(R.layout.mydeviceslist, null);
                holder.img = (ImageView)convertView.findViewById(R.id.headImg);
                holder.title = (TextView)convertView.findViewById(R.id.title);
                holder.info = (TextView)convertView.findViewById(R.id.info);
                holder.dianliang= (TextView)convertView.findViewById(R.id.tv_dianliang);
                holder.privilege=(CheckBox)convertView.findViewById(R.id.devicePrivilege);
                holder.usersNum = (TextView)convertView.findViewById(R.id.usersNum);
                holder.dianliangbar=(TextView)convertView.findViewById(R.id.tv_dianliangbar);
                convertView.setTag(holder);
                 
            }else {
                 
                holder = (ViewHolder)convertView.getTag();
            }
            Device item=mData.get(position);
            
            aq.id(holder.img).image(item.getDeviceImg(),true,true);
            //holder.img.setImageURI(Uri.parse(item.getDeviceImg()));
            holder.title.setText(item.getDeviceName());
            if(btn22.isChecked())
            {
            	holder.info.setVisibility(View.GONE);
            	holder.privilege.setVisibility(View.GONE);
            	holder.usersNum.setVisibility(View.GONE);
            	holder.dianliangbar.setVisibility(View.VISIBLE);
            	holder.dianliang.setVisibility(View.VISIBLE);
            	holder.dianliang.setText(String.valueOf(item.getDianLiang())+getString(R.string.watt));
            	holder.dianliangbar.setBackgroundResource(R.color.lightblueprogress);
            	holder.dianliangbar.setWidth((int)(item.getPercentDianliang()*(holder.usersNum.getLeft()-holder.img.getWidth()-40)));
            }
            else
            {
            	holder.info.setVisibility(View.VISIBLE);
            	holder.privilege.setVisibility(View.VISIBLE);
            	holder.usersNum.setVisibility(View.VISIBLE);
            	holder.dianliangbar.setVisibility(View.GONE);
            	holder.dianliang.setVisibility(View.GONE);
	            holder.info.setText(item.getStatus());
	            
	            holder.info.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
	            		LayoutParams.WRAP_CONTENT));
	            if(item.getStatus().equals("离线"))
	            	holder.info.setBackgroundResource(R.drawable.corner_view_gray);
	            else
	            {
	            	if(item.getJidianqiFlag()==1)
	            	{
	            		holder.info.setBackgroundResource(R.drawable.corner_view_green);
	            		holder.info.setText("开启");
	            	}
	            	else
	            	{
	            		holder.info.setBackgroundResource(R.drawable.corner_view_red);
	            		holder.info.setText("关闭");
	            	}
	            }
	            if(item.getPrivilege()==1)
	            	holder.privilege.setChecked(true);
	            else
	            	holder.privilege.setChecked(false);
	            holder.usersNum.setText(String.valueOf(item.getUsersNum()));
            }
           
             
            return convertView;
        }
         
	}
	
	public class MyComparator implements Comparator<Device> {

		public int compare(Device s1, Device s2) {
			if(s1.getDianLiang() > s2.getDianLiang()){
				return -1;
			} else if(s1.getDianLiang() < s2.getDianLiang()) {
				return 1;
			}
			return 0;
		}

	}

	
}
	

