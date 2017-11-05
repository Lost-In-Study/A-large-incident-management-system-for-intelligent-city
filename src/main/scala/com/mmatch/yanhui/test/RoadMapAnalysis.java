package com.mmatch.yanhui.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bmwcarit.barefoot.matcher.Test;
import com.bmwcarit.barefoot.roadmap.Loader;
import com.bmwcarit.barefoot.roadmap.Road;
import com.bmwcarit.barefoot.roadmap.RoadMap;
import com.bmwcarit.barefoot.spatial.Geography;

public class RoadMapAnalysis {

	private final static Logger logger = LoggerFactory.getLogger(RoadMapAnalysis.class);
	
	public static void main(String[] args) {
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
		
		Geography spatialOperator=new Geography();
		int count=0;
		double length;
		Iterator<Road> edges=map.edges();
		while(edges.hasNext()){
			Road road=edges.next();
			length=spatialOperator.length(road.geometry());
			System.out.println(length);
			if(length>1000){
				
				count++;
			}
		}
		System.out.println("Roads segment>100:"+count);
	}

}
