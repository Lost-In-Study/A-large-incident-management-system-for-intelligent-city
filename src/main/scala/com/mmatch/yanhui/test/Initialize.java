package com.mmatch.yanhui.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bmwcarit.barefoot.analysis.MatcherSampleIndex;
import com.bmwcarit.barefoot.matcher.Matcher;
import com.bmwcarit.barefoot.matcher.MatcherSample;
import com.bmwcarit.barefoot.matcher.Test;
import com.bmwcarit.barefoot.matcher.Test.InputFormatter;
import com.bmwcarit.barefoot.roadmap.Loader;
import com.bmwcarit.barefoot.roadmap.Road;
import com.bmwcarit.barefoot.roadmap.RoadMap;
import com.bmwcarit.barefoot.roadmap.RoadPoint;
import com.bmwcarit.barefoot.roadmap.Route;
import com.bmwcarit.barefoot.roadmap.TimePriority;
import com.bmwcarit.barefoot.spatial.Geography;
import com.bmwcarit.barefoot.topology.Dijkstra;
import com.esri.core.geometry.Envelope2D;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.WktExportFlags;

public class Initialize {
	private final static Logger logger = LoggerFactory.getLogger(Initialize.class);
	public static RoadMap initMap(){
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
		
		return map;
	}
	
	public static Matcher initMatcher(RoadMap map){
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
        
        return matcher;
	}
	
	public static void main(String[] args){
		RoadMap map=Initialize.initMap();
		
		/**
		 * test spatial operations
		 */
//		Geography spatial=new Geography();
//		Envelope2D env=spatial.envelope(new Point(114.1704, 22.319442), 1000);
//		System.out.println("Before zoom");
//		System.out.println("center: "+env.getCenterY()+","+env.getCenterX());
//		System.out.println("");
//		System.out.println(spatial.distance(env.getLowerLeft(), env.getUpperLeft()));
		
		
		/**
		 * test spatial search for road segments
		 */
//		 logger.info("start");
//		Set<Road> roads=map.spatial().coveredRoads(new Point(114.1704, 22.319442),1000);
//		 Set<RoadPoint> roads=map.spatial().knearest(new Point(114.1704, 22.319442), 100);
//		 Set<RoadPoint> roads=map.spatial().nearest(new Point(114.1704, 22.319442));
//		 logger.info("end");
//		 System.out.println("Returned num of roads: "+roads.size());
//		 for(Road road:roads){
//			 Point point=roadPoint.geometry();
//			 System.out.println("road id:"+road.id());
//			 String text=GeometryEngine.geometryToGeoJson(road.geometry());
//			 System.out.println(text.substring(35, text.length()-1)+",");
//			 
//			 System.out.print("("+point.getX()+","+point.getY()+"),");
//		 }
		
		/**
		 * test Dijkstra router
		 */
		Dijkstra<Road, RoadPoint> router=new Dijkstra<>();
		TimePriority cost=new TimePriority();
		
		Road sourceR=map.get(33518);
		double sourceFrac=0.2;
		RoadPoint source=new RoadPoint(sourceR,sourceFrac);
		System.out.println("source Point:"+source.geometry().getY()+","+source.geometry().getX());
		
		Road targetR=map.get(22174);
//		Road targetR=map.get(33522);
		double targetFrac=0.5;
		RoadPoint target=new RoadPoint(targetR,targetFrac);
		System.out.println("target Point:"+target.geometry().getY()+","+target.geometry().getX());
		
		logger.info("start route");
		List<Road> edges=router.route(source, target, cost);
		logger.info("end route");
		Route route=new Route(source,target,edges);
//		
		System.out.println("length(m): "+route.length());
//		String text=GeometryEngine.geometryToGeoJson(route.geometry());
//		System.out.println(text.substring(35, text.length()-1)+",");
		
		/**
		 * test MatcherSampleIndex.nearest(polyline,distance)
		 */
//		List<MatcherSample> samples=new LinkedList<>();
//        
//        final String DB_URL = "jdbc:mysql://localhost:3306/taxi?characterEncoding=utf8&useSSL=true";
//        final String USER = "root";
//        final String PASS = "yanhui";
//        
//        Connection conn = null;
//        Statement stmt = null;
//        try{
//        	Class.forName("com.mysql.jdbc.Driver");
//        	
//            conn = DriverManager.getConnection(DB_URL,USER,PASS);
//            
//            stmt = conn.createStatement();
//            String sql;
//            ResultSet rs;
//            
//            //GPS data of one vehicle on one day is a batch
//            String devID="001038";
//            String date="2010-01-01";
//            InputFormatter input=new InputFormatter();
//                    	
//            sql="SELECT PosID,HkDatetime,Lat,Lon FROM taxidata where DevID='"+devID+"'";
////            sql="SELECT PosID,HkDatetime,Lat,Lon FROM taxidata where DevID='"+devID+"' and Date(HkDatetime)='"+date+"'";
//            rs = stmt.executeQuery(sql);
//            samples=input.format(rs);
//            rs.close();
//            
//            MatcherSampleIndex operation=new MatcherSampleIndex(samples);
//            operation.construct();
//            Set<MatcherSample> results;
//            
//            logger.info("start");
//            results=operation.nearest(route.geometry(), 20);
//            logger.info("end");
//            System.out.println(results.size());
//            for(MatcherSample sample:results){
//            	System.out.println("["+sample.point().getY()+","+sample.point().getX()+"],");
//            }
//            stmt.close();
//            conn.close();
//        }catch(SQLException se){
//        	se.printStackTrace();
//        }catch(Exception e){
//        	e.printStackTrace();
//        }
		
		
		/**
		 * test components()
		 */
//		logger.info("start");
//		Set<Set<Road>> components=map.components();
//		logger.info("end");
//		System.out.println("total components: "+components.size());
//		
////		int i=1;
////		for(Set<Road> roads:components){
////			System.out.println(i+" :"+roads.size());
////			i++;
////			for(Road road:roads){
////				String text=GeometryEngine.geometryToGeoJson(road.geometry());
////				System.out.println(text.substring(35, text.length()-1)+",");
////			}
////		}
		 
		
	}
}
