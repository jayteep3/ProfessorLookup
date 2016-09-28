import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
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
	
	public static void GetEmailPhone()
	{
		//Connect to DB in here, get prof contact info.
		try{
		Connection con = null;
		Class.forName("com.mysql.jdbc.Driver");
		con = DriverManager.getConnection("jdbc:mysql://cwolf.cs.sonoma.edu:3306/restrella", "restrella", "abc123");
		
		}
		catch (SQLConnection e) {
			e.printStrackTrace();
			return;
		}
		if(connection != null) {
			Statement stmnt = con.createStatement();
			ResultSet rs =  stmnt.ExecuteQuery("SELECT email, phone FROM professors WHERE (name is Like '" + name + "')");
			email = rs.getString("email");
			phone = rs.getString("phone");
		}
		
	}
}
