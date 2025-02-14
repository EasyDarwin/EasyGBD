package com.easygbs.easygbd.dao.bean;

public class Chan{
	private int ec;
	private int uid;
	private String cid;
	private String na;
	//0:无效  1:有效
	private int sta;
	private int selected;

	public int getEc() {
		return ec;
	}

	public void setEc(int ec) {
		this.ec = ec;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getNa() {
		return na;
	}

	public void setNa(String na) {
		this.na = na;
	}

	public int getSta() {
		return sta;
	}

	public void setSta(int sta) {
		this.sta = sta;
	}

	public int getSelected() {
		return selected;
	}

	public void setSelected(int selected) {
		this.selected = selected;
	}
}