package com.dandian.iswitch.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;


@SuppressLint("DefaultLocale")
public class MyRunnable implements Runnable{
	 String url;
	 String method;
	 HttpEntity obj;
	 Handler myHandler;
	 int messageId;
	 public MyRunnable(String url,Handler myHandler){
		 this.url=url;
		 this.method="get";
		 this.myHandler=myHandler;
		 messageId=1;
	 }
	 public MyRunnable(String url,Handler myHandler,int messageId){
		 this.url=url;
		 this.method="get";
		 this.myHandler=myHandler;
		 this.messageId=messageId;
	 }
	 public MyRunnable(String url,String method,HttpEntity obj,Handler myHandler){
		 this.url=url;
		 this.method=method;
		 this.obj=obj;
		 this.myHandler=myHandler;
		 this.messageId=1;
	 }
	 public MyRunnable(String url,String method,HttpEntity obj,Handler myHandler,int messageId){
		 this.url=url;
		 this.method=method;
		 this.obj=obj;
		 this.myHandler=myHandler;
		 this.messageId=messageId;
	 }
       @Override
       public void run() {
    	   InputStream inputStream=null;
    	   BufferedReader reader=null;
    	   HttpPost httpPost=null;
    	   HttpGet httpGet=null;
    	   HttpEntity httpEntity=null;
    	   try
    	   {
    		   HttpResponse httpResponse;
	           if(method.toLowerCase().equals("post"))
	           {
	        	   httpPost  = new HttpPost(url);
	   			   httpPost.setEntity(obj);
	               httpResponse = HttpClientHelper.getHttpClient().execute(httpPost);
	        	  
	           }
	           else
	           {
	        	   httpGet  = new HttpGet(url);
	        	   httpResponse = HttpClientHelper.getHttpClient().execute(httpGet);
	        	   
	           }
			
        	   httpEntity = httpResponse.getEntity();
               inputStream = httpEntity.getContent();
               reader = new BufferedReader(new InputStreamReader(
                       inputStream));
               String result = "";
               String line = "";
               while (null != (line = reader.readLine()))
               {
                   result += line;

               }
               Message msg = myHandler.obtainMessage();
                msg.what = messageId;
                msg.obj = result;
                myHandler.sendMessage(msg);
               System.out.println(result);
              
                   
               
           } catch (Exception ex){
        	   if(httpGet!=null)
        		   httpGet.abort();
        	   if(httpPost!=null)
        		   httpPost.abort();
               ex.printStackTrace();
               Message msg = myHandler.obtainMessage();
               msg.what = -1;
               msg.obj = ex.getMessage();
               myHandler.sendMessage(msg);
           }finally {
        	   if (reader != null){  
                   try  
                   {  
                	   reader.close ();  
                   }  
                   catch (IOException e)  
                   {  
                       e.printStackTrace ();  
                   }  
               }  
        	   if(inputStream!=null){
        		   try {
        			   inputStream.close();
        		   }
        		   catch (IOException e) {
        		   		e.printStackTrace();
        		   	}
        	   }
        	   if(httpEntity!=null)
        	   {
        		   try {
        			   httpEntity.consumeContent();
        		   }
        		   catch (IOException e) {
        		   		e.printStackTrace();
        		   	}
        	   }
        	   
        	}
       }
   }
