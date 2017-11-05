package com.mmatch.yanhui.segmentation;

import org.json.JSONException;
import org.json.JSONObject;

import com.bmwcarit.barefoot.roadmap.RoadMap;
import com.bmwcarit.barefoot.roadmap.RoadPoint;
import com.bmwcarit.barefoot.roadmap.Route;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.WktExportFlags;

public class Segment {
	private final RoadPoint start;
    private final RoadPoint end;
    private final Route route;
    private String id;
    
    public Segment(RoadPoint start,RoadPoint end,Route route){
    	if (route==null){
    		this.start=null;
    		this.end=null;
    		this.route=route;
    	}else{
    		this.start=start;
    		this.end=end;
    		this.route=route;
    	}
    }
    
    public Segment(Point start,Point end,Route route){  
    		
    	if (route==null){
    		this.start=null;
    		this.end=null;
    		this.route=route;
    	}else{
    		this.start=route.source();
    		this.end=route.target();
    		this.route=route;
    	}
    }
    
    public Segment(Route route, String id){
    	if (route==null){
    		this.start=null;
    		this.end=null;
    		this.route=route;
    	}else{
    		this.start=route.source();
    		this.end=route.target();
    		this.route=route;
    	}
    	this.id=id;
    }
    
    public Segment(Point start,Point end,Route route,String id ){  
		
    	if (route==null){
    		this.start=null;
    		this.end=null;
    		this.route=route;
    	}else{
    		this.start=route.source();
    		this.end=route.target();
    		this.route=route;
    	}
    	this.id=id;
    }
    
    public String id(){
    	return id;
    }
    
    public RoadPoint start() {
        return start;
    }
    
    public RoadPoint end() {
        return end;
    }
    
    public Route route() {
        return route;
    }
    
    public JSONObject toGeoJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", "LineString");
        JSONObject jsoncandidate = new JSONObject(GeometryEngine
                        .geometryToGeoJson(route().geometry()));
        json.put("coordinates", jsoncandidate.getJSONArray("coordinates"));
        
        return json;
    }
    
    public JSONObject toSlimJSON(String id) throws JSONException{
    	JSONObject json = new JSONObject();
    	json.put("id", id);
    	json.put("len",route.length() );
    	json.put("route",
                GeometryEngine.geometryToWkt(route().geometry(),
                        WktExportFlags.wktExportLineString));
    	return json;
    }
    
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("route",route.toJSON());
        return json;
    }
    
    public static Segment fromJSON(JSONObject json, RoadMap map) throws JSONException {
        JSONObject jsonroute=json.getJSONObject("route");
        String id=json.getString("id");
        
        Route route=Route.fromJSON(jsonroute, map);

        return new Segment(route,id);
    }
    
}
