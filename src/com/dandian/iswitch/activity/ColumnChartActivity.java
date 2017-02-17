package com.dandian.iswitch.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dandian.iswitch.R;
import com.dandian.iswitch.entity.Device;
import com.dandian.iswitch.utility.MyRunnable;
import com.dandian.iswitch.utility.MyUtility;
import com.dandian.iswitch.utility.NumberCircleProgressBar;
import com.dandian.iswitch.utility.ThreadPoolUtils;

public class ColumnChartActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_column_chart);
        String macAddress=getIntent().getStringExtra("macAddress");
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment(macAddress)).commit();
        }
        
        
    }

    /**
     * A fragment containing a column chart.
     */
    public static class PlaceholderFragment extends Fragment {

        



		private static final int DEFAULT_DATA = 0;
        private static final int SUBCOLUMNS_DATA = 1;
        private static final int STACKED_DATA = 2;
        private static final int NEGATIVE_SUBCOLUMNS_DATA = 3;
        private static final int NEGATIVE_STACKED_DATA = 4;

        private ColumnChartView chart;
        private ColumnChartData data;
        private boolean hasAxes = true;
        private boolean hasAxesNames = true;
        private boolean hasLabels = false;
        private boolean hasLabelForSelected = true;
        private int dataType = DEFAULT_DATA;
        private String macAddress;
        private String timeRange="day";//day,week,month
        private ProgressBar pb1;
        private Device device;
        private float maxY,maxX;
        private Spinner spinner;
        private Timer timer;
    	private TimerTask task;
    	private JSONObject realtimePower;
    	private NumberCircleProgressBar bnp1,bnp2,bnp3;
        public PlaceholderFragment(String macAddress) {
        	this.macAddress=macAddress;
        }

        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            View rootView = inflater.inflate(R.layout.fragment_column_chart, container, false);
            pb1=(ProgressBar)rootView.findViewById(R.id.progressBar1);
            chart = (ColumnChartView) rootView.findViewById(R.id.chart);
            chart.setOnValueTouchListener(new ValueTouchListener());
            TextView backbtn=(TextView)rootView.findViewById(R.id.leftButton);
            backbtn.setText(R.string.back);
    		backbtn.setOnClickListener(new OnClickListener() {
    			public void onClick(View v) {
    				getActivity().finish();
    			    }
    		} );
            spinner=(Spinner) rootView.findViewById(R.id.spinner1);  
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {  
            	  
                @Override  
                public void onItemSelected(AdapterView<?> parent, View view,  
                        int position, long id) {  
                	if(position==0)
                		timeRange="day";
                	else if(position==1)
                		timeRange="week";
                	else if(position==2)
                		timeRange="month";
                	else if(position==3)
                		timeRange="year";
                	getDateByTimeRange();
                    //Spinner spinner=(Spinner) parent;  
                    //Toast.makeText(getActivity(), "xxxx"+spinner.getItemAtPosition(position), Toast.LENGTH_LONG).show();  
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
                }
            });
            bnp1 = (NumberCircleProgressBar)rootView.findViewById(R.id.numbercircleprogress_bar1);
            bnp2 = (NumberCircleProgressBar)rootView.findViewById(R.id.numbercircleprogress_bar2);
            bnp3 = (NumberCircleProgressBar)rootView.findViewById(R.id.numbercircleprogress_bar3);
            return rootView;
        }
        
        @Override
    	public  void onResume()
    	{
    		super.onResume();
    		if(MyUtility.isConnect(getActivity()))
    			getRealtimeData();
    		
    		if(timer==null)
    			timer = new Timer(true);
    		task = new MyTimerTask();
    		timer.schedule(task,0, 5000);
    		
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
    	      message.what = 3;      
    	      mHandler.sendMessage(message);    
    	   }  
    	};
    	private void getRealtimeData()
    	{
    		String url="http://school.eduyun.la/wulian/functions.php?action=getRealtimePower";
    		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    		String userName=pref.getString("userName","");
    		String password=pref.getString("password","");
    	 	JSONObject json=new JSONObject();
    	 	try {
    			json.put("username",userName);
    			json.put("password",password);
    			json.put("macAddress",macAddress);
    		} catch (JSONException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
            ThreadPoolUtils.execute(new MyRunnable(url,"post",MyUtility.jsonToEntity(json),mHandler,2));
    	}
        private void getDateByTimeRange()
    	{
    		pb1.setVisibility(View.VISIBLE);
    		String url="http://school.eduyun.la/wulian/functions.php?action=getMyDevicesGroup";
    		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    		String userName=pref.getString("userName","");
    		String password=pref.getString("password","");
    	 	JSONObject json=new JSONObject();
    	 	try {
    			json.put("username",userName);
    			json.put("password",password);
    			json.put("timeRange",timeRange);
    			json.put("macAddress",macAddress);
    		} catch (JSONException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
            ThreadPoolUtils.execute(new MyRunnable(url,"post",MyUtility.jsonToEntity(json),mHandler,1));
    	}
        
        private Handler mHandler = new Handler() {

    		public void handleMessage(Message msg) {
    			
    			switch (msg.what) {
    			case -1:
    				pb1.setVisibility(View.INVISIBLE);
    				Toast.makeText(getActivity(), (CharSequence) msg.obj, Toast.LENGTH_LONG).show();
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
    					
    							JSONArray jarr=jo.getJSONArray("deviceList");
    							device=new Device((JSONObject)jarr.get(0));
    							if(device!=null)
    								generateData();
    							else
    								Toast.makeText(getActivity(), R.string.dataisempty, Toast.LENGTH_LONG).show();
    						}
    						else
    							Toast.makeText(getActivity(), tips, Toast.LENGTH_LONG).show();
    					}
    				} catch (JSONException e) {
    					e.printStackTrace();
    				}
    				break;
    			case 2:
    				
    				realtimePower = null;
    				try 
    				{
    					realtimePower = new JSONObject((String)msg.obj);
    					if(realtimePower!=null)
    					{
    						float realtime=(float)realtimePower.optDouble("realtime");
    						float topvalue=(float)realtimePower.optDouble("topvalue");
    						float bottomvalue=(float)realtimePower.optDouble("bottomvalue");
    						float maxvalue=(float)realtimePower.optDouble("maxvalue");
    						if(realtime>maxvalue)
    							bnp1.setMax(realtime);
    						else
    							bnp1.setMax(maxvalue);
    						
    						if(topvalue>maxvalue)
    							bnp2.setMax(topvalue);
    						else
    							bnp2.setMax(maxvalue);
    						if(bottomvalue>maxvalue)
    							bnp3.setMax(bottomvalue);
    						else
    							bnp3.setMax(maxvalue);
    						bnp1.setProgress(realtime);
    						bnp2.setProgress(topvalue);
    						bnp3.setProgress(bottomvalue);
    					}
    				} catch (JSONException e) {
    					e.printStackTrace();
    				}
    				break;
    			case 3:
    				getRealtimeData();
    				break;
    			}
    		}
        };
        /*
        // MENU
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.column_chart, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_reset) {
                reset();
                generateData();
                return true;
            }
            if (id == R.id.action_subcolumns) {
                dataType = SUBCOLUMNS_DATA;
                generateData();
                return true;
            }
            if (id == R.id.action_stacked) {
                dataType = STACKED_DATA;
                generateData();
                return true;
            }
            if (id == R.id.action_negative_subcolumns) {
                dataType = NEGATIVE_SUBCOLUMNS_DATA;
                generateData();
                return true;
            }
            if (id == R.id.action_negative_stacked) {
                dataType = NEGATIVE_STACKED_DATA;
                generateData();
                return true;
            }
            if (id == R.id.action_toggle_labels) {
                toggleLabels();
                return true;
            }
            if (id == R.id.action_toggle_axes) {
                toggleAxes();
                return true;
            }
            if (id == R.id.action_toggle_axes_names) {
                toggleAxesNames();
                return true;
            }
            if (id == R.id.action_animate) {
                prepareDataAnimation();
                chart.startDataAnimation();
                return true;
            }
            if (id == R.id.action_toggle_selection_mode) {
                toggleLabelForSelected();

                Toast.makeText(getActivity(),
                        "Selection mode set to " + chart.isValueSelectionEnabled() + " select any point.",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            if (id == R.id.action_toggle_touch_zoom) {
                chart.setZoomEnabled(!chart.isZoomEnabled());
                Toast.makeText(getActivity(), "IsZoomEnabled " + chart.isZoomEnabled(), Toast.LENGTH_SHORT).show();
                return true;
            }
            if (id == R.id.action_zoom_both) {
                chart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
                return true;
            }
            if (id == R.id.action_zoom_horizontal) {
                chart.setZoomType(ZoomType.HORIZONTAL);
                return true;
            }
            if (id == R.id.action_zoom_vertical) {
                chart.setZoomType(ZoomType.VERTICAL);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
         private void toggleLabels() {
            hasLabels = !hasLabels;

            if (hasLabels) {
                hasLabelForSelected = false;
                chart.setValueSelectionEnabled(hasLabelForSelected);
            }

            generateData();
        }

        private void toggleLabelForSelected() {
            hasLabelForSelected = !hasLabelForSelected;
            chart.setValueSelectionEnabled(hasLabelForSelected);

            if (hasLabelForSelected) {
                hasLabels = false;
            }

            generateData();
        }

        private void toggleAxes() {
            hasAxes = !hasAxes;

            generateData();
        }

        private void toggleAxesNames() {
            hasAxesNames = !hasAxesNames;

            generateData();
        }

        private void prepareDataAnimation() {
            for (Column column : data.getColumns()) {
                for (SubcolumnValue value : column.getValues()) {
                    value.setTarget((float) Math.random() * 100);
                }
            }
        }
		*/
        private void reset() {
            hasAxes = true;
            hasAxesNames = true;
            hasLabels = false;
            hasLabelForSelected = true;
            dataType = DEFAULT_DATA;
            chart.setValueSelectionEnabled(hasLabelForSelected);

        }
        private void resetViewport() {
            // Reset viewport height range to (0,100)
            final Viewport v = new Viewport(chart.getMaximumViewport());
            v.bottom = 0;
            v.top = maxY;
            v.left = 0;
            v.right = maxX;
            chart.setMaximumViewport(v);
            chart.setCurrentViewport(v);
        }

        private void generateDefaultData() {
        	if(device.getDianLiangArray()==null)
        		return;
        	maxY=10;
            int numSubcolumns = 1;
            List<Column> columns = new ArrayList<Column>();
            List<SubcolumnValue> values;
            List<AxisValue> listAxis=new ArrayList<AxisValue>();
            
        	String orderKey=device.getOrderKey();
        	if(orderKey!=null)
    		{
    			String[] keys=orderKey.split(",");
    			for(int i=0;i<keys.length;i++)
    			{
    				values = new ArrayList<SubcolumnValue>();
    				for (int j = 0; j < numSubcolumns; ++j) {
    					float value=(float)device.getDianLiangArray().optDouble(keys[i]);
    					values.add(new SubcolumnValue(value, ChartUtils.pickColor()));
    					if(value>maxY)
                    		maxY=value;
    				}

    				Column column = new Column(values);
    				column.setHasLabels(hasLabels);
    				column.setHasLabelsOnlyForSelected(hasLabelForSelected);
    				columns.add(column);
    				
    				AxisValue axValue=new AxisValue(i);
            		axValue.setLabel(keys[i]);
            		listAxis.add(axValue);
    			}
    			maxY=maxY+10;
    			maxX=keys.length;
    			
    		}

            data = new ColumnChartData(columns);

            if (hasAxes) {
                Axis axisX = new Axis();
                Axis axisY = new Axis().setHasLines(true);
                if (hasAxesNames) {
                	String AxisX = null;
                	if(timeRange.equals("day"))
        			{
        				AxisX=getString(R.string.XNameHour);
        			}
        			else if(timeRange.equals("week"))
        			{
        				AxisX=getString(R.string.XNameDay);
        			}
        			else if(timeRange.equals("month"))
        			{
        				AxisX=getString(R.string.XNameMonth);
        			}
        			else if(timeRange.equals("year"))
        			{
        				AxisX=getString(R.string.XNameYear);
        			}
                    axisX.setName(AxisX);
                    axisY.setName(getString(R.string.YName));
                }
                axisX.setValues(listAxis);
                axisX.setHasTiltedLabels(true);
                data.setAxisXBottom(axisX);
                data.setAxisYLeft(axisY);
            } else {
                data.setAxisXBottom(null);
                data.setAxisYLeft(null);
            }

            chart.setColumnChartData(data);
            resetViewport();

        }

        /**
         * Generates columns with subcolumns, columns have larger separation than subcolumns.
         */
        private void generateSubcolumnsData() {
            int numSubcolumns = 4;
            int numColumns = 4;
            // Column can have many subcolumns, here I use 4 subcolumn in each of 8 columns.
            List<Column> columns = new ArrayList<Column>();
            List<SubcolumnValue> values;
            for (int i = 0; i < numColumns; ++i) {

                values = new ArrayList<SubcolumnValue>();
                for (int j = 0; j < numSubcolumns; ++j) {
                    values.add(new SubcolumnValue((float) Math.random() * 50f + 5, ChartUtils.pickColor()));
                }

                Column column = new Column(values);
                column.setHasLabels(hasLabels);
                column.setHasLabelsOnlyForSelected(hasLabelForSelected);
                columns.add(column);
            }

            data = new ColumnChartData(columns);

            if (hasAxes) {
                Axis axisX = new Axis();
                Axis axisY = new Axis().setHasLines(true);
                if (hasAxesNames) {
                    axisX.setName("Axis X");
                    axisY.setName("Axis Y");
                }
                data.setAxisXBottom(axisX);
                data.setAxisYLeft(axisY);
            } else {
                data.setAxisXBottom(null);
                data.setAxisYLeft(null);
            }

            chart.setColumnChartData(data);

        }

        /**
         * Generates columns with stacked subcolumns.
         */
        private void generateStackedData() {
            int numSubcolumns = 4;
            int numColumns = 8;
            // Column can have many stacked subcolumns, here I use 4 stacke subcolumn in each of 4 columns.
            List<Column> columns = new ArrayList<Column>();
            List<SubcolumnValue> values;
            for (int i = 0; i < numColumns; ++i) {

                values = new ArrayList<SubcolumnValue>();
                for (int j = 0; j < numSubcolumns; ++j) {
                    values.add(new SubcolumnValue((float) Math.random() * 20f + 5, ChartUtils.pickColor()));
                }

                Column column = new Column(values);
                column.setHasLabels(hasLabels);
                column.setHasLabelsOnlyForSelected(hasLabelForSelected);
                columns.add(column);
            }

            data = new ColumnChartData(columns);

            // Set stacked flag.
            data.setStacked(true);

            if (hasAxes) {
                Axis axisX = new Axis();
                Axis axisY = new Axis().setHasLines(true);
                if (hasAxesNames) {
                    axisX.setName("Axis X");
                    axisY.setName("Axis Y");
                }
                data.setAxisXBottom(axisX);
                data.setAxisYLeft(axisY);
            } else {
                data.setAxisXBottom(null);
                data.setAxisYLeft(null);
            }

            chart.setColumnChartData(data);
        }

        private void generateNegativeSubcolumnsData() {

            int numSubcolumns = 4;
            int numColumns = 4;
            List<Column> columns = new ArrayList<Column>();
            List<SubcolumnValue> values;
            for (int i = 0; i < numColumns; ++i) {

                values = new ArrayList<SubcolumnValue>();
                for (int j = 0; j < numSubcolumns; ++j) {
                    int sign = getSign();
                    values.add(new SubcolumnValue((float) Math.random() * 50f * sign + 5 * sign, ChartUtils.pickColor
                            ()));
                }

                Column column = new Column(values);
                column.setHasLabels(hasLabels);
                column.setHasLabelsOnlyForSelected(hasLabelForSelected);
                columns.add(column);
            }

            data = new ColumnChartData(columns);

            if (hasAxes) {
                Axis axisX = new Axis();
                Axis axisY = new Axis().setHasLines(true);
                if (hasAxesNames) {
                    axisX.setName("Axis X");
                    axisY.setName("Axis Y");
                }
                data.setAxisXBottom(axisX);
                data.setAxisYLeft(axisY);
            } else {
                data.setAxisXBottom(null);
                data.setAxisYLeft(null);
            }

            chart.setColumnChartData(data);
        }

        private void generateNegativeStackedData() {

            int numSubcolumns = 4;
            int numColumns = 8;
            // Column can have many stacked subcolumns, here I use 4 stacke subcolumn in each of 4 columns.
            List<Column> columns = new ArrayList<Column>();
            List<SubcolumnValue> values;
            for (int i = 0; i < numColumns; ++i) {

                values = new ArrayList<SubcolumnValue>();
                for (int j = 0; j < numSubcolumns; ++j) {
                    int sign = getSign();
                    values.add(new SubcolumnValue((float) Math.random() * 20f * sign + 5 * sign, ChartUtils.pickColor()));
                }

                Column column = new Column(values);
                column.setHasLabels(hasLabels);
                column.setHasLabelsOnlyForSelected(hasLabelForSelected);
                columns.add(column);
            }

            data = new ColumnChartData(columns);

            // Set stacked flag.
            data.setStacked(true);

            if (hasAxes) {
                Axis axisX = new Axis();
                Axis axisY = new Axis().setHasLines(true);
                if (hasAxesNames) {
                    axisX.setName("Axis X");
                    axisY.setName("Axis Y");
                }
                data.setAxisXBottom(axisX);
                data.setAxisYLeft(axisY);
            } else {
                data.setAxisXBottom(null);
                data.setAxisYLeft(null);
            }

            chart.setColumnChartData(data);
        }

        private int getSign() {
            int[] sign = new int[]{-1, 1};
            return sign[Math.round((float) Math.random())];
        }

        private void generateData() {
            switch (dataType) {
                case DEFAULT_DATA:
                    generateDefaultData();
                    break;
                case SUBCOLUMNS_DATA:
                    generateSubcolumnsData();
                    break;
                case STACKED_DATA:
                    generateStackedData();
                    break;
                case NEGATIVE_SUBCOLUMNS_DATA:
                    generateNegativeSubcolumnsData();
                    break;
                case NEGATIVE_STACKED_DATA:
                    generateNegativeStackedData();
                    break;
                default:
                    generateDefaultData();
                    break;
            }
        }

       

        private class ValueTouchListener implements ColumnChartOnValueSelectListener {

            @Override
            public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
                //Toast.makeText(getActivity(), "Selected: " + value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onValueDeselected() {
                // TODO Auto-generated method stub

            }

        }

    }
}
