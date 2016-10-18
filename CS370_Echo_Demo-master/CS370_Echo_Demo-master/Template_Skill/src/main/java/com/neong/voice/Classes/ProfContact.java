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
	private static String name;
	private static String phone;
	private static String email;
	
	public String getName(){ return name;}
	public void setName(String n) { name = n;}
	public String getPhone(){ return phone;}
	public void setPhone(String p) { phone = p;}
	public String getEmail(){ return email;}
	public void setEmail(String e) {email = e;}
	
	public void GetEmailPhone(String name2) throws ClassNotFoundException, SQLException
	{
		
		// setup

		// assume name2 is only a last name
		// for first and last name name string = first_name + "%20" + last name
		String full_url = "https://moonlight.cs.sonoma.edu/api/v1/directory/person/?format=json&search=" + name2;
			// gives an unknown exception
			// from front desk: george ledin has no phone number
		try
		{
				//URL obj = new URL(full_url);
				//HttpURLConnection con =(HttpURLConnection) obj.openConnection();
				//con.setRequestMethod("Get");
				//con.setDoOutput(true);
				//con.connect();
				//con.setRequestProperty("User-Agent", "Mozilla/5.0");
				//int response_code = con.getResponseCode();
				//System.out.println("\nSending 'GET' request to URL: " + full_url);
				//System
				/*
				BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response4 = new StringBuffer();

				while ((inputLine = in.readLine()) != null)
				{
					response4.append(inputLine);
				}
				in.close();*/
				//pc.setPhone(response4.toString());
				// setup url for https call
				StringBuilder result = new StringBuilder();
				URL url2 = new URL(full_url);
						
				// make https call
				HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
				conn.setRequestMethod("GET");
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line;
				while ((line = rd.readLine()) != null)
				{
					result.append(line);
				}
				rd.close();
				conn.disconnect();
					      
				// convert json formated text to json data structure
				String json_text = result.toString();
				//URLConnection connection = new URL(full_url/*url + "?" + query*/).openConnection();
										
						
				// gets the html page with the json
						
						
				JSONArray arr = new JSONArray(json_text);
				JSONObject json = arr.getJSONObject(0);

				// assumes response_body is already valid json
				//JSONObject json = new JSONObject(response2);
				//JSONObject obj = null;
				// com.amazonaws.util.json.JSONException: JSONObject[\"display_name\"] not found."
				//for(int i = 0; i < arr.length(); i++)
				//{
								
				//if(arr.getJSONObject(i).getString("first_name").equals("Janet"))
								
				//obj = arr.getJSONObject(0);
				//}
				//}
				email = json.getString("email");
				phone = json.getString("phone");/* + "   " + arr.toString()*//*json.getString("first_name") + " " + json.getString("last_name")*//*result.toString()*//*response_body*//*obj.toString()*//*.getString("first_name")*//*works/*arr.getJSONObject(0).getString("created")*//*obj.getString("id")*//*response_body*//*json.getString("display_name")*///json.getString("name")); //System.out.println(response_body);
				/*String number_part_1 = pc.getPhone().substring(2, 5);
				String number_part_2 = pc.getPhone().substring(5, 9);
				String number_part_3 = pc.getPhone().substring(9, 13);
				pc.setPhone(number_part_1 + "," + number_part_2 + "," + number_part_3);*/
		}
		catch (JSONException e)
		{
						
			phone = e.toString();

		}
		catch(IOException e)
		{
			phone = e.toString();
		}
		
		
	}
}
