package com.mmatch.yanhui.test;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.bmwcarit.barefoot.analysis.DBSCAN;
import com.bmwcarit.barefoot.matcher.MatcherSample;
import com.bmwcarit.barefoot.matcher.Test;
import com.bmwcarit.barefoot.matcher.Test.InputFormatter;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPoint;
import com.esri.core.geometry.Point;
import com.mmatch.yanhui.segmentation.MaxStaySegmentation;
import com.mmatch.yanhui.segmentation.StopDetection;
import com.mmatch.yanhui.stopdetection.StopCandidateSet;

public class SpatialAnalysis {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		List<MatcherSample> samples=new LinkedList<>();
        
        final String DB_URL = "jdbc:mysql://localhost:3306/taxi?characterEncoding=utf8&useSSL=true";
        final String USER = "root";
        final String PASS = "yanhui";
        
        Connection conn = null;
        Statement stmt = null;
        try{
        	Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            
            stmt = conn.createStatement();
            String sql;
            ResultSet rs;
            
            //GPS data of one vehicle on one day is a batch
            String devID="001038";
            String date="2010-01-01";
            InputFormatter input=new InputFormatter();
                    	
//            sql="SELECT PosID,HkDatetime,Lat,Lon FROM taxidata where DevID='"+devID+"' and Date(HkDatetime)='"+date+"'"+" and time(HkDatetime) between "+"'13:00:00'" +" and "+"'14:00:00'";
            sql="SELECT PosID,HkDatetime,Lat,Lon FROM taxidata where DevID='"+devID+"' and Date(HkDatetime)='"+date+"'";
            rs = stmt.executeQuery(sql);
            samples=input.format(rs);
            rs.close();
            
            StopDetection stopDetection=new StopDetection();
            stopDetection.setMaximumStayTime(300*1000);
            
            
            //List<List<Integer>> stops: stop candidates' index in samples
    		List<List<Integer>> stops=stopDetection.getStops(samples, 50, 1);
    		
            System.out.println(stops.size());
    		
    		List<MatcherSample> candidateSampleSet=new LinkedList<>();
    		List<StopCandidateSet> candidateSets=new LinkedList<>();
    		List<Integer> item;
//    		PrintWriter writer=new PrintWriter("E:/Dissertation/data/stopDetection/"+date+"-"+devID+".json");
//    		for(int i=0;i<stops.size();i++){
//    			item=stops.get(i);
//    			System.out.println(i+1+": "+item.size()); 
//    			Timestamp start=new Timestamp(samples.get(item.get(0)).time());
//    			Timestamp end=new Timestamp(samples.get(item.get(item.size()-1)).time());
//    			System.out.println(start.toString());
//    			System.out.println(end.toString());
//    			
//    			candidateSampleSet.clear();
//    			for(int index:item){
//    				candidateSampleSet.add(samples.get(index));
//    			}
//    			StopCandidateSet candidateSet=new StopCandidateSet(candidateSampleSet);
//    			candidateSet.setTrajectoryIdentifier(devID, i+1);
//    			System.out.println(candidateSet.toJSON().toString());
//    			
//    			writer.println(candidateSet.toJSON().toString());
//    						
//    			candidateSets.add(candidateSet);	
//    		}
//    		writer.close(); 
    		
    		//update sample state in table matched
    		conn.prepareStatement("update matched set state=0").executeUpdate();
    		MatcherSample sample;
    		List<Integer> cluster;
    		sql="update matched set state=? where PosID=?";
    		PreparedStatement pst = conn.prepareStatement(sql);
    		for(int i=0;i<stops.size();i++){
    			cluster=stops.get(i);
    			for(int index:cluster){
    				sample=samples.get(index);
    				pst.setInt(1, i+1);
    				pst.setInt(2, Integer.parseInt(sample.id()));
    				pst.executeUpdate();
          		}
    		}
            

            stmt.close();
            conn.close();
        }catch(SQLException se){
        	se.printStackTrace();
        }catch(Exception e){
        	e.printStackTrace();
        }
	}

}
