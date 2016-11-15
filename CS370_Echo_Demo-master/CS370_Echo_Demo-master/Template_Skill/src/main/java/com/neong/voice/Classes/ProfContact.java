package com.neong.voice.Classes;


public class ProfContact {
	private String name;
	private String phone;
	private String email;
	private String building_name;
	
	public String getName(){ return name;}
	public void setName(String n) { name = n;}
	public String getPhone(){ return phone;}
	public void setPhone(String p) { phone = p;}
	public String getEmail(){ return email;}
	public void setEmail(String e) {email = e;}
	public String getBuildingName() { return building_name;}
	public void setBuildingName(String b) {building_name = b;}
	public ProfContact copy(){
		ProfContact pc = new ProfContact();
		pc.setName(name);
		pc.setPhone(phone);
		pc.setEmail(email);
		pc.setBuildingName(building_name);
		return pc;
	}
}
