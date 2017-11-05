package com.bmwcarit.barefoot.analysis;

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

import com.bmwcarit.barefoot.matcher.Matcher;
import com.bmwcarit.barefoot.matcher.MatcherKState;
import com.bmwcarit.barefoot.matcher.MatcherSample;
import com.bmwcarit.barefoot.matcher.Test;
import com.bmwcarit.barefoot.matcher.Test.InputFormatter;
import com.bmwcarit.barefoot.roadmap.RoadMap;
import com.bmwcarit.barefoot.util.SampleComparator;
import com.bmwcarit.barefoot.util.Tuple;
import com.esri.core.geometry.Point;
import com.mmatch.yanhui.segmentation.StopDetection;
import com.mmatch.yanhui.segmentation.TimeBasedSegmentation;
import com.mmatch.yanhui.stopdetection.StopCandidateSet;
import com.mmatch.yanhui.test.Initialize;

public class IncidentAnalysis {
	private final static Logger logger = LoggerFactory.getLogger(IncidentAnalysis.class);
	
	public static List<MatcherSample> match(List<Tuple<String,List<MatcherSample>>> samples_origin,Matcher matcher){
		TimeBasedSegmentation timeSeg=new TimeBasedSegmentation();
		List<MatcherSample> samples_matched=new ArrayList<>();
		
		for(Tuple<String,List<MatcherSample>> samples_tp:samples_origin){
        	List<MatcherSample> samples=samples_tp.two();
        	Collections.sort(samples, new SampleComparator());
        	List<List<MatcherSample>> trajectories=timeSeg.doSegmentation(samples);
        	for(List<MatcherSample> trajectory:trajectories){
        		MatcherKState state=matcher.mmatch(trajectory, 0, 1000);
        		for(int i=0;i<state.samples().size();i++){
        			MatcherSample item=state.samples().get(i);
        			item.setPoint(state.sequence().get(i).point().geometry());
        			samples_matched.add(item);
        		}
        	}   	
        }
		return samples_matched;
	}
	
	public static List<Tuple<String,List<MatcherSample>>> radius(List<Tuple<String,List<MatcherSample>>> samples_origin,Point c,double r){
		MatcherSampleIndex index;
		List<Tuple<String,List<MatcherSample>>> samples_filtered=new ArrayList<>();
		
		for(Tuple<String,List<MatcherSample>> samples:samples_origin){
        	index=new MatcherSampleIndex(samples.two());
        	index.construct();
        	@SuppressWarnings("unchecked")
        	List<MatcherSample> filtered=new ArrayList<>();
        	filtered.addAll(index.radius(c, r));
        	Collections.sort(filtered, new SampleComparator());
        	samples.two(filtered);
        	samples_filtered.add(samples);
        }
		return samples_filtered;
	}
	
}
