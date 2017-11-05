package com.bmwcarit.barefoot.matcher;

import java.io.BufferedReader;
import java.io.File;
import java.sql.*;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bmwcarit.barefoot.markov.Filter;
import com.bmwcarit.barefoot.matcher.MatcherServer.OutputFormatter;
import com.bmwcarit.barefoot.road.Heading;
import com.bmwcarit.barefoot.roadmap.Loader;
import com.bmwcarit.barefoot.roadmap.Road;
import com.bmwcarit.barefoot.roadmap.RoadMap;
import com.bmwcarit.barefoot.roadmap.RoadPoint;
import com.bmwcarit.barefoot.roadmap.TimePriority;
import com.bmwcarit.barefoot.spatial.Geography;
import com.bmwcarit.barefoot.topology.Dijkstra;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.WktImportFlags;
import com.esri.core.geometry.Geometry.Type;

import com.mmatch.yanhui.segmentation.*;

public class Test {
	
	private final static Logger logger = LoggerFactory.getLogger(Test.class);
	
	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		String pathDatabaseProperties="config\\hongkong.properties";
		Properties databaseProperties = new Properties();
		try{
			databaseProperties.load(new FileInputStream(pathDatabaseProperties));
		}catch(FileNotFoundException e){
			System.exit(1);
		}catch (IOException e){
            System.exit(1);
		}
		
		RoadMap map=Loader.roadmap(databaseProperties, true);
		map.construct();
		
		String pathServerProperties="config\\server.properties";
		Properties serverProperties = new Properties();
		try{
			serverProperties.load(new FileInputStream(pathServerProperties));
		}catch(FileNotFoundException e){
			System.exit(1);
		}catch (IOException e){
            System.exit(1);
		}
		Matcher matcher=new Matcher(map,new Dijkstra<Road,RoadPoint>(),new TimePriority(),new Geography());
		matcher.setMaxRadius(Double.parseDouble(serverProperties.getProperty("matcher.radius.max",
                Double.toString(matcher.getMaxRadius()))));
        matcher.setMaxDistance(Double.parseDouble(serverProperties.getProperty("matcher.distance.max",
                Double.toString(matcher.getMaxDistance()))));
        matcher.setLambda(Double.parseDouble(serverProperties.getProperty("matcher.lambda",
                Double.toString(matcher.getLambda()))));
        matcher.setSigma(Double.parseDouble(
        		serverProperties.getProperty("matcher.sigma", Double.toString(matcher.getSigma()))));
        int interval;
        double distance;
        interval = Integer.parseInt(serverProperties.getProperty("matcher.interval.min", "1000"));
        distance = Integer.parseInt(serverProperties.getProperty("matcher.distance.min", "0"));
		
        List<MatcherSample> samples=new LinkedList<>();
//        MaxStaySegmentation maxStaySegmentation=new MaxStaySegmentation();
        TimeBasedSegmentation timeBasedSegmentation=new TimeBasedSegmentation();
        
//        samples=new Test().readInput("E:/Dissertation/data/Taxidata/taxi001038.json");
//        MatcherKState state;
//        state=matcher.mmatch(samples, distance, interval); 
//		DebugJSONOutputFormatter output=new Test.DebugJSONOutputFormatter();
//		String result=output.format("1", state);
//		System.out.println(result);
        
        
		/*
		 * read taxi data from mysql data base 
		 * A batch of data is the GPS data for one day from one vehicle
		 * */
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
            
            
            sql = "SELECT distinct(DevID) FROM taxidata limit 1,10";
            ResultSet rs = stmt.executeQuery(sql);
            logger.info("get DevIDs list");
            ArrayList<String> devIDList = new ArrayList<>();
            while(rs.next()){
            	devIDList.add(rs.getString("DevID"));
            }
            rs.close();
            
            
            sql = "SELECT distinct(Date(HkDatetime)) FROM taxidata";
            rs = stmt.executeQuery(sql);
            logger.info("get Date list");
            ArrayList<String> dateList = new ArrayList<>();
            while(rs.next()){
            	System.out.println(rs.getString(1));
            	dateList.add(rs.getString(1));
            }
            rs.close();
            
