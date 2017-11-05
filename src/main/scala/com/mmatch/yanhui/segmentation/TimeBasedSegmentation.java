package com.mmatch.yanhui.segmentation;

import java.util.ArrayList;
import java.util.List;

import com.bmwcarit.barefoot.matcher.MatcherSample;

public class TimeBasedSegmentation implements TrajectorySegmentation {
	public long getIntervalTime() {
        return maximumIntervalTime;
    }

    public void setIntervalTime(int maximumStayTime) {
        this.maximumIntervalTime = maximumStayTime;
    }
    
 // maximum stay period allowed in milliseconds (default value is 3 minutes)
    private long maximumIntervalTime = 300 * 1000;
    
    @Override
    public List<List<MatcherSample>> doSegmentation(List<MatcherSample> trajectory) {

        List<List<MatcherSample>> segmentedT = new ArrayList<>();
        int startIndex = 0;
        int endIndex=0;

        for(int i = 1; i < trajectory.size(); i++){
        	if(trajectory.get(i).time()-trajectory.get(i-1).time()>maximumIntervalTime){
        		endIndex=i;
        		List<MatcherSample> temp = trajectory.subList(startIndex, endIndex);
        		segmentedT.add(temp);
        		startIndex=i;
        		endIndex=i;
        	}        
        }

        // add last segment
        if(trajectory.size()  - startIndex > 1){
        	List<MatcherSample> temp = trajectory.subList(startIndex, trajectory.size());
            segmentedT.add(temp);
        }

        return segmentedT;
    }
}
