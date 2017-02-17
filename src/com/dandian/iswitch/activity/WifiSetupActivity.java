package com.dandian.iswitch.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dandian.iswitch.R;
import com.dandian.iswitch.utility.MyUtility;
import com.dandian.iswitch.utility.TimeUtility;
import com.dandian.iswitch.utility.WifiAdmin;

public class WifiSetupActivity extends Activity {

	private ProgressBar progressbar;
	private WifiAdmin wifi;
	final String devSSID="wifi-socket";
	private LinearLayout setupview;
	private Button refreshbtn,connbtn;
	private Spinner edwifiname;
	private EditText edwifipassword;
	private Timer timer;
	private MyTimerTask task;
	private ArrayAdapter<CharSequence> adapterWifi = null; 
	private String[] wifiarray;
	private ProgressDialog progressDialog;
	private String macAddress;
	private WifiConfiguration oldwificonfig;
	private String userName;
	private SharedPreferences pref;
	private LinearLayout refreshLayout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi_list);
		TextView backbtn=(TextView)findViewById(R.id.leftButton);
		setupview=(LinearLayout)findViewById(R.id.setupLayout);
		connbtn=(Button)findViewById(R.id.connBtn);
		TextView title=(TextView)findViewById(R.id.titleView);
		progressbar=(ProgressBar)findViewById(R.id.radarBar);
		refreshbtn=(Button)findViewById(R.id.refresh);
		refreshLayout=(LinearLayout)findViewById(R.id.refreshLayout);
		edwifiname=(Spinner)findViewById(R.id.wifiName);
		edwifipassword=(EditText)findViewById(R.id.wifiPassword);
		backbtn.setText(R.string.back);
		title.setText(R.string.scan_device);
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		backbtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				WifiSetupActivity.this.finish();
			    }
		} );
		connbtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
					connbtn.setEnabled(false);
					 new Handler().postDelayed(new Runnable(){  
					     public void run() {  
					    	 connbtn.setEnabled(true);
					     }  
					  }, 5000); 
					if(macAddress!=null && macAddress.length()>0)
					{
						Message msg = new Message();
		            	msg.what = 2;
		            	mHandler.sendMessage(msg); 
		            	return;
					}
					LoginTask asyncTask = new LoginTask();  
					String wifiname=(String) edwifiname.getSelectedItem();
					String password=edwifipassword.getText().toString();
					if(wifiname.trim().length()==0)
					{
						Toast.makeText(WifiSetupActivity.this, "请输入要连接的wifi名称!", Toast.LENGTH_LONG).show();
						return;
					}
					boolean flag=false;
					flag=wifi.testNetWork(wifi.CreateWifiInfo(wifiname, password, 3));
					if(!flag)
						flag=wifi.testNetWork(wifi.CreateWifiInfo(wifiname, password, 2));
					if(!flag)
					{
						Toast.makeText(WifiSetupActivity.this, "无法连接此Wifi，请检查密码是否正确!", Toast.LENGTH_LONG).show();
						return;
					}
					wifi.refreshWifiInfo();
					if(wifi.getSSID().equals(devSSID))
					{
						macAddress=wifi.getBSSID();
						asyncTask.execute("http://192.168.1.100/hedbasic.html?Mode=0&Ssid="+wifiname+"&ApEnable=0&Key="+password+"&Dhcp=1&DnsName=local.hed.com.cn&Auto=1&AutoHiden=0&Protocol=0&Cs=0&Domain=203.195.183.230&Port=2015&Save=Save");
					}
					else
					{
						Toast.makeText(WifiSetupActivity.this, "请先在手机设置中连接名称为"+devSSID+"的wifi!", Toast.LENGTH_LONG).show();
						return;
					}
						
					
			    }
		} );
		refreshbtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				refreshLayout.setVisibility(View.INVISIBLE);
				progressbar.setVisibility(View.VISIBLE);
				 if (timer != null){
				    if (task != null){
				    	  task.cancel();  //将原任务从队列中移除
				    }
				    task = new MyTimerTask();
				    timer.schedule(task, 1000);
				 }
			}
		});
		wifi=new WifiAdmin(this);
		
		wifiarray=new String[1];
		wifiarray[0]=wifi.getSSID();
		oldwificonfig=wifi.IsExsits(wifiarray[0]);
		adapterWifi = new ArrayAdapter<CharSequence>(this,  
			    android.R.layout.simple_spinner_dropdown_item, wifiarray);
		edwifiname.setAdapter(adapterWifi);  
		
		
		timer = new Timer();
		task=new MyTimerTask();
		timer.schedule(task, 1000);
		progressDialog=new ProgressDialog(this);
		
	}
	@Override  
	protected void onStop() 
	{  
		if(oldwificonfig!=null)
			wifi.addNetWork(oldwificonfig);
	    super.onStop();  
	    
	}  
	
	 class MyTimerTask extends TimerTask{
		  @Override
		  public void run() {
			  	int times=5;
			  	boolean flag=false;
			  	ArrayList<String> goodwifi=new ArrayList<String>();
			  	wifi.closeWifi();
				wifi.openWifi();
			  	while(times>0 && !flag)
			  	{
					wifi.startScan(); 
					List<ScanResult> wifiList=wifi.getWifiList();
					for(int i=0;i<wifiList.size();i++)
					{
						ScanResult item=wifiList.get(i);
						if(item.SSID.equals(devSSID))
						{
							flag=wifi.addNetWork(wifi.CreateWifiInfo(devSSID, "", 1));
							
						}
						if(wifi.calculateSignalLevel(item.level, 100)>80 && !item.SSID.equals(devSSID))
							goodwifi.add(item.SSID.replace("\"",""));
						
					}
					times--;
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			  	}
			  	
				wifiarray=new String[goodwifi.size()];
				for(int i=0;i<goodwifi.size();i++)
					wifiarray[i]=goodwifi.get(i);
				Message msg = new Message();
				msg.what = 0;
				msg.obj = flag;
				mHandler.sendMessage(msg);
			  	
		  }
	}
	 
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				
				boolean flag=(Boolean) msg.obj;
				if(flag)
				{
					
					setupview.setVisibility(View.VISIBLE);
					adapterWifi = new ArrayAdapter<CharSequence>(WifiSetupActivity.this,  
						    android.R.layout.simple_spinner_dropdown_item, wifiarray);
					edwifiname.setAdapter(adapterWifi);  
					Toast.makeText(WifiSetupActivity.this, "已找到设备!", Toast.LENGTH_LONG).show();
					edwifipassword.requestFocus();
					TimeUtility.popSoftKeyBoard(WifiSetupActivity.this,edwifipassword);
				}
				else
				{
					refreshLayout.setVisibility(View.VISIBLE);
					Toast.makeText(WifiSetupActivity.this, "没有找到设备!", Toast.LENGTH_LONG).show();
				}
				progressbar.setVisibility(View.INVISIBLE);
				break;
			
			case 1:
				LoginTask asyncTask = new LoginTask(); 
				asyncTask.execute("http://192.168.1.100/hedbasic.html?restart=1");
				break;
			case 2:
				if(oldwificonfig!=null)
					wifi.addNetWork(oldwificonfig);
				final EditText et=new EditText(WifiSetupActivity.this);
				new AlertDialog.Builder(WifiSetupActivity.this).setTitle("请为当前设备起一个名称").setView(et).setCancelable(false)
				.setPositiveButton("确定", new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
						String deviceName=et.getText().toString().trim();
						if(et.length()==0)
						{
							Toast.makeText(WifiSetupActivity.this, "设备名称不能为空!", Toast.LENGTH_SHORT).show();
							return;
						}
						String userName=pref.getString("userName","");
						LoginTask asyncTask1 = new LoginTask(); 
						try {
							asyncTask1.execute("http://school.eduyun.la/wulian/functions.php?action=newDevice&macAddress="+macAddress+"&deviceName="+URLEncoder.encode(deviceName, "UTF-8")+"&userName="+userName);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
					}
					
				}).setNegativeButton("取消", null).show();
				TimeUtility.popSoftKeyBoard(WifiSetupActivity.this,et);
				
				break;
			case 3:
				 if(msg.obj.equals("设备注册成功"))
	             {
	                Toast.makeText(WifiSetupActivity.this, (String)msg.obj, Toast.LENGTH_LONG).show();
	                WifiSetupActivity.this.finish();
	             }
				 else
					Toast.makeText(WifiSetupActivity.this, (String)msg.obj, Toast.LENGTH_LONG).show();
				break;
			}
			
		}
	};
	class LoginTask extends AsyncTask<String, Object, String> {  
		
		 
        @Override  
        protected void onPreExecute() {  
            progressDialog.setMessage("Loading...");  
            progressDialog.show();  
        }  
  
        @Override  
        protected String doInBackground(String... par) {  
            HttpGet get = new HttpGet(par[0]);  
           
            DefaultHttpClient client=new DefaultHttpClient();  
            AuthScope mAuthScope = new AuthScope("192.168.1.100", AuthScope.ANY_PORT);
            client.getCredentialsProvider().setCredentials(mAuthScope,
                    new UsernamePasswordCredentials("admin", "000000"));
            
            HttpResponse res=null;
      
          
            try {  
                res = client.execute(get);  
            } catch (Exception e) {  
                return "网络连接错误,请检查网络是否打开！";  
            }  
            int responseCode = res.getStatusLine().getStatusCode();  
            if (responseCode != 200)  
                return "服务器正忙！ 返回代码为：" + responseCode;  
            
            String result = "";
			try {
				HttpEntity httpEntity = res.getEntity();
	            InputStream inputStream = httpEntity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(
	                    inputStream));
	            
	            String line = "";
	            while (null != (line = reader.readLine()))
	            {
	                result += line;

	            }
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            
            if(par[0].contains("Save"))
            {
            	Message msg = new Message();
            	msg.what = 1;
            	mHandler.sendMessage(msg);   
            }
            else if(par[0].contains("restart"))
            {
            	Message msg = new Message();
            	msg.what = 2;
            	mHandler.sendMessage(msg);   
            }
            else if(par[0].contains("newDevice"))
            {
            	Message msg = new Message();
            	msg.what = 3;
            	msg.obj=result;
            	mHandler.sendMessage(msg);   
            }
            return null;  
        }  
  
        @Override  
        protected void onPostExecute(String result) {  
            progressDialog.cancel();  
            if (result != null) {  
                alertDialog(result);  
            }  
            
        }  
    }  

	public void alertDialog(String result) {
		new AlertDialog.Builder(this)
		 .setTitle("标题") 
		 .setMessage(result)
		 	.setPositiveButton("确定", null)
		 	.show();
	}
}
