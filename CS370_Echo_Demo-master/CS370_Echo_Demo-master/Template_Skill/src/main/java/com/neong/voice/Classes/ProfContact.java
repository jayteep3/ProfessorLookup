package com.neong.voice.Classes;
import java.util.*;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ProfContact {
	private String name;
	private String phone;
	private String email;
	
	public String getName(){ return name;}
	public void setName(String n) { name = n;}
	public String getPhone(){ return phone;}
	public void setPhone(String p) { phone = p;}
	public String getEmail(){ return email;}
	public void setEmail(String e) {email = e;}
	public ProfContact copy(){
		ProfContact pc = new ProfContact();
		pc.setName(name);
		pc.setPhone(phone);
		pc.setEmail(email);
		return pc;
	}
}
