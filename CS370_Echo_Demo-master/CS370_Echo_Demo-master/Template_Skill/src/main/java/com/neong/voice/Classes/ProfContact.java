package com.neong.voice.Classes;
import java.util.*;
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
		/*
		Connection con = null;
		//Connect to DB in here, get prof contact info.

		try
		{

		Class.forName("com.mysql.jdbc.Driver");
		con = DriverManager.getConnection("jdbc:mysql://cwolf.cs.sonoma.edu:3306/restrella", "restrella", "abc123");
		Statement stmnt = con.createStatement();
		String sql = "SELECT email, phone FROM Professors WHERE (lName Like '" + name2 + "') OR (lName Like '%" + name2 + "%')";
		stmnt = con.createStatement();
		ResultSet rs = stmnt.executeQuery(sql);
		rs.next();
		String em = rs.getString("email");
		if(!rs.wasNull()){
			email = em.replace("@", " at ");
		}
		String ph = rs.getString("phone");
		if(!rs.wasNull()){
			phone = ph;
		}

		}
		catch (SQLException e)
	    {
	        phone = e.toString();
	    }
	    */
		
	}
}
