package com.dandian.iswitch.utility;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class MyUtility {

	public static HttpEntity jsonToEntity(JSONObject obj)
	{
		List<NameValuePair> params=new ArrayList<NameValuePair>();
		Iterator<String> keyIter=obj.keys();
		try {
			while(keyIter.hasNext())
			{
				String key = (String)keyIter.next();
				String value = (String) obj.get(key);
				
				NameValuePair pair1=new BasicNameValuePair(key,value);
				params.add(pair1);
			}
			return new UrlEncodedFormEntity(params, HTTP.UTF_8);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static MultipartEntity jsonToEntity(JSONObject obj,String imagepath)
	{
		MultipartEntity mpEntity = new MultipartEntity();  
		
		Iterator<String> keyIter=obj.keys();
		try {
			while(keyIter.hasNext())
			{
				String key = (String)keyIter.next();
				String value = (String) obj.get(key);
				
				StringBody par = new StringBody(value);  
                mpEntity.addPart(key, par);  
			}
			 if (!imagepath.equals("")) {  
		            FileBody file = new FileBody(new File(imagepath));  
		            mpEntity.addPart("filename", file);  
		        } 
			return mpEntity;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	public static boolean isMobileNO(String mobiles) {
		/*
		 * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
		 * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
		 * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
		 */
		String telRegex = "[1][3578]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
		if (TextUtils.isEmpty(mobiles))
		return false;
		else
		return mobiles.matches(telRegex);
		}
	public static boolean isConnect(Context context) { 
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理） 
    try { 
        ConnectivityManager connectivity = (ConnectivityManager) context 
                .getSystemService(Context.CONNECTIVITY_SERVICE); 
        if (connectivity != null) { 
            // 获取网络连接管理的对象 
            NetworkInfo info = connectivity.getActiveNetworkInfo(); 
            if (info != null&& info.isConnected()) { 
                // 判断当前网络是否已经连接 
                if (info.getState() == NetworkInfo.State.CONNECTED) { 
                    return true; 
                } 
            } 
        } 
    } catch (Exception e) { 
// TODO: handle exception 
    Log.v("error",e.toString()); 
} 
        return false; 
    } 
	public static void setListViewHeightBasedOnChildren(ListView listView) {

		  ListAdapter listAdapter = listView.getAdapter();

		  if (listAdapter == null) {
		   return;
		  }

		  int totalHeight = 0;

		  for (int i = 0; i < listAdapter.getCount(); i++) {
		   View listItem = listAdapter.getView(i, null, listView);
		   listItem.measure(0, 0);
		   totalHeight += listItem.getMeasuredHeight();
		  }

		  ViewGroup.LayoutParams params = listView.getLayoutParams();

		  params.height = totalHeight
		    + (listView.getDividerHeight() * (listAdapter.getCount() - 1));

		  //((MarginLayoutParams) params).setMargins(10, 10, 10, 10); // 可删除

		  listView.setLayoutParams(params);
		 }
}
