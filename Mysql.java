package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
public class  Mysql
{
	static String url="jdbc:mysql://127.0.0.1/internetspider?useUnicode=true&characterEncoding=utf-8&useSSL=false";;
	static String sql;
	static String linktype="com.mysql.jdbc.Driver";
	static String username="root";
	static String password="123456";
	static Connection con=null;
	public static PreparedStatement ps=null;
	public Mysql(){
		try
		{
			Class.forName(linktype);
			con=DriverManager.getConnection(url,username,password);
		}
		catch (Exception e)
		{
		} 
	}
	 

	public void setSQL(String s){
		this.sql=s;
	}
	public void setURL(String s){
		this.url="jdbc:mysql://127.0.0.1/"+s+"?useUnicode=true&characterEncoding=utf-8&useSSL=false";
	}
	public void MysqlRun(){
		try{
			//指定类型，获取连接，准备执行语句
			ps=con.prepareStatement(sql);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void close(){
		try{
			//this.con.close();
			this.ps.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}
}