            //GPS data of one vehicle on one day is a batch
            InputFormatter input=new InputFormatter();
            MatcherKState state;
            for(String date:dateList){
            	for(String devID:devIDList){
            		sql="SELECT PosID,HkDatetime,Lat,Lon FROM taxidata where DevID='"+devID+"' and Date(HkDatetime)='"+date+"'";
            		rs = stmt.executeQuery(sql);
            		samples=input.format(rs);
            		rs.close();
            		List<List<MatcherSample>> segmentedT=timeBasedSegmentation.doSegmentation(samples);
            		logger.info("Trj of "+devID+" on "+date+" is splited to "+segmentedT.size()+" segments");
            		for(List<MatcherSample> sub:segmentedT){
            			logger.info("size: "+sub.size());
            		}
            		
            		
//            		File dir=new File("E:/Dissertation/data/matched/"+date+"/"+devID);
//            		if(!dir.exists()){
//            			dir.mkdirs();
//            			logger.info("create folder:"+"E:/Dissertation/data/matched/"+date+"/"+devID);
//            		}
            		int sum=0;
            		for(int i=0; i<segmentedT.size();i++){
            			List<MatcherSample> subTraj=segmentedT.get(i);
            			state=matcher.mmatch(subTraj, distance, interval); 
            			for(int j=0;j<state.sequence().size();j++){
            				Test.insert(state.sequence().get(j),state.samples().get(j), conn);
            			}
////            			DebugJSONOutputFormatter output=new Test.DebugJSONOutputFormatter();
////            			String result=output.format("1", state);
////            			PrintWriter writer=new PrintWriter("E:/Dissertation/data/matched/"+date+"/"+devID+"/"+i+".json");
////            			writer.print(result);
////            			writer.close();
            			sum+=subTraj.size();
        			}
            		logger.info("Complete map matching of "+devID+" on "+date+" having "+sum+" points"); 
            	}
            }
            stmt.close();
            conn.close();
        }catch(SQLException se){
        	se.printStackTrace();
        }catch(Exception e){
        	e.printStackTrace();
        }finally{
       
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
		
	}
	
	public List<MatcherSample> readInput(String filePath){
		String jsonText=new String();
		try{
			BufferedReader reader =
                    new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
			jsonText=reader.readLine();
			reader.close();
					
		}catch(IOException e){
			e.printStackTrace();
		}
		
		Test.InputFormatter input=new Test.InputFormatter();
		return input.format(jsonText);
		
	}
	public static void insert(MatcherCandidate candidate,MatcherSample sample,Connection conn){
		try{
			int PosID=Integer.parseInt(sample.id());
			Long gid=candidate.point().edge().base().id();
			Long osm_id=candidate.point().edge().base().refid();
			double fraction=candidate.point().fraction();
			int heading=candidate.point().edge().heading()==Heading.forward ? 1 : 0;
			
			String sql="insert into matched(PosID,gid,osm_id,fraction,heading) values(?,?,?,?,?)";
			PreparedStatement ps=conn.prepareStatement(sql);
			ps.setInt(1, PosID);
			ps.setLong(2, gid);
			ps.setLong(3,osm_id);
			ps.setDouble(4, fraction);
			ps.setInt(5,heading);
			
			ps.executeLargeUpdate();
			ps.close();
			
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	public static class InputFormatter {
        /**
         * Converts a request message into a list of {@link MatcherSample} objects as input for map
         * matching.
         *
         * @param input JSON input format of sample data.
         * @return List of {@link MatcherSample} objects.
         */
        public List<MatcherSample> format(String input) {
            List<MatcherSample> samples = new LinkedList<>();

            try {
                Object jsoninput = new JSONTokener(input).nextValue();
                JSONArray jsonsamples = null;

                if (jsoninput instanceof JSONObject) {
                    jsonsamples = ((JSONObject) jsoninput).getJSONArray("request");
                } else {
                    jsonsamples = ((JSONArray) jsoninput);
                }

                Set<Long> times = new HashSet<>();
                for (int i = 0; i < jsonsamples.length(); ++i) {
                    MatcherSample sample = new MatcherSample(jsonsamples.getJSONObject(i));
                    samples.add(sample);
                    if (times.contains(sample.time())) {
                        throw new RuntimeException("multiple samples for same time");
                    } else {
                        times.add(sample.time());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                throw new RuntimeException("parsing JSON request: " + e.getMessage());
            }

            return samples;
        }
        
        public boolean hasColumn(ResultSet rs,String columnName){
        	try {  
                if (rs.findColumn(columnName) > 0 ) {  
                    return true;  
                }   
            }  
            catch (SQLException e) {  
                return false;  
            }  
              
            return false;  
        }
        
        public List<MatcherSample> format(ResultSet input) {
            List<MatcherSample> samples = new LinkedList<>();

            try {
                Set<Long> times = new HashSet<>();
                while (input.next()) {
                	String id=input.getString("PosID");
                	Long time=input.getTimestamp("HkDatetime").getTime();
                	
                	double longitude=input.getDouble("Lon");
                	double latitude=input.getDouble("Lat");
                	
                    Point point = new Point(longitude,latitude);
                    MatcherSample sample;
                    if(hasColumn(input,"SpeedKmHr")){
                    	double speed=input.getDouble("SpeedKmHr");
                    	sample = new MatcherSample(id,time,point,speed);
                    	samples.add(sample);
                    }
                    else{
                    	sample = new MatcherSample(id,time,point);
                        samples.add(sample);
                    }
                    
                    if (times.contains(sample.time())) {
                        throw new RuntimeException("multiple samples for same time");
                    } else {
                        times.add(sample.time());
                    }
                }
            } catch(SQLException se){
            	se.printStackTrace();
            }

            return samples;
        }
    
        public List<MatcherSample> format_noSequence(ResultSet input) {
            List<MatcherSample> samples = new LinkedList<>();

            try {
                while (input.next()) {
                	String id=input.getString("PosID");
                	Long time=input.getTimestamp("HkDatetime").getTime();
                	
                	double longitude=input.getDouble("Lon");
                	double latitude=input.getDouble("Lat");
                	
                    Point point = new Point(longitude,latitude);
                    MatcherSample sample;
                    if(hasColumn(input,"SpeedKmHr")){
                    	double speed=input.getDouble("SpeedKmHr");
                    	sample = new MatcherSample(id,time,point,speed);
                    	samples.add(sample);
                    }
                    else{
                    	sample = new MatcherSample(id,time,point);
                        samples.add(sample);
                    }                 
                }
            } catch(SQLException se){
            	se.printStackTrace();
            }

            return samples;
        }
	}

	public static class GeoJSONOutputFormatter extends OutputFormatter {
        @Override
        public String format(String request, MatcherKState output) {
            try {
                return output.toGeoJSON().toString();
            } catch (JSONException e) {
                throw new RuntimeException("creating JSON response");
            }
        }
    }
	
	public static class DebugJSONOutputFormatter extends OutputFormatter {
        @Override
        public String format(String request, MatcherKState output) {
            try {
                return output.toDebugJSON();
            } catch (JSONException e) {
                throw new RuntimeException("creating JSON response: " + e.getMessage());
            }
        }
    }
	public static class SlimJSONOutputFormatter extends OutputFormatter {
        @Override
        public String format(String request, MatcherKState output) {
            try {
                return output.toSlimJSON().toString();
            } catch (JSONException e) {
                throw new RuntimeException("creating JSON response");
            }
        }
    }
	
    public static String format(String request, MatcherKState output,RoadMap map) {
        try {
                return output.toRoadJSON(map).toString();
            } catch (JSONException e) {
                throw new RuntimeException("creating JSON response");
       }
   }
}
