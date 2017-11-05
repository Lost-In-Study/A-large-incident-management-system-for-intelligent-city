package com.mmatch.yanhui.stopdetection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bmwcarit.barefoot.matcher.Matcher;
import com.bmwcarit.barefoot.matcher.MatcherCandidate;
import com.bmwcarit.barefoot.matcher.MatcherKState;
import com.bmwcarit.barefoot.matcher.MatcherSample;
import com.bmwcarit.barefoot.roadmap.RoadMap;
import com.bmwcarit.barefoot.spatial.Geography;
import com.esri.core.geometry.Envelope2D;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.mmatch.yanhui.test.Initialize;

public class StopCandidateSet {
	
	private List<MatcherSample> samples=new LinkedList<>();
	private Boolean stopped=false;
	private String devID="";
	private int trajectoryIndex=-1;
	private double routeLength=0;
	
	public StopCandidateSet(List<MatcherSample> samples){
		this.samples=samples;
	}
	
	public Boolean isStopped(){
		return stopped;
	}
	
	public void setState(Boolean i){
		stopped=i;
	}
	
	public void setTrajectoryIdentifier(String devID,int trajectoryIndex){
		this.devID=devID;
		this.trajectoryIndex=trajectoryIndex;
	}
	
	public String devID(){
		return devID;
	}
	
	public int trajectoryIndex(){
		return trajectoryIndex;
	}
 	
	public List<MatcherSample> samples(){
		return samples;
	}
	
	/**
	 * the area of the rectangle that than include all points
	 * @return area in m^2
	 */
	public double MBRc(){
		List<Point> points=new LinkedList<>();
		Geography spatial=new Geography();
		
		for(MatcherSample sample:samples){
			points.add(sample.point());
		}
		return spatial.area(points);		
	}
	
	/**
	 * density per point occupy
	 * MBRc/size
	 * @return
	 */
	public double Density(){
		return MBRc()/samples.size();
	}
	
	/**
	 * map GPS points to road segment
	 * calculate route length after map matching
	 * @param matcher
	 */
	public void MapMatch(Matcher matcher){
		MatcherKState state;
		double total=0;
		state=matcher.mmatch(samples, 0, 1000);
		
		if (state.sequence() != null) {
            for (MatcherCandidate candidate : state.sequence()) {
                if (candidate.transition() == null) {
                    continue;
                }
                total+=candidate.transition().route().length();
            }
        }
		routeLength=total;
		
	}
	/**
	 * match samples to road and calculate route
	 * @param matcher
	 * @return route distance in meters
	 */
 	public double routeLength(){
		return routeLength;
	}
	
	/**
     * average speed in km/h
     */
	public double avgSpeed(){
		double len=routeLength/1000;
		double duration=(double)duration()/(1000*60*60);
		return len/duration;
	}
	
	/**
     * average lenth of one transition in meters
     */
	public double avgRouteLength(){
		return routeLength/this.samples.size();
	}
	
	public int size(){
		return samples.size();
	}
	
	/**
	 * duration time in milliseconds
	 * @return
	 */
 	public long duration(){
		return samples.get(samples.size()-1).time()-samples.get(0).time();
	}
	
 	public double duration_min(){
 		Long milliseconds=samples.get(samples.size()-1).time()-samples.get(0).time();
 		return (double)milliseconds/(1000*60);
 	}
 	
	public JSONObject toJSON() throws JSONException{
		JSONObject json = new JSONObject();
        json.put("state", stopped);
        json.put("devID", devID);
        json.put("index", trajectoryIndex);
        
        JSONArray jsonsequence = new JSONArray();
        for(MatcherSample sample:samples){
        	jsonsequence.put(sample.toJSON());
        }
        json.put("samples", jsonsequence);
        
        return json;
	}
	
	public static StopCandidateSet fromJSON(JSONObject json) throws JSONException{

		Boolean state=json.getBoolean("state");
		String devID=json.getString("devID");
		int index=json.getInt("index");
		
		List<MatcherSample> samples=new LinkedList<>();
		JSONArray jsonsequence=json.getJSONArray("samples");
		for(int i=0;i<jsonsequence.length();i++){
			samples.add(new MatcherSample(jsonsequence.getJSONObject(i)));
		}
		
		StopCandidateSet candidateSet=new StopCandidateSet(samples);
		candidateSet.setState(state);
		candidateSet.setTrajectoryIdentifier(devID, index);
		
		return candidateSet;
		
	}

	public static void main(String[] args){
		RoadMap map=Initialize.initMap();
		Matcher matcher=Initialize.initMatcher(map);
		List<StopCandidateSet> candidateSets=new ArrayList<>();
		
		BufferedReader brname;
		String jsonText;
		JSONObject json;
		try{
			brname=new BufferedReader(new FileReader("E:/Dissertation/data/stopDetection/2010-01-01-001038.json"));
			while((jsonText=brname.readLine()) != null){
				json=new JSONObject(jsonText);
				StopCandidateSet candidateSet=StopCandidateSet.fromJSON(json);
				candidateSets.add(candidateSet);
			}
			brname.close();
			
			for(StopCandidateSet candidateSet: candidateSets){
				candidateSet.MapMatch(matcher);
			
//				System.out.println(candidateSet.toJSON().toString());
				System.out.println("Index: "+candidateSet.trajectoryIndex());
				System.out.println("point number: "+candidateSet.size());
				System.out.println("duration(min): "+candidateSet.duration_min());
				System.out.println("MBRc(m^2): "+candidateSet.MBRc());
				System.out.println("Density(m^2): "+candidateSet.Density());
				System.out.println("route length(m): "+candidateSet.routeLength());
				System.out.println("average speed(km/h): "+candidateSet.avgSpeed());
				System.out.println();
			}
			
		}catch(IOException e){
			e.printStackTrace();
		}catch(JSONException e){
			e.printStackTrace();
		}		
		
	}
}
