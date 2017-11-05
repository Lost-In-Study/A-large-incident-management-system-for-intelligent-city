package com.bmwcarit.barefoot.analysis;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bmwcarit.barefoot.matcher.MatcherSample;
import com.bmwcarit.barefoot.matcher.Test.InputFormatter;
import com.bmwcarit.barefoot.spatial.PointsIndex;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.mmatch.yanhui.segmentation.StopDetection;
import com.mmatch.yanhui.stopdetection.StopCandidateSet;

public class MatcherSampleIndex implements Serializable {
	private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(MatcherSampleIndex.class);
    
    private final PointsIndex index=new PointsIndex();
    private List<MatcherSample> samples;
    
    public MatcherSampleIndex(List<MatcherSample> samples){
    	this.samples=samples;
    }
    
    public void put(int id,MatcherSample sample) {

        if (index.contains(id)) {
            return;
        }

        index.add(id, sample.point());
    }
    
    public void clear() {
        index.clear();
    }
    
    public Set<MatcherSample> nearest(Point c) {
    	Set<MatcherSample> neighbors = new HashSet<>();
    	Set<Integer> ids=index.nearest(c);
    	for(int id:ids){
    		neighbors.add(samples.get(id));
    	}
    	return neighbors;
    }
    
    public Set<MatcherSample> radius(Point c, double r) {
        
    	Set<MatcherSample> neighbors = new HashSet<>();
    	Set<Integer> ids=index.radius(c, r);
    	
    	for(int id:ids){
    		neighbors.add(samples.get(id));
    	}
    	return neighbors;
    }
    
    public Set<MatcherSample> nearest(Polyline polyline, double distance){
    	Set<MatcherSample> neighbors = new HashSet<>();
    	Set<Integer> ids=index.nearest(polyline, distance);
    	for(int id:ids){
    		neighbors.add(samples.get(id));
    	}
    	return neighbors;
    }
    
    public MatcherSampleIndex construct(){
    	long memory = 0;
    	
    	System.gc();
        memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        logger.info("index constructing ...");
        
        for(int i=0;i<samples.size();i++){
        	put(i,samples.get(i));
        }
        
        logger.info("index constructed");
        
        System.gc();
        memory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) - memory;
        logger.info("~{} megabytes used for spatial index (estimate)",
                Math.max(0, Math.round(memory / 1E6)));
        
        return this;
    }
    
    public static void main(String[] args){
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
                    	
            sql="SELECT PosID,HkDatetime,Lat,Lon FROM taxidata limit 100000";
//            sql="SELECT PosID,HkDatetime,Lat,Lon FROM taxidata where DevID='"+devID+"' and Date(HkDatetime)='"+date+"'";
            rs = stmt.executeQuery(sql);
            samples=input.format_noSequence(rs);
            rs.close();
            
            MatcherSampleIndex operation=new MatcherSampleIndex(samples);
            operation.construct();
            Set<MatcherSample> results;
            logger.info("start 100");
            results=operation.radius(new Point(114.1704, 22.319442),100);
            logger.info("end");
            System.out.println("returned num: "+results.size());
            
            logger.info("start 500");
            results=operation.radius(new Point(114.1704, 22.319442),500);
            logger.info("end");
            System.out.println("returned num: "+results.size());
            
            logger.info("start 1000");
            results=operation.radius(new Point(114.1704, 22.319442),1000);
            logger.info("end");
            System.out.println("returned num: "+results.size());
            
//            for(MatcherSample sample:results){
//            	System.out.println(sample.point().getY()+","+sample.point().getX());
//            }
    		

            stmt.close();
            conn.close();
        }catch(SQLException se){
        	se.printStackTrace();
        }catch(Exception e){
        	e.printStackTrace();
        }
	}
    
}
