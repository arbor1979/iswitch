package com.dandian.iswitch.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import com.dandian.iswitch.utility.ThreadPoolUtils;

public class LineChartActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_chart);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        }
        
    }
   
    /**
     * A fragment containing a line chart.
     */
    public static class PlaceholderFragment extends Fragment {

        private LineChartView chart;
        private LineChartData data;
        private int numberOfLines = 1;
		private int maxNumberOfLines = 4;//×î´ó4¸ö

        JSONObject[] randomNumbersTab;
        private String[] lineName;
		private boolean hasAxes = true;
        private boolean hasAxesNames = true;
        private boolean hasLines = true;
        private boolean hasPoints = true;
        private ValueShape shape = ValueShape.CIRCLE;
        private boolean isFilled = false;
        private boolean hasLabels = false;
        private boolean isCubic = false;
        private boolean hasLabelForSelected = true;
        private boolean pointsHaveDifferentColor;
        private String AxisX,AxisY;
        private String timeRange="day";//day,week,month
        private ProgressBar pb1;
        private List<Device> mData;
        private Comparator<Device> comparator;
        private float maxY,maxX;
        private Spinner spinner;
        private View rootView;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            rootView = inflater.inflate(R.layout.fragment_line_chart, container, false);

            chart = (LineChartView) rootView.findViewById(R.id.chart);
            chart.setOnValueTouchListener(new ValueTouchListener());
            pb1=(ProgressBar)rootView.findViewById(R.id.progressBar1);
            
            comparator = new MyComparator();
            mData= new ArrayList<Device>();
            // Generate some randome values.
            //generateValues();

            //generateData();
           
            // Disable viewpirt recalculations, see toggleCubic() method for more info.
            chart.setViewportCalculationEnabled(false);
            AxisY=getString(R.string.YName);
            getDateByTimeRange();
            
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
            return rootView;
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
    							mData.clear();
    							JSONArray jarr=jo.getJSONArray("deviceList");
    							for(int i=0;i<jarr.length();i++)
    							{
    								Device dev=new Device((JSONObject)jarr.get(i));
    								if(dev!=null)
    									mData.add(dev);
    							}
    							float dianliang=0;
    							for(Device item:mData)
    							{
    								if(item.getDianLiang()>dianliang)
    									dianliang=item.getDianLiang();
    							}
    							if(dianliang>0)
    							{
    								for(int i=0;i<mData.size();i++)
    								{
    									mData.get(i).setPercentDianliang((float)(mData.get(i).getDianLiang())/dianliang);
    								}
    							}
    							Collections.sort(mData, comparator); 
    							initChartData();
    						}
    						else
    							Toast.makeText(getActivity(), tips, Toast.LENGTH_LONG).show();
    					}
    				} catch (JSONException e) {
    					e.printStackTrace();
    				}
    				break;
    			}
    		}
        };
        public class MyComparator implements Comparator<Device> {

    		public int compare(Device s1, Device s2) {
    			if(s1.getDianLiang() > s2.getDianLiang()){
    				return -1;
    			} else if(s1.getDianLiang() < s2.getDianLiang()) {
    				return 1;
    			}
    			return 0;
    		}

    	}
        private void initChartData()
        {
        	numberOfLines=maxNumberOfLines;
        	if(mData.size()<maxNumberOfLines)
        	{
        		numberOfLines=mData.size();
        	}
        	lineName=new String[numberOfLines];
        	randomNumbersTab=new JSONObject[numberOfLines];
        	
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
        	for(int i=0;i<numberOfLines;i++)
        	{
        		lineName[i]=mData.get(i).getDeviceName();
        		randomNumbersTab[i]=mData.get(i).getDianLiangArray();
        		//numberOfPoints=randomNumbersTab[i].length();
        	}
        	
        	generateData();
        }
        /*
        private void generateValues() {
            for (int i = 0; i < maxNumberOfLines; ++i) {
                for (int j = 0; j < numberOfPoints; ++j) {
                    randomNumbersTab[i][j] = (float) Math.random() * 100f;
                }
            }
        }
		*/
        

        private void resetViewport() {
            // Reset viewport height range to (0,100)
            final Viewport v = new Viewport(chart.getMaximumViewport());
            v.bottom = 0;
            if(maxY<10)
            	maxY=10;
            v.top = maxY;
            v.left = 0;
            v.right = maxX;
            chart.setMaximumViewport(v);
            chart.setCurrentViewport(v);
        }

        private void generateData() {

        	if(mData==null || mData.size()==0)
        		return;
        	maxY=10;
            List<Line> lines = new ArrayList<Line>();
            List<AxisValue> listAxis=new ArrayList<AxisValue>();
            
            for (int i = 0; i < numberOfLines; ++i) {
            	if(mData.get(i).getDianLiang()==0)
            		continue;
                List<PointValue> values = new ArrayList<PointValue>();
                String orderKey=mData.get(i).getOrderKey();
                if(orderKey!=null)
        		{
        			String[] keys=orderKey.split(",");
        			for(int j=0;j<keys.length;j++)
        			{
        				float value=(float)randomNumbersTab[i].optDouble(keys[j]);
                    	values.add(new PointValue(j, value));
                    	if(value>maxY)
                    		maxY=value;
                    	if(i==0)
                    	{
                    		AxisValue axValue=new AxisValue(j);
                    		axValue.setLabel(keys[j]);
                    		listAxis.add(axValue);
                    	}
        			}
        			maxX=keys.length-1;
        		
        		}

                Line line = new Line(values);
                line.setColor(ChartUtils.COLORS[i]);
                line.setShape(shape);
                line.setCubic(isCubic);
                line.setFilled(isFilled);
                line.setHasLabels(hasLabels);
                line.setHasLabelsOnlyForSelected(hasLabelForSelected);
                line.setHasLines(hasLines);
                line.setHasPoints(hasPoints);
                if (pointsHaveDifferentColor){
                    line.setPointColor(ChartUtils.COLORS[(i + 1) % ChartUtils.COLORS.length]);
                }
                lines.add(line);
                
                TextView roundsign = null,tv_desc = null;
                
                if(i==0)
                {
                	roundsign=(TextView)rootView.findViewById(R.id.roundSign1);
                	tv_desc=(TextView)rootView.findViewById(R.id.tv_disc1);
                }
                else if(i==1)
                {
                	roundsign=(TextView)rootView.findViewById(R.id.roundSign2);
                	tv_desc=(TextView)rootView.findViewById(R.id.tv_disc2);
                }
                else if(i==2)
                {
                	roundsign=(TextView)rootView.findViewById(R.id.roundSign3);
                	tv_desc=(TextView)rootView.findViewById(R.id.tv_disc3);
                }
                else if(i==3)
                {
                	roundsign=(TextView)rootView.findViewById(R.id.roundSign4);
                	tv_desc=(TextView)rootView.findViewById(R.id.tv_disc4);
                }
                GradientDrawable mdraw=(GradientDrawable)roundsign.getBackground();
            	mdraw.setColor(ChartUtils.COLORS[i]);
            	//roundsign.setBackgroundColor(ChartUtils.COLORS[i]);
            	tv_desc.setText(mData.get(i).getDeviceName());
            }

            data = new LineChartData(lines);

            if (hasAxes) {
                Axis axisX = new Axis();
                Axis axisY = new Axis().setHasLines(true);
                if (hasAxesNames) {
                    axisX.setName(AxisX);
                    axisY.setName(AxisY);
                }
                axisX.setValues(listAxis);
                axisX.setHasTiltedLabels(true);
                data.setAxisXBottom(axisX);
                data.setAxisYLeft(axisY);
            } else {
                data.setAxisXBottom(null);
                data.setAxisYLeft(null);
            }

            data.setBaseValue(Float.NEGATIVE_INFINITY);
            chart.setLineChartData(data);
            resetViewport();
        }

     

        private class ValueTouchListener implements LineChartOnValueSelectListener {

            @Override
            public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
                //Toast.makeText(getActivity(), "Selected: " + value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onValueDeselected() {
                // TODO Auto-generated method stub

            }

        }
        

        /*
        // MENU
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.line_chart, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_reset) {
                reset();
                generateData();
                return true;
            }
            if (id == R.id.action_add_line) {
                addLineToData();
                return true;
            }
            if (id == R.id.action_toggle_lines) {
                toggleLines();
                return true;
            }
            if (id == R.id.action_toggle_points) {
                togglePoints();
                return true;
            }
            if (id == R.id.action_toggle_cubic) {
                toggleCubic();
                return true;
            }
            if (id == R.id.action_toggle_area) {
                toggleFilled();
                return true;
            }
            if (id == R.id.action_point_color) {
                togglePointColor();
                return true;
            }
            if (id == R.id.action_shape_circles) {
                setCircles();
                return true;
            }
            if (id == R.id.action_shape_square) {
                setSquares();
                return true;
            }
            if (id == R.id.action_shape_diamond) {
                setDiamonds();
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
        private void reset() {
            numberOfLines = 1;

            hasAxes = true;
            hasAxesNames = true;
            hasLines = true;
            hasPoints = true;
            shape = ValueShape.CIRCLE;
            isFilled = false;
            hasLabels = false;
            isCubic = false;
            hasLabelForSelected = false;
            pointsHaveDifferentColor = false;

            chart.setValueSelectionEnabled(hasLabelForSelected);
            resetViewport();
        }
        
         //Adds lines to data, after that data should be set again with
         // {@link LineChartView#setLineChartData(LineChartData)}. Last 4th line has non-monotonically x values.
       
        private void addLineToData() {
            if (data.getLines().size() >= maxNumberOfLines) {
                Toast.makeText(getActivity(), "Samples app uses max 4 lines!", Toast.LENGTH_SHORT).show();
                return;
            } else {
                ++numberOfLines;
            }

            generateData();
        }

        private void toggleLines() {
            hasLines = !hasLines;

            generateData();
        }

        private void togglePoints() {
            hasPoints = !hasPoints;

            generateData();
        }

        private void toggleCubic() {
            isCubic = !isCubic;

            generateData();

            if (isCubic) {
                // It is good idea to manually set a little higher max viewport for cubic lines because sometimes line
                // go above or below max/min. To do that use Viewport.inest() method and pass negative value as dy
                // parameter or just set top and bottom values manually.
                // In this example I know that Y values are within (0,100) range so I set viewport height range manually
                // to (-5, 105).
                // To make this works during animations you should use Chart.setViewportCalculationEnabled(false) before
                // modifying viewport.
                // Remember to set viewport after you call setLineChartData().
                final Viewport v = new Viewport(chart.getMaximumViewport());
                v.bottom = -5;
                v.top = 105;
                // You have to set max and current viewports separately.
                chart.setMaximumViewport(v);
                // I changing current viewport with animation in this case.
                chart.setCurrentViewportWithAnimation(v);
            } else {
                // If not cubic restore viewport to (0,100) range.
                final Viewport v = new Viewport(chart.getMaximumViewport());
                v.bottom = 0;
                v.top = 100;

                // You have to set max and current viewports separately.
                // In this case, if I want animation I have to set current viewport first and use animation listener.
                // Max viewport will be set in onAnimationFinished method.
                chart.setViewportAnimationListener(new ChartAnimationListener() {

                    @Override
                    public void onAnimationStarted() {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationFinished() {
                        // Set max viewpirt and remove listener.
                        chart.setMaximumViewport(v);
                        chart.setViewportAnimationListener(null);

                    }
                });
                // Set current viewpirt with animation;
                chart.setCurrentViewportWithAnimation(v);
            }

        }

        private void toggleFilled() {
            isFilled = !isFilled;

            generateData();
        }

        private void togglePointColor() {
            pointsHaveDifferentColor = !pointsHaveDifferentColor;

            generateData();
        }

        private void setCircles() {
            shape = ValueShape.CIRCLE;

            generateData();
        }

        private void setSquares() {
            shape = ValueShape.SQUARE;

            generateData();
        }

        private void setDiamonds() {
            shape = ValueShape.DIAMOND;

            generateData();
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

     
        // To animate values you have to change targets values and then call {@link Chart#startDataAnimation()}
        // method(don't confuse with View.animate()). If you operate on data that was set before you don't have to call
        // {@link LineChartView#setLineChartData(LineChartData)} again.
       
        private void prepareDataAnimation() {
            for (Line line : data.getLines()) {
                for (PointValue value : line.getValues()) {
                    // Here I modify target only for Y values but it is OK to modify X targets as well.
                    value.setTarget(value.getX(), (float) Math.random() * 100);
                }
            }
        }
		*/
    }
}
