package dblab.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ToMysql {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 final String DB_URL = "jdbc:mysql://msd16094s1.cs.hku.hk:3306/taxi?characterEncoding=utf8&useSSL=true";
	        final String USER = "root";
	        final String PASS = "1234";
	        
	        Connection conn = null;
	        Statement stmt = null;
	        
	        try{
	        	Class.forName("com.mysql.jdbc.Driver");
	        	System.out.println("连接数据库...");
	            conn = DriverManager.getConnection(DB_URL,USER,PASS);
	            
	            System.out.println(" 实例化Statement对...");
	            stmt = conn.createStatement();
	            String sql;
	            
	            //得到devID 的列表
	            sql = "SELECT distinct(DevID) FROM taxidata limit 1,10";
	            ResultSet rs = stmt.executeQuery(sql);
	            rs.close();    
	            
	            stmt.close();
	            conn.close();
	        }catch(SQLException se){
	        	se.printStackTrace();
	        }catch(Exception e){
	        	e.printStackTrace();
	        }finally{
	            // 关闭资源
	            try{
	                if(stmt!=null) stmt.close();
	            }catch(SQLException se2){
	            }// 什么都不做
	            try{
	                if(conn!=null) conn.close();
	            }catch(SQLException se){
	                se.printStackTrace();
	            }
	        }

	}

}
