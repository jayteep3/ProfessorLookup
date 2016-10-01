package com.neong.voice.Classes;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
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
	
	public void GetEmailPhone() throws ClassNotFoundException, SQLException
	{
		Connection con = null;
		//Connect to DB in here, get prof contact info.
		try
		{

		Class.forName("com.mysql.jdbc.Driver");
		con = DriverManager.getConnection("jdbc:mysql://cwolf.cs.sonoma.edu:3306/restrella", "restrella", "abc123");
		Statement stmnt = con.createStatement();
			ResultSet rs =  stmnt.executeQuery("SELECT email, phone FROM Professors WHERE (name is Like '" + name + "')");
			email = rs.getString("email");
			phone = "yes";
		}
		catch (SQLException e)
	    {
	        phone = e.toString();
	    }
		//if(con != null) {
			
		//}
		
	}
}
