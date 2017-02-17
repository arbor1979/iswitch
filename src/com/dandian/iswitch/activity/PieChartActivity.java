package com.dandian.iswitch.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.PieChartView;
import android.content.SharedPreferences;
import android.graphics.Typeface;
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
import com.dandian.iswitch.utility.ThreadPoolUtils;


public class PieChartActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie_chart);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    /**
     * A fragment containing a pie chart.
     */
    public static class PlaceholderFragment extends Fragment {

        private PieChartView chart;
        private PieChartData data;

        private boolean hasLabels = true;
        private boolean hasLabelsOutside = false;
        private boolean hasCenterCircle = false;
        private boolean hasCenterText1 = false;
        private boolean hasCenterText2 = false;
        private boolean isExploded = false;
        private boolean hasLabelForSelected = false;
        private String timeRange="day";//day,week,month
        private ProgressBar pb1;
        private List<Device> mData;
        private Spinner spinner;
        
        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            View rootView = inflater.inflate(R.layout.fragment_pie_chart, container, false);

            chart = (PieChartView) rootView.findViewById(R.id.chart);
            chart.setOnValueTouchListener(new ValueTouchListener());
            pb1=(ProgressBar)rootView.findViewById(R.id.progressBar1);
            mData= new ArrayList<Device>();
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
    		String url="http://school.eduyun.la/wulian/functions.php?action=getMyDevices";
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
    							generateData();
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
        
        /*
        // MENU
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.pie_chart, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_reset) {
                reset();
                generateData();
                return true;
            }
            if (id == R.id.action_explode) {
                explodeChart();
                return true;
            }
            if (id == R.id.action_center_circle) {
                hasCenterCircle = !hasCenterCircle;
                if (!hasCenterCircle) {
                    hasCenterText1 = false;
                    hasCenterText2 = false;
                }

                generateData();
                return true;
            }
            if (id == R.id.action_center_text1) {
                hasCenterText1 = !hasCenterText1;

                if (hasCenterText1) {
                    hasCenterCircle = true;
                }

                hasCenterText2 = false;

                generateData();
                return true;
            }
            if (id == R.id.action_center_text2) {
                hasCenterText2 = !hasCenterText2;

                if (hasCenterText2) {
                    hasCenterText1 = true;// text 2 need text 1 to by also drawn.
                    hasCenterCircle = true;
                }

                generateData();
                return true;
            }
            if (id == R.id.action_toggle_labels) {
                toggleLabels();
                return true;
            }
            if (id == R.id.action_toggle_labels_outside) {
                toggleLabelsOutside();
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
            return super.onOptionsItemSelected(item);
        }
        private void explodeChart() {
            isExploded = !isExploded;
            generateData();

        }

        private void toggleLabelsOutside() {
            // has labels have to be true:P
            hasLabelsOutside = !hasLabelsOutside;
            if (hasLabelsOutside) {
                hasLabels = true;
                hasLabelForSelected = false;
                chart.setValueSelectionEnabled(hasLabelForSelected);
            }

            if (hasLabelsOutside) {
                chart.setCircleFillRatio(0.7f);
            } else {
                chart.setCircleFillRatio(1.0f);
            }

            generateData();

        }

        private void toggleLabels() {
            hasLabels = !hasLabels;

            if (hasLabels) {
                hasLabelForSelected = false;
                chart.setValueSelectionEnabled(hasLabelForSelected);

                if (hasLabelsOutside) {
                    chart.setCircleFillRatio(0.7f);
                } else {
                    chart.setCircleFillRatio(1.0f);
                }
            }

            generateData();
        }

        private void toggleLabelForSelected() {
            hasLabelForSelected = !hasLabelForSelected;

            chart.setValueSelectionEnabled(hasLabelForSelected);

            if (hasLabelForSelected) {
                hasLabels = false;
                hasLabelsOutside = false;

                if (hasLabelsOutside) {
                    chart.setCircleFillRatio(0.7f);
                } else {
                    chart.setCircleFillRatio(1.0f);
                }
            }

            generateData();
        }

       
        private void prepareDataAnimation() {
            for (SliceValue value : data.getValues()) {
                value.setTarget((float) Math.random() * 30 + 15);
            }
        }
        */

        private void reset() {
            chart.setCircleFillRatio(1.0f);
            hasLabels = false;
            hasLabelsOutside = false;
            hasCenterCircle = false;
            hasCenterText1 = false;
            hasCenterText2 = false;
            isExploded = false;
            hasLabelForSelected = false;
        }

        private void generateData() {
            int numValues = 6;

            List<SliceValue> values = new ArrayList<SliceValue>();
            for (int i = 0; i < mData.size(); ++i) {
            	if(mData.get(i).getDianLiang()==0)
            		continue;
                SliceValue sliceValue = new SliceValue(mData.get(i).getDianLiang(), ChartUtils.pickColor());
                sliceValue.setLabel(mData.get(i).getDeviceName());
                values.add(sliceValue);
            }

            data = new PieChartData(values);
            data.setHasLabels(hasLabels);
            data.setHasLabelsOnlyForSelected(hasLabelForSelected);
            data.setHasLabelsOutside(hasLabelsOutside);
            data.setHasCenterCircle(hasCenterCircle);

            if (isExploded) {
                data.setSlicesSpacing(24);
            }

            if (hasCenterText1) {
                data.setCenterText1("Hello!");

                // Get roboto-italic font.
                Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Italic.ttf");
                data.setCenterText1Typeface(tf);

                // Get font size from dimens.xml and convert it to sp(library uses sp values).
                data.setCenterText1FontSize(ChartUtils.px2sp(getResources().getDisplayMetrics().scaledDensity,
                        (int) getResources().getDimension(R.dimen.pie_chart_text1_size)));
            }

            if (hasCenterText2) {
                data.setCenterText2("Charts (Roboto Italic)");

                Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Italic.ttf");

                data.setCenterText2Typeface(tf);
                data.setCenterText2FontSize(ChartUtils.px2sp(getResources().getDisplayMetrics().scaledDensity,
                        (int) getResources().getDimension(R.dimen.pie_chart_text2_size)));
            }

            chart.setPieChartData(data);
        }

        

        private class ValueTouchListener implements PieChartOnValueSelectListener {

            @Override
            public void onValueSelected(int arcIndex, SliceValue value) {
                //Toast.makeText(getActivity(), "Selected: " + value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onValueDeselected() {
                // TODO Auto-generated method stub

            }

        }
    }
}
