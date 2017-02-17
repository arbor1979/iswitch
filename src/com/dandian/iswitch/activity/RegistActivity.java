package com.dandian.iswitch.activity;


import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

import com.dandian.iswitch.R;
import com.dandian.iswitch.utility.MyRunnable;
import com.dandian.iswitch.utility.MyUtility;
import com.dandian.iswitch.utility.ThreadPoolUtils;

public class RegistActivity extends Activity{
	
	private static String APPKEY = "aba0b957701a";
	private static String APPSECRET = "365aafe52e8e295bacad6f55504c9184";
	
	private EditText mMobileView,mPasswordView,mConfirmPasswordView,mRealNameView;
	private Button mbtnRegist;
	private MyHandler myHandler;
	private boolean verifySuc=false;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_regist);
		myHandler = new MyHandler();
		mMobileView = (EditText) findViewById(R.id.edMobile);
		mPasswordView = (EditText) findViewById(R.id.edPassword);
		mConfirmPasswordView = (EditText) findViewById(R.id.edConfirmPassword);
		mRealNameView = (EditText) findViewById(R.id.edRealName);
		TextView backbtn=(TextView)findViewById(R.id.leftButton);
		TextView title=(TextView)findViewById(R.id.titleView);
		backbtn.setText(R.string.back);
		title.setText(R.string.newUserRegiest);
		
		
		SMSSDK.initSDK(this,APPKEY,APPSECRET);
		EventHandler eh=new EventHandler(){

			@Override
			public void afterEvent(int event, int result, Object data) {

			   if (result == SMSSDK.RESULT_COMPLETE) 
			   {
				//回调完成
				   if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                //提交验证码成功
				   }else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
			    //获取验证码成功
				   }else if (event ==SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){
                //返回支持发送验证码的国家列表
				   } 
			   }else{                                                                 
				   ((Throwable)data).printStackTrace(); 
				   try {
					     Throwable throwable = (Throwable) data;
					     throwable.printStackTrace();
					     JSONObject object = new JSONObject(throwable.getMessage());
					     String des = object.optString("detail");//错误描述
					     int status = object.optInt("status");//错误代码
					     if (status > 0 && !TextUtils.isEmpty(des)) {
					    Toast.makeText(RegistActivity.this, des, Toast.LENGTH_SHORT).show();
					    return;
					     }
					} catch (Exception e) {
					     //do something                            
					}
			   }
			}
		};
		SMSSDK.registerEventHandler(eh); //注册短信回调

		backbtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				RegistActivity.this.finish();  
			    }
		} );
		mbtnRegist=(Button)findViewById(R.id.btnRegist);
		mbtnRegist.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
							
							if(!verifySuc)
							{
								openMobileVerify();
								return;
							}
							
							String confirmpass=mConfirmPasswordView.getEditableText().toString();
							String pass=mPasswordView.getEditableText().toString();
							String mobileNumber=mMobileView.getEditableText().toString();
							if(mobileNumber==null || !MyUtility.isMobileNO(mobileNumber))
							{
								mMobileView.setError("手机号码必须为11位数字");
								return;
							}
							if(pass==null || pass.length()<4)
							{
								mPasswordView.setError("密码长度不能小于4位");
								return;
							}
							if(!confirmpass.equals(pass))
							{
								mConfirmPasswordView.setError("两次输入密码不一致");
								return;
							}
						 	
						 	String url="http://school.eduyun.la/wulian/functions.php?action=reguser";
						 	JSONObject json=new JSONObject();
						 	try {
								json.put("username",mMobileView.getEditableText().toString());
								json.put("password", mPasswordView.getEditableText().toString());
							 	json.put("realname", mRealNameView.getEditableText().toString());
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					        ThreadPoolUtils.execute(new MyRunnable(url,"post",MyUtility.jsonToEntity(json),myHandler));
					}
				});
		
		openMobileVerify();
		
	}
	private void openMobileVerify()
	{
		//打开注册页面
		RegisterPage registerPage = new RegisterPage();
		registerPage.setRegisterCallback(new EventHandler() {
		public void afterEvent(int event, int result, Object data) {
		// 解析注册结果
		if (result == SMSSDK.RESULT_COMPLETE) {
		@SuppressWarnings("unchecked")
			HashMap<String,Object> phoneMap = (HashMap<String, Object>) data;
			//String country = (String) phoneMap.get("country");
			String phone = (String) phoneMap.get("phone"); 
			mMobileView.setText(phone);
			mMobileView.setEnabled(false);
			verifySuc=true;
		}
		}
		});
		registerPage.show(RegistActivity.this);
	}
	
	
	
	protected void onDestroy() {
		super.onDestroy();
		SMSSDK.unregisterAllEventHandler();
	}

	
	 
	 @SuppressLint("HandlerLeak")
	private class MyHandler extends Handler{
	        @Override
	        public void dispatchMessage(Message msg) {
	            switch(msg.what){
	           
	            case -1:
	                Toast.makeText(RegistActivity.this, "失败:"+msg.obj, Toast.LENGTH_LONG).show();
	                break;
	            case 1:
	                
	                if(msg.obj.equals("注册成功"))
	                {
	                	Toast.makeText(RegistActivity.this, (String)msg.obj, Toast.LENGTH_LONG).show();
	                	Intent aintent = new Intent(RegistActivity.this, LoginActivity.class);
	                	aintent.putExtra("mobile", mMobileView.getEditableText().toString());
	                	setResult(RESULT_OK,aintent);
	                	RegistActivity.this.finish();
	                }
	                else
	                {
	                	verifySuc=false;
	                	mMobileView.setError((String)msg.obj);
	                	mMobileView.setEnabled(true);
	                	mMobileView.requestFocus();
	                }
	                break;

	            default:
	                System.out.println("nothing to do");
	                break;
	            }
	        }
	    }
	 

	    


	

}
