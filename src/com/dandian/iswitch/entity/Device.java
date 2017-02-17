package com.dandian.iswitch.entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Device implements  Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6385863748274112020L;
	private int id;
	private String macAddress;
	private String deviceName;
	private String ipAddress;
	private String regTime;
	private String status;
	
	private int privilege;
	private String deviceImg;
	private int usersNum;
	private String manager;
	private int jidianqiFlag;
	private float dianLiang;
	private float percentDianliang;
	private JSONObject dianLiangArray;
	private String orderKey;
	public float getPercentDianliang() {
		return percentDianliang;
	}

	public void setPercentDianliang(float percentDianliang) {
		this.percentDianliang = percentDianliang;
	}

	public Device(JSONObject obj) {
		// TODO Auto-generated constructor stub
		id=obj.optInt("id");
		macAddress=obj.optString("macAddress");
		deviceName=obj.optString("deviceName");
		ipAddress=obj.optString("ipAddress");
		regTime=obj.optString("regTime");
		status=obj.optString("status");
		
		privilege=obj.optInt("privilege");
		deviceImg=obj.optString("deviceImg");
		usersNum=obj.optInt("usersNum");
		manager=obj.optString("manager");
		jidianqiFlag=obj.optInt("jidianqiFlag");
		dianLiang=(float)obj.optDouble("dianLiang");
		dianLiangArray=obj.optJSONObject("dianLiangArray");
		orderKey=obj.optString("orderKey");
	}
	

	public String getOrderKey() {
		return orderKey;
	}

	public void setOrderKey(String orderKey) {
		this.orderKey = orderKey;
	}

	public JSONObject getDianLiangArray() {

		return dianLiangArray;
	}

	public void setDianLiangArray(JSONObject dianLiangArray) {
		this.dianLiangArray = dianLiangArray;
	}

	public float getDianLiang() {
		return dianLiang;
	}

	public void setDianLiang(float dianLiang) {
		this.dianLiang = dianLiang;
	}

	public int getJidianqiFlag() {
		return jidianqiFlag;
	}

	public void setJidianqiFlag(int jidianqiFlag) {
		this.jidianqiFlag = jidianqiFlag;
	}

	public String getManager() {
		return manager;
	}
	public void setManager(String manager) {
		this.manager = manager;
	}
	public int getUsersNum() {
		return usersNum;
	}
	public void setUsersNum(int usersNum) {
		this.usersNum = usersNum;
	}
	public int getPrivilege() {
		return privilege;
	}
	public void setPrivilege(int privilege) {
		this.privilege = privilege;
	}
	public String getDeviceImg() {
		return deviceImg;
	}
	public void setDeviceImg(String deviceImg) {
		this.deviceImg = deviceImg;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getIdAddress() {
		return ipAddress;
	}
	public void setIdAddress(String idAddress) {
		this.ipAddress = idAddress;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getRegTime() {
		return regTime;
	}
	public void setRegTime(String regTime) {
		this.regTime = regTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
}
