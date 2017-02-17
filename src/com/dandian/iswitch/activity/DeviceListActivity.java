package com.dandian.iswitch.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.dandian.iswitch.R;
import com.dandian.iswitch.fragment.MyDevicesFragment;

public class DeviceListActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if(!getIntent().getBooleanExtra("ÒÑµÇÂ¼", false))
		{
			Intent aintent = new Intent(this, LoginActivity.class);
        	startActivity(aintent);
        	finish();
			return;
		}
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new MyDevicesFragment()).commit();
		}
	}


}
