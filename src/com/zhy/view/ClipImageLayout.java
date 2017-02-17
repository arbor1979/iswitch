package com.zhy.view;

import com.dandian.iswitch.utility.ImageUtility;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.RelativeLayout;


/*
 * http://blog.csdn.net/lmj623565791/article/details/39761281
 * @author zhy
 *
 */
public class ClipImageLayout extends RelativeLayout
{

	

	private ClipZoomImageView mZoomImageView;
	private ClipImageBorderView mClipImageView;
	public String picPath;

	/**
	 * 鏉╂瑩鍣峰ù瀣槸閿涘瞼娲块幒銉ュ晸濮濊绨℃径褍鐨敍宀�婀″锝勫▏閻€劏绻冪粙瀣╄厬閿涘苯褰叉禒銉﹀絹閸欐牔璐熼懛顏勭暰娑斿鐫橀幀锟�
	 */
	private int mHorizontalPadding = 20;

	public ClipImageLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		mZoomImageView = new ClipZoomImageView(context);
		mClipImageView = new ClipImageBorderView(context);

		android.view.ViewGroup.LayoutParams lp = new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		
		/**
		 * 鏉╂瑩鍣峰ù瀣槸閿涘瞼娲块幒銉ュ晸濮濊绨￠崶鍓у閿涘瞼婀″锝勫▏閻€劏绻冪粙瀣╄厬閿涘苯褰叉禒銉﹀絹閸欐牔璐熼懛顏勭暰娑斿鐫橀幀锟�
		 */
		
		
		this.addView(mZoomImageView, lp);
		this.addView(mClipImageView, lp);

		
		// 鐠侊紕鐣籶adding閻ㄥ埦x
		mHorizontalPadding = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, mHorizontalPadding, getResources()
						.getDisplayMetrics());
		mZoomImageView.setHorizontalPadding(mHorizontalPadding);
		mClipImageView.setHorizontalPadding(mHorizontalPadding);
	}

	/**
	 * 鐎电懓顦婚崗顒�绔风拋鍓х枂鏉堢绐涢惃鍕煙濞夛拷,閸楁洑缍呮稉绡竝
	 * 
	 * @param mHorizontalPadding
	 */
	public void setHorizontalPadding(int mHorizontalPadding)
	{
		this.mHorizontalPadding = mHorizontalPadding;
	}
	public String getPicPath() {
		return picPath;
	}

	public void setPicPath(String picPath) {
		this.picPath = picPath;
		Bitmap bm=ImageUtility.getDiskBitmapByPath(picPath);
		mZoomImageView.setImageBitmap(bm);
	}
	/**
	 * 鐟佷礁鍨忛崶鍓у
	 * 
	 * @return
	 */
	public Bitmap clip()
	{
		return mZoomImageView.clip();
	}

}
