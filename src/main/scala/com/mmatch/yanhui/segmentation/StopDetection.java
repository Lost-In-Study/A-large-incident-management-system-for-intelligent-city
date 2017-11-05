package com.mmatch.yanhui.segmentation;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.sql.Timestamp;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bmwcarit.barefoot.analysis.DBSCAN;
import com.bmwcarit.barefoot.analysis.DBSCAN_MatcherSample;
import com.bmwcarit.barefoot.matcher.MatcherKState;
import com.bmwcarit.barefoot.matcher.MatcherSample;
import com.bmwcarit.barefoot.matcher.Test;
import com.bmwcarit.barefoot.matcher.Test.DebugJSONOutputFormatter;
import com.bmwcarit.barefoot.matcher.Test.InputFormatter;
import com.bmwcarit.barefoot.spatial.Geography;
import com.esri.core.geometry.Point;

public class StopDetection {
	
	private final static Logger logger = LoggerFactory.getLogger(StopDetection.class);
	
	public List<int[]> getClusterIndex(List<MatcherSample> samples,int radius, int density){
        
//		 DBSCAN algorithm with radius neighborhood of 100 and minimum density of 10
		Set<List<MatcherSample>> clusters = DBSCAN_MatcherSample.cluster(samples, radius, density);

		List<int[]> clusterList=new LinkedList<int[]>();
		for (List<MatcherSample> cluster : clusters) {
			int[] clusterIndexes=new int[cluster.size()];
			int i=0;
			for (MatcherSample sample : cluster) {
				clusterIndexes[i]=samples.indexOf(sample);
				i++;
			}
			Arrays.sort(clusterIndexes);
			clusterList.add(clusterIndexes);
		}
		return clusterList;
	}
	
	public long getMaximumStayTime() {
        return maximumStayTime;
    }

    public void setMaximumStayTime(int maximumStayTime) {
        this.maximumStayTime = maximumStayTime;
    }
    
 // maximum stay period allowed in milliseconds (default value is 3 minutes)
    private long maximumStayTime = 180 * 10000;
    
	public List<List<Integer>> getStops(List<MatcherSample> samples,int radius, int density){
		List<int[]> clustersIndex=getClusterIndex(samples,radius, density);
		
		List<List<Integer>> stops=new ArrayList<>();
		for(int[] cluster : clustersIndex){
			List<int[]> stopRanges=subSeqRange(cluster);
			for(int[] seq : stopRanges){
				if(samples.get(seq[1]).time()-samples.get(seq[0]).time()>=maximumStayTime){
					List<Integer> substop=new ArrayList<>();
					for(int i=seq[0];i<seq[1]+1;i++){
						substop.add(i);
					}
					stops.add(substop);
					
				}
			}		
		}
//		stops.sort(null);
		return StopDetection.sort(stops);
	}
	
//	public stopRangeList()
	
	
	public int[] maxSequences(int[] a){
		int size=a.length;
		int[] p=new int[size];
		for(int i=0;i<size;i++){
			p[i]=1;
		}
		
		for(int i=1;i<size;i++){
			if(a[i]-a[i-1]==1){
				p[i]+=p[i-1];
			}
		}
		return p;
	}
	
	public List<int[]> subSeqRange(int[] a){
		int[] p=maxSequences(a);
		List<int[]> subSeqs=new ArrayList<>();
		for(int i=1;i<p.length;i++){
			if(p[i]-p[i-1]!=1){
				if(p[i-1]>1){
					int[] seq=new int[2];
					seq[0]=a[i-1-p[i-1]+1];
					seq[1]=a[i-1];
					subSeqs.add(seq);
				}
			}
		}
		
		if(p[p.length-1]>1){
			int i=p.length;
			int[] seq=new int[2];
			seq[0]=a[i-1-p[i-1]+1];
			seq[1]=a[i-1];
			subSeqs.add(seq);
		}
		return subSeqs;
	}
	
	public static List<List<Integer>> sort(List<List<Integer>> clusters){
		List<List<Integer>> sorted=new ArrayList<>();
		for(List<Integer> cluster:clusters){
			if(sorted.size()==0){
				sorted.add(cluster);
			}
			else{
				for(int j=0;j<sorted.size();j++){
					if(j==sorted.size()-1 && cluster.get(0)>sorted.get(j).get(0)){
						sorted.add(cluster);
						break;
					}
					else{
						if(cluster.get(0)<sorted.get(j).get(0) ){
							sorted.add(j, cluster);
							break;
						}
					}
				}
			}
		}
		
		return sorted;
	}
	
	public static void main(String[] args) throws JSONException {
		
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
            String devID="001040";
            String date="2010-01-01";
            InputFormatter input=new InputFormatter();
                    	
//            sql="SELECT PosID,HkDatetime,Lat,Lon FROM taxidata where DevID='"+devID+"' and Date(HkDatetime)='"+date+"'"+" and time(HkDatetime) between "+"'13:00:00'" +" and "+"'14:00:00'";
            sql="SELECT PosID,HkDatetime,Lat,Lon FROM taxidata where DevID='"+devID+"' and Date(HkDatetime)='"+date+"'";
            rs = stmt.executeQuery(sql);
            samples=input.format(rs);
            rs.close();
            
            StopDetection stopDetection=new StopDetection();
            stopDetection.setMaximumStayTime(180*1000);
            
//            List<int[]> clusters=stopDetection.getClusterIndex(samples, 150, 4);
//            int clustersize=clusters.size();
//            int[] cluster;
//            MatcherSample sample;
//            sql="update matched set state=? where PosID=?";
//            PreparedStatement pst = conn.prepareStatement(sql);
//            for(int i=0;i<clustersize;i++){
//            	cluster=clusters.get(i);
//            	for(int index:cluster){
//            		sample=samples.get(index);
//            		pst.setInt(1, i);
//            		pst.setInt(2, Integer.parseInt(sample.id()));
//            		pst.executeUpdate();
//            	}
//            }
            
            //List<List<Integer>> stops: stop candidates' index in samples
    		List<List<Integer>> stops=stopDetection.getStops(samples, 100, 1);
    		System.out.println(stops.toString());
    		for(List<Integer> item : stops){
    			System.out.println(item.toString());
    			Timestamp start=new Timestamp(samples.get(item.get(0)).time());
    			Timestamp end=new Timestamp(samples.get(item.get(item.size()-1)).time());
    			System.out.println(start.toString());
    			System.out.println(end.toString());
    		}
    		
    		//
//            

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
        
//        Set<List<MatcherSample>> clusters=DBSCAN_MatcherSample.cluster(samples, 100, 10);
        
//        Geography spatial=new Geography();
//        for(int i=1;i<100;i++){
//        	double distance=spatial.distance(samples.get(i-1).point(), samples.get(i).point());
//        	System.out.println(distance);
//        }
        
        
	}
}
