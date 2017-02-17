package com.dandian.iswitch.utility;

import java.text.SimpleDateFormat;  
import java.util.Calendar;  







import com.dandian.iswitch.R;

import android.app.Activity;  
import android.app.AlertDialog;  
import android.content.DialogInterface;  
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.DatePicker;  
import android.widget.DatePicker.OnDateChangedListener;  
import android.widget.LinearLayout;  
import android.widget.TextView;
import android.widget.TimePicker;  
import android.widget.TimePicker.OnTimeChangedListener; 

public class DateTimePickDialogUtil1  implements OnDateChangedListener,  
OnTimeChangedListener{
	private DatePicker datePicker;  
    private TimePicker timePicker;  
    private AlertDialog ad;  
    private String dateTime,pattern,title;  
    private String initDateTime;  
    private Activity activity; 
    
    public DateTimePickDialogUtil1(Activity activity, String initDateTime,String pattern,String title) {  
        this.activity = activity;  
        this.initDateTime = initDateTime;  
        this.pattern=pattern;
        this.title=title;
  
    } 
    
    public void init(DatePicker datePicker, TimePicker timePicker) {  
        Calendar calendar = Calendar.getInstance();  
        if (!(null == initDateTime || "".equals(initDateTime))) {  
            calendar.setTime(DateHelper.getStringDate(initDateTime, pattern));  
        } else {  
            initDateTime = DateHelper.getDateString(calendar.getTime(), pattern);
        }  
  
        datePicker.init(calendar.get(Calendar.YEAR),  
                calendar.get(Calendar.MONTH),  
                calendar.get(Calendar.DAY_OF_MONTH), this);  
        timePicker.setIs24HourView(true);  
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));  
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));  
    }
    
    /** 
     * 弹出日期时间选择框方法 
     *  
     * @param inputDate 
     *            :为需要设置的日期时间文本编辑框 
     * @return 
     */  
    public AlertDialog dateTimePicKDialog(final Button inputDate,final Handler mHandler) {  
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(activity); 
    	LinearLayout view = (LinearLayout) activity  
                .getLayoutInflater().inflate( R.layout.date_time_dialog, null); 
        datePicker = (DatePicker) view.findViewById(R.id.date_picker); 
        timePicker = (android.widget.TimePicker) view.findViewById(R.id.time_picker); 
     
        init(datePicker, timePicker);  
        timePicker.setOnTimeChangedListener(this);  
  
        ad = builder  
                .setTitle(title)  
                .setView(view)  
                .setPositiveButton(activity.getString(R.string.go), new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int whichButton) {  
                        //inputDate.setText(dateTime); 
                        TextView question=(TextView) inputDate.getTag();
                        question.setText(dateTime);
                        Message message = new Message();      
              	      	message.what = 2;      
              	      	mHandler.sendMessage(message);   
                        
                    }  
                })  
                .setNegativeButton(activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int whichButton) {  
                        //inputDate.setText("");  
                    }  
                }).show();  
  
        onDateChanged(null, 0, 0, 0);  
        return ad;  
    }  
  
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {  
        onDateChanged(null, 0, 0, 0);  
    }  
  
    public void onDateChanged(DatePicker view, int year, int monthOfYear,  
            int dayOfMonth) {  
        // 获得日历实例  
        Calendar calendar = Calendar.getInstance();  
  
        calendar.set(datePicker.getYear(), datePicker.getMonth(),  
                datePicker.getDayOfMonth(), timePicker.getCurrentHour(),  
                timePicker.getCurrentMinute());  
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);  
  
        dateTime = sdf.format(calendar.getTime());  
        
    }  
  

}
