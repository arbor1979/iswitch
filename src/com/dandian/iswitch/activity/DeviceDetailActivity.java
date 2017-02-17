package com.dandian.iswitch.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.entity.mime.MultipartEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.dandian.iswitch.R;
import com.dandian.iswitch.entity.Device;
import com.dandian.iswitch.utility.DialogUtility;
import com.dandian.iswitch.utility.FileUtility;
import com.dandian.iswitch.utility.ImageUtility;
import com.dandian.iswitch.utility.MyRunnable;
import com.dandian.iswitch.utility.MyUtility;
import com.dandian.iswitch.utility.ThreadPoolUtils;
import com.dandian.iswitch.utility.TimeUtility;



public class DeviceDetailActivity extends Activity {

	public static final int REQUEST_CODE_TAKE_PICTURE = 2;// //设置图片操作的标志
	public static final int REQUEST_CODE_TAKE_CAMERA = 1;// //设置拍照操作的标志
	private Switch swiOpenClose;
	private ImageView imgDevice;
	private Device device;
	private AQuery aq;
	private Button btnChangeIcon,grantBtn,btnCenter;
	private ImageButton btnPower,btnReturn,btnTop,btnLeft,btnRight,btnDown;
	private String picturePath;
	private ProgressDialog mypDialog;
	private ListView userList;
	private List<String[]> mData;
	private MyAdapter adapter;
	private OnClickListener commBtnClick;
	private OnLongClickListener commBtnLongClick;
	private LinearLayout yaokonglayout;
	private TextView tvDeviceName,tvManager;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.devicedetaillayout);
		TextView backbtn=(TextView)findViewById(R.id.leftButton);
		TextView title=(TextView)findViewById(R.id.titleView);
		backbtn.setText(R.string.back);
		title.setText(R.string.devicedetail);
		backbtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			    }
		} );
		
		tvDeviceName=(TextView)findViewById(R.id.tvDeviceName);
		tvManager=(TextView)findViewById(R.id.tvManager);
		swiOpenClose=(Switch)findViewById(R.id.swiOpenClose);
		imgDevice=(ImageView)findViewById(R.id.imgDevice);
		btnChangeIcon=(Button)findViewById(R.id.btnChangeIcon);
		grantBtn=(Button)findViewById(R.id.grantBtn);
		userList=(ListView)findViewById(R.id.userList);
		btnCenter=(Button)findViewById(R.id.btnCenter);
		btnPower=(ImageButton)findViewById(R.id.btnPower);
		btnReturn=(ImageButton)findViewById(R.id.btnReturn);
		btnTop=(ImageButton)findViewById(R.id.btnTop);
		btnLeft=(ImageButton)findViewById(R.id.btnLeft);
		btnRight=(ImageButton)findViewById(R.id.btnRight);
		btnDown=(ImageButton)findViewById(R.id.btnDown);
		yaokonglayout=(LinearLayout)findViewById(R.id.yaokonglayout);
		ImageButton quxianbutton=(ImageButton)findViewById(R.id.quxianbutton);
		device=(Device) getIntent().getSerializableExtra("device");
		aq = new AQuery(this);
		if(device.getPrivilege()!=1)
			btnChangeIcon.setVisibility(View.GONE);
		tvDeviceName.setText(device.getDeviceName());
		tvManager.setText(tvManager.getText()+device.getManager());
		aq.id(imgDevice).image(device.getDeviceImg());
		if(device.getPrivilege()!=1)
		{
			userList.setVisibility(View.GONE);
			grantBtn.setVisibility(View.GONE);
		}
		else
		{
			mData= new ArrayList<String[]>();
			adapter = new MyAdapter(this);
	        userList.setAdapter(adapter);
			getUserList();
		}
		
		initDeviceStatus();
		btnChangeIcon.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showGetPictureDiaLog();
				
			}
			
		});
		imgDevice.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				DialogUtility.showImageDialog(DeviceDetailActivity.this,device.getDeviceImg());
				
			}
			
		});
		grantBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				final EditText et=new EditText(DeviceDetailActivity.this);
				et.setInputType(InputType.TYPE_CLASS_PHONE);
				new AlertDialog.Builder(DeviceDetailActivity.this).setTitle(R.string.inputusername).setView(et)
				.setPositiveButton(R.string.Commons_Ok, new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String newphone=et.getText().toString().trim();
						if(et.length()!=11)
						{
							Toast.makeText(DeviceDetailActivity.this,R.string.mobilenumberformaterror, Toast.LENGTH_SHORT).show();
						}
						else
							grantToUser(newphone);
					}
					
				}).setNegativeButton(R.string.alert_dialog_cancel, null).show();
				TimeUtility.popSoftKeyBoard(DeviceDetailActivity.this,et);
			}
			
		});
		swiOpenClose.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
			  
            @Override  
            public void onCheckedChanged(CompoundButton buttonView,  
                    boolean isChecked) {  
            	if(!buttonView.isPressed())return; 
                if (isChecked) {  
                	sendCommand("开启");
                } else {  
                	sendCommand("关闭");
                }  
            }  
        });  
		commBtnClick=new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(device.getStatus().equals("离线"))
				{
					Toast.makeText(DeviceDetailActivity.this, R.string.devicecannotuse, Toast.LENGTH_SHORT).show();
					return;
				}
				sendCommand((String)v.getTag());
			}
			
		};
		commBtnLongClick=new OnLongClickListener(){

			@Override
			public boolean onLongClick(View v) {
				if(device.getStatus().equals("离线"))
				{
					Toast.makeText(DeviceDetailActivity.this, R.string.devicecannotuse, Toast.LENGTH_SHORT).show();
					return true;
				}
				studyCode((String)v.getTag());
				return true;
			}
			
		};
		quxianbutton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(DeviceDetailActivity.this,ColumnChartActivity.class);
				intent.putExtra("macAddress", device.getMacAddress());
				startActivity(intent);
			}
			
		});
		btnCenter.setOnClickListener(commBtnClick);
		btnPower.setOnClickListener(commBtnClick);
		btnReturn.setOnClickListener(commBtnClick);
		btnTop.setOnClickListener(commBtnClick);
		btnLeft.setOnClickListener(commBtnClick);
		btnRight.setOnClickListener(commBtnClick);
		btnDown.setOnClickListener(commBtnClick);
		
		btnCenter.setOnLongClickListener(commBtnLongClick);
		btnPower.setOnLongClickListener(commBtnLongClick);
		btnReturn.setOnLongClickListener(commBtnLongClick);
		btnTop.setOnLongClickListener(commBtnLongClick);
		btnLeft.setOnLongClickListener(commBtnLongClick);
		btnRight.setOnLongClickListener(commBtnLongClick);
		btnDown.setOnLongClickListener(commBtnLongClick);
	}
	private void initDeviceStatus()
	{
		
		if(device.getStatus().equals("离线"))
		{
			swiOpenClose.setText(device.getStatus());
			swiOpenClose.setEnabled(false);
			yaokonglayout.setVisibility(View.GONE);
		}
		else
		{
			swiOpenClose.setEnabled(true);
			if(device.getJidianqiFlag()==1)
				swiOpenClose.setChecked(true);
			else
				swiOpenClose.setChecked(false);
			yaokonglayout.setVisibility(View.VISIBLE);
		}
		
	}

	@Override
	public  void onResume()
	{
		super.onResume();
		if(MyUtility.isConnect(this))
			getDeviceStatus();
		/*
		if(timer==null)
			timer = new Timer(true);
		task = new MyTimerTask();
		timer.schedule(task,0, 20000);
		*/
	}
	private void getDeviceStatus()
	{
		String url="http://school.eduyun.la/wulian/functions.php?action=deviceState";
	 	JSONObject json=new JSONObject();
	 	try {
			json.put("macAddress",device.getMacAddress());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        ThreadPoolUtils.execute(new MyRunnable(url,"post",MyUtility.jsonToEntity(json),mHandler,3));
		
	}
	private void sendCommand(String comStr)
	{
		String url="http://school.eduyun.la/wulian/functions.php?action=sendCommand";
	 	JSONObject json=new JSONObject();
		try {
			json.put("macAddress",device.getMacAddress());
			json.put("commandName",comStr);
			json.put("actionName","");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        ThreadPoolUtils.execute(new MyRunnable(url,"post",MyUtility.jsonToEntity(json),mHandler,5));
	
	}
	private void grantToUser(String newphone)
	{
		String url="http://school.eduyun.la/wulian/functions.php?action=grantUser";
	 	JSONObject json=new JSONObject();
		try {
			json.put("macAddress",device.getMacAddress());
			json.put("userName",newphone);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
        ThreadPoolUtils.execute(new MyRunnable(url,"post",MyUtility.jsonToEntity(json),mHandler,4));
	
	}
	private void getUserList()
	{
		
        
		String url="http://school.eduyun.la/wulian/functions.php?action=getUserList";
	 	JSONObject json=new JSONObject();
		try {
			json.put("macAddress",device.getMacAddress());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
        ThreadPoolUtils.execute(new MyRunnable(url,"post",MyUtility.jsonToEntity(json),mHandler,2));
	}
	private void showGetPictureDiaLog() {
		View view = getLayoutInflater()
				.inflate(R.layout.view_get_picture, null);
		Button cancel = (Button) view.findViewById(R.id.cancel);
		TextView byCamera = (TextView) view.findViewById(R.id.tv_by_camera);
		TextView byLocation = (TextView) view.findViewById(R.id.tv_by_location);

		final AlertDialog ad = new AlertDialog.Builder(this).setView(view)
				.create();

		Window window = ad.getWindow();
		window.setGravity(Gravity.BOTTOM);// 在底部弹出
		window.setWindowAnimations(R.style.CustomDialog);
		ad.show();
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ad.dismiss();
			}
		});
		byCamera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getPictureByCamera();
				ad.dismiss();
			}
		});
		byLocation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getPictureFromLocation();
				ad.dismiss();
			}
		});
	}
	private synchronized void getPictureByCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 调用android自带的照相机
		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
			Toast.makeText(this, R.string.Commons_SDCardErrorTitle, Toast.LENGTH_SHORT).show();
			return;
		}
		picturePath = FileUtility.getRandomSDFileName("jpg");
		
		File mCurrentPhotoFile = new File(picturePath);

		Uri uri = Uri.fromFile(mCurrentPhotoFile);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		startActivityForResult(intent, REQUEST_CODE_TAKE_CAMERA);
	}
	
	public void getPictureFromLocation() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡
			/*
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
			*/
			Intent intent; 
			intent = new Intent(Intent.ACTION_PICK, 
			                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); 
			startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
		} else {
			Toast.makeText(this, R.string.Commons_SDCardErrorTitle, Toast.LENGTH_SHORT).show();
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_TAKE_CAMERA: // 拍照返回
			if (resultCode == RESULT_OK) {
				if(picturePath!=null)
					rotateAndCutImage(new File(picturePath));
				else
					Toast.makeText(this, R.string.getcamerafailed, Toast.LENGTH_SHORT).show();
					
			}
			break;
		case REQUEST_CODE_TAKE_PICTURE:
			if (data != null) {
				
				
				Uri uri = data.getData();
				String[] pojo  = { MediaStore.Images.Media.DATA };
				CursorLoader cursorLoader = new CursorLoader(this, uri, pojo, null,null, null); 
				Cursor cursor = cursorLoader.loadInBackground();
				if(cursor!=null)
				{
					cursor.moveToFirst(); 
					picturePath = cursor.getString(cursor.getColumnIndex(pojo[0]));
				}
				else
				{
					if(uri.toString().startsWith("file://"))
					{
						picturePath=uri.toString().replace("file://", "");
					}
					else
					{
						Toast.makeText(this, R.string.getalbumfailed, Toast.LENGTH_SHORT).show();
						return;
					}
				}
			     
				
				String tempPath =FileUtility.getRandomSDFileName("jpg");
				if(FileUtility.copyFile(picturePath,tempPath))
					rotateAndCutImage(new File(tempPath));
				else
					Toast.makeText(this, R.string.failedcopytosdcard, Toast.LENGTH_SHORT).show();
			}
			break;
		case 3:
			if (resultCode == 200 && data != null) {
				
				String picPath = data.getStringExtra("picPath");
				SubmitUploadFile(picPath);
			}
		default:
			break;
		}
	}
	private void rotateAndCutImage(final File file) {
		if(!file.exists()) return;
		
		ImageUtility.rotatingImageIfNeed(file.getAbsolutePath());
		Intent intent=new Intent(this,CutImageActivity.class);
		intent.putExtra("picPath", file.getAbsolutePath());
		startActivityForResult(intent,3);
		
	}
	public void SubmitUploadFile(String picPath){
		String url="http://school.eduyun.la/wulian/upload.php?action=deviceImg";
	 	JSONObject json=new JSONObject();
		try {
			json.put("macAddress",device.getMacAddress());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MultipartEntity mpEntity=MyUtility.jsonToEntity(json,picPath);
		showProgress(true);
        ThreadPoolUtils.execute(new MyRunnable(url,"post",mpEntity,mHandler,1));

	}
	private void getSwitchStatusOpenOrClose(String macAddress)
	{
		
		String url="http://school.eduyun.la/wulian/functions.php?action=sendCommand";
	 	JSONObject json=new JSONObject();
	 	try {
			json.put("commandName","状态");
			json.put("macAddress",macAddress);
			json.put("actionName","");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        ThreadPoolUtils.execute(new MyRunnable(url,"post",MyUtility.jsonToEntity(json),mHandler,5));
			
	}
	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			case -1:
				showProgress(false);
				Toast.makeText(DeviceDetailActivity.this, "失败:"+msg.obj, Toast.LENGTH_LONG).show();
				break;
			case 1:
				showProgress(false);
				String returnStr=(String)msg.obj;
				String[] returnArray=returnStr.split(";");
				if(returnArray[0].equals("成功"))
				{
					device.setDeviceImg(returnArray[1]);
					aq.id(imgDevice).image(device.getDeviceImg());
				}
				else
					Toast.makeText(DeviceDetailActivity.this, "失败:"+returnStr, Toast.LENGTH_LONG).show();
				
				break;
			case 2:
				JSONArray jo = null;
				try 
				{
					jo = new JSONArray((String)msg.obj);
					if(jo!=null)
					{
						mData.clear();
						for(int i=0;i<jo.length();i++)
						{
							JSONObject obj=jo.getJSONObject(i);
							String[] strArray={obj.optString("userName"),obj.optString("realName"),obj.optString("headImg")};
							mData.add(strArray);
						}
						adapter.notifyDataSetChanged();
						MyUtility.setListViewHeightBasedOnChildren(userList);	
					}
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(DeviceDetailActivity.this, (String)msg.obj, Toast.LENGTH_LONG).show();
				}
				break;
			case 3:
				
				JSONObject job;
				try 
				{
					job = new JSONObject((String)msg.obj);
					if(job!=null)
					{
						device.setStatus(job.optString("status"));
						device.setJidianqiFlag(job.optInt("jidianqiFlag"));
						initDeviceStatus();
						//getSwitchStatusOpenOrClose(device.getMacAddress());
						
					}
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(DeviceDetailActivity.this, (String)msg.obj, Toast.LENGTH_LONG).show();
				}
				break;
			case 4:
					Toast.makeText(DeviceDetailActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
					if(msg.obj.equals("成功"))
					{
						getUserList();
					}
				break;
			case 5:
					String result=(String)msg.obj;
					final String resultArray[]=result.split(":");
					if(resultArray.length>1)
					{
						if(resultArray[1].equals("此按键还未学码"))
							studyCode(resultArray[0]);
						else if(resultArray[0].equals("红外学码"))
							Toast.makeText(DeviceDetailActivity.this, resultArray[1], Toast.LENGTH_SHORT).show();
						else if(resultArray[0].equals("开启") || resultArray[0].equals("关闭"))
						{	
							if(resultArray[1].equals("成功"))
							{
								if(resultArray[0].equals("开启"))
									swiOpenClose.setChecked(true);
								else
									swiOpenClose.setChecked(false);
							}
							else
							{
								Toast.makeText(DeviceDetailActivity.this, resultArray[1], Toast.LENGTH_SHORT).show();
								if(resultArray[0].equals("开启"))
									swiOpenClose.setChecked(false);
								else
									swiOpenClose.setChecked(true);
							}
						}
						else if(resultArray[0].equals("状态"))
						{
							if(resultArray[1].equals("吸合"))
								swiOpenClose.setChecked(true);
							else
								swiOpenClose.setChecked(false);
						}
					}
					else
						Toast.makeText(DeviceDetailActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
				break;
			
			}
			
		}
	};
	private void studyCode(final String key)
	{
		AlertDialog.Builder builder=new AlertDialog.Builder(DeviceDetailActivity.this);
		builder.setMessage("是否现在学码？将遥控器对准智能开关，一直按遥控器上被学习的按键");
		builder.setPositiveButton(R.string.Commons_Ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String url="http://school.eduyun.la/wulian/functions.php?action=sendCommand";
        	 	JSONObject json=new JSONObject();
        		try {
        			json.put("macAddress",device.getMacAddress());
        			json.put("commandName","红外学码");
        			json.put("actionName",key);
        		} catch (JSONException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
                ThreadPoolUtils.execute(new MyRunnable(url,"post",MyUtility.jsonToEntity(json),mHandler,5));               
            
			}
		});
		builder.setNegativeButton(R.string.alert_dialog_cancel, null);
		builder.create();
		builder.show();
	}
	private void showProgress(final boolean show) {
		
		if(show)
		{
		if(mypDialog==null)
			mypDialog=new ProgressDialog(this);
        //实例化
        mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //设置ProgressDialog 标题
        mypDialog.setMessage(getResources().getString(R.string.uploading_progress));
        //设置ProgressDialog 提示信息
        //设置ProgressDialog 的一个Button
        mypDialog.setIndeterminate(false);
        //设置ProgressDialog 的进度条是否不明确
        mypDialog.setCancelable(false);
        //设置ProgressDialog 是否可以按退回按键取消
        mypDialog.show();
		}
		else
		{
			if(mypDialog!=null)
				mypDialog.cancel();
		}
	}
	public final class ViewHolder{
        public ImageView img;
        public TextView title;
        public TextView info;
        public Button delete;
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
                 
                convertView = mInflater.inflate(R.layout.userlist, null);
                holder.img = (ImageView)convertView.findViewById(R.id.headImg);
                holder.title = (TextView)convertView.findViewById(R.id.title);
                holder.info = (TextView)convertView.findViewById(R.id.info);
                holder.delete = (Button)convertView.findViewById(R.id.btnDelete);
                convertView.setTag(holder);
                 
            }else {
                 
                holder = (ViewHolder)convertView.getTag();
            }
            final String[] item=mData.get(position);
            if(item[2].length()>0 && !item[2].equals("null"))
            	aq.id(holder.img).image(item[2],true,true);
            //holder.img.setImageURI(Uri.parse(item.getDeviceImg()));
            holder.title.setText(item[0]);
            holder.info.setText(item[1]);
            holder.delete.setOnClickListener(new View.OnClickListener() {
                
                @Override
                public void onClick(View v) {
                	AlertDialog.Builder builder=new AlertDialog.Builder(DeviceDetailActivity.this);
            		builder.setTitle("是否解除此用户的使用授权？");

            		builder.setPositiveButton(R.string.Commons_Ok, new DialogInterface.OnClickListener() {
            			
            			@Override
            			public void onClick(DialogInterface dialog, int which) {
            				// TODO Auto-generated method stub
            				String url="http://school.eduyun.la/wulian/functions.php?action=removeUser";
                    	 	JSONObject json=new JSONObject();
                    		try {
                    			json.put("macAddress",device.getMacAddress());
                    			json.put("userName",item[0]);
                    		} catch (JSONException e) {
                    			// TODO Auto-generated catch block
                    			e.printStackTrace();
                    		}
                            ThreadPoolUtils.execute(new MyRunnable(url,"post",MyUtility.jsonToEntity(json),mHandler,4));               
                        
            			}
            		});
            		builder.setNegativeButton(R.string.alert_dialog_cancel, null);
            		builder.create();
            		builder.show();
                	}
            });
            return convertView;
        }
         
	}
}
