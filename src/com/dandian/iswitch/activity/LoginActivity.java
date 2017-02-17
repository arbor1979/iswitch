package com.dandian.iswitch.activity;

import java.util.Timer;

import org.json.JSONException;
import org.json.JSONObject;

import com.dandian.iswitch.R;
import com.dandian.iswitch.utility.MyRunnable;
import com.dandian.iswitch.utility.MyUtility;
import com.dandian.iswitch.utility.ThreadPoolUtils;
import com.dandian.iswitch.utility.TimeUtility;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system.
	
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */

	// Values for email and password at the time of the login attempt.
	private String mMobile;
	private String mPassword;

	// UI references.
	private EditText mMobileView;
	private EditText mPasswordView;
	private SharedPreferences pref;
	ProgressDialog mypDialog;
	private CheckBox ck_remberpass;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		// Set up the login form.
		
		mMobileView = (EditText) findViewById(R.id.mobile);
		mPasswordView = (EditText) findViewById(R.id.password);
		ck_remberpass=(CheckBox)findViewById(R.id.ck_remberpass);
		String userName=pref.getString("userName","");
		if(pref.getBoolean("remberpass", false))
		{
			ck_remberpass.setChecked(true);
			mPasswordView.setText(pref.getString("password",""));
		}
		mMobileView.setText(userName);
		
		if(userName.length()>0)
		{
			mPasswordView.requestFocus();
			//TimeUtility.popSoftKeyBoard(this,mPasswordView);
			 
		}
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						
						attemptLogin();
					}
				});
		findViewById(R.id.regist_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent intent=new Intent(LoginActivity.this,RegistActivity.class);
						startActivityForResult(intent, 0);
					}
				});
		
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
		case RESULT_OK:
			if(requestCode==0)
			{
				Bundle b=data.getExtras();  
				String str=b.getString("mobile");
				mMobileView.setText(str);
				mPasswordView.requestFocus();
			}
			break;
		default:
		          break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		
		// Reset errors.
		mMobileView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mMobile = mMobileView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mMobile)) {
			mMobileView.setError(getString(R.string.error_field_required));
			focusView = mMobileView;
			cancel = true;
		} else if (!MyUtility.isMobileNO(mMobile)) {
			mMobileView.setError(getString(R.string.error_invalid_email));
			focusView = mMobileView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			showProgress(true);
			String url="http://school.eduyun.la/wulian/functions.php?action=login";
			JSONObject json=new JSONObject();
		 	try {
				json.put("username",mMobileView.getEditableText().toString());
				json.put("password", mPasswordView.getEditableText().toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 	ThreadPoolUtils.execute(new MyRunnable(url,"post",MyUtility.jsonToEntity(json),myHandler));
			findViewById(R.id.sign_in_button).setEnabled(false);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		
		if(show)
		{
		if(mypDialog==null)
			mypDialog=new ProgressDialog(this);
        //实例化
        mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //设置ProgressDialog 标题
        mypDialog.setMessage(getResources().getString(R.string.login_progress_signing_in));
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

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	
	 @SuppressLint("HandlerLeak")
	 private Handler myHandler = new Handler() {

			public void handleMessage(Message msg) {
				switch(msg.what)
				{
		           
		            case -1:
		            	showProgress(false);
		                findViewById(R.id.sign_in_button).setEnabled(true);
		                Toast.makeText(LoginActivity.this, "失败:"+msg.obj, Toast.LENGTH_LONG).show();
		                break;
		            case 1:
		            	showProgress(false);
		            	findViewById(R.id.sign_in_button).setEnabled(true);
		                if(msg.obj.equals("登录成功"))
		                {
		                	
		                	SharedPreferences.Editor edit = pref.edit();
		            		edit.putString("userName", mMobileView.getEditableText().toString());
		            		edit.putString("password", mPasswordView.getEditableText().toString());
		            		edit.putBoolean("remberpass", ck_remberpass.isChecked());
		            		edit.commit();
		            			
		                	Intent aintent = new Intent(LoginActivity.this, DeviceListActivity.class);
		                	aintent.putExtra("已登录", true);
		                	startActivity(aintent);
		                	LoginActivity.this.finish();
		                }
		                else
		                {
		                	Toast.makeText(LoginActivity.this, (String)msg.obj, Toast.LENGTH_LONG).show();
		                }
		                break;
	
		            default:
		                System.out.println("nothing to do");
		                break;
				}
			}
	 };
		
		 

}
