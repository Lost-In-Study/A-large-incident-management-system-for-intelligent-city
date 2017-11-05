package com.mmatch.yanhui.segmentation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bmwcarit.barefoot.matcher.Matcher;
import com.bmwcarit.barefoot.roadmap.Distance;
import com.bmwcarit.barefoot.roadmap.Loader;
import com.bmwcarit.barefoot.roadmap.Road;
import com.bmwcarit.barefoot.roadmap.RoadMap;
import com.bmwcarit.barefoot.roadmap.RoadPoint;
import com.bmwcarit.barefoot.roadmap.Route;
import com.bmwcarit.barefoot.roadmap.TimePriority;
import com.bmwcarit.barefoot.spatial.Geography;
import com.bmwcarit.barefoot.spatial.SpatialOperator;
import com.bmwcarit.barefoot.topology.Cost;
import com.bmwcarit.barefoot.topology.Dijkstra;
import com.bmwcarit.barefoot.topology.Router;
import com.bmwcarit.barefoot.util.Triple;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;

public class RoadSegment {
	private static final Logger logger = LoggerFactory.getLogger(RoadSegment.class);
	
	private final RoadMap map;
	private final SpatialOperator spatial;
	private final Router<Road, RoadPoint> router;
	private final Cost<Road> cost;
	
	public RoadSegment(RoadMap map,SpatialOperator spatial,Router<Road, RoadPoint> router,Cost<Road> cost) {
        this.map = map;
        this.spatial = spatial;
        this.router=router;
        this.cost=cost;
    }
	
	public Route segment2points(Point source,Point target){
		return segment2points(point2road(source),point2road(target));
	}
	
	public Route segment2points(Set<RoadPoint> sources,Set<RoadPoint> targets){
		double length=Double.MAX_VALUE;
		Route minRoute=null;
		for(RoadPoint source:sources){
			Route route=segment2points(source,targets);
			if(route!=null){
				if(route.length()<length){
					minRoute=route;
					length=route.length();
				}	
			}
		}
		return minRoute;
	}
	
	public Route segment2points(RoadPoint source,Set<RoadPoint> targets){
		double length=Double.MAX_VALUE;
		Route minRoute=null;
		for(RoadPoint target:targets){
			List<Road> edges=router.route(source, target, cost);
			if(!(edges==null)){
				Route route=new Route(source,target,edges);
				if(route.length()<length){
					minRoute=route;
					length=route.length();
				}
			}			
		}
		return minRoute;
	}
	
	public Route segment2points(RoadPoint source,RoadPoint target){
		List<Road> edges=router.route(source, target, cost);
		Route route=new Route(source,target,edges);
		return route;
	}
	
	public Set<RoadPoint> point2road(Point point){
		return map.spatial().radius(point, 10);	
	}
	
	public List<Segment> getSegment(ResultSet rs){
		String id;
        double start_lat;
        double start_lon;
        double end_lat;
        double end_lon;
        List<Segment> segments=new ArrayList<>();
        
        try{
        	while(rs.next()){
        		id=rs.getString("id");
        		start_lat=rs.getDouble("start_lat");
        		start_lon=rs.getDouble("start_lon");
        		end_lat=rs.getDouble("end_lat");
        		end_lon=rs.getDouble("end_lon");
        		Point start=new Point(start_lon,start_lat);
        		Point end=new Point(end_lon,end_lat);
        		Route route=this.segment2points(start, end);
        		if(route!=null){
        			Segment roadSegment=new Segment(start,end,route,id);
        			segments.add(roadSegment);
        		}
        		else{
        			logger.info(id+":"+null); 
        		}
        	}
        }catch(SQLException se){
        	se.printStackTrace();
        }
        
        return segments;
	}
	
	
	
}
