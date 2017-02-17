package com.dandian.iswitch.utility;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;


/**
 * �����࣬Ϊ����Ӧ�ó����ṩΨһ��һ��HttpClient����
 * ���������һЩ��ʼ���������������ԣ���Щ���Կ��Ա�HttpGet��HttpPost�����Ը���
 * @author zet
 *
 */
public class HttpClientHelper {
    private static HttpClient httpClient;
    
    private HttpClientHelper(){
        
    }
    
    public static synchronized HttpClient getHttpClient(){
        if(null == httpClient){
            //��ʼ������
            HttpParams params = new BasicHttpParams();
            
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            HttpProtocolParams.setUseExpectContinue(params, true);
            
            //�������ӹ������ĳ�ʱ
            ConnManagerParams.setTimeout(params, 1000);
            
            // ����ÿ��·�����������  
            ConnPerRouteBean connPerRoute = new ConnPerRouteBean(400);  
            ConnManagerParams.setMaxConnectionsPerRoute(params,connPerRoute);  
            
            //�������ӳ�ʱ
            HttpConnectionParams.setConnectionTimeout(params, 5000);
            //����Socket��ʱ
            HttpConnectionParams.setSoTimeout(params, 12000);
            
            SchemeRegistry schReg = new SchemeRegistry();
            schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 80));
            
            ClientConnectionManager conManager = new ThreadSafeClientConnManager(params, schReg);
            
            httpClient = new DefaultHttpClient(conManager, params);
        }
        
        return httpClient;
    }
}
