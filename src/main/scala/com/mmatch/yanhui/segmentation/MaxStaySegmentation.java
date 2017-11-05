package com.mmatch.yanhui.segmentation;

import java.util.ArrayList;
import java.util.List;

import com.bmwcarit.barefoot.matcher.MatcherSample;

/**
 * Segment a trajectory if it stops for longer period than the maximum stay time.
 * @author Zhao Yanhui
 */
public class MaxStaySegmentation implements TrajectorySegmentation{
	public long getMaximumStayTime() {
        return maximumStayTime;
    }

    public void setMaximumStayTime(int maximumStayTime) {
        this.maximumStayTime = maximumStayTime;
    }
    
 // maximum stay period allowed in milliseconds (default value is 3 minutes)
    private long maximumStayTime = 180 * 1000;
    
    @Override
    public List<List<MatcherSample>> doSegmentation(List<MatcherSample> trajectory) {

        List<List<MatcherSample>> segmentedT = new ArrayList<>();
        MatcherSample lastPoint;
        //the index for first stopped point
        int stoppedIndex = 0;
        //the index for a new trajectory segment
        int startIndex = 0;
        long timeCount ;
        //a flag to indicate whether stopped point is detected or not
        boolean firstStoppedFlag = true;

        for(int i = 1; i < trajectory.size(); i++){

            lastPoint = trajectory.get(i - 1);
            if(SampleUtils.isEqual(lastPoint, trajectory.get(i))){
                if(firstStoppedFlag){
                    //record the first stopped point index
                    stoppedIndex = i;
                    firstStoppedFlag = false;
                }
            }else{
                if(!firstStoppedFlag){
                    //calculate the stopped time
                    timeCount = trajectory.get(i - 1).time() - trajectory.get(stoppedIndex - 1).time();
                    if(timeCount > this.maximumStayTime){
                        if((stoppedIndex - 1) - startIndex > 0){
                            List<MatcherSample> temp = trajectory.subList(startIndex, stoppedIndex - 1);
                            segmentedT.add(temp);
                        }
                        //start a new segment
                        startIndex = i;
                        stoppedIndex = 0;
                    }

                }
                firstStoppedFlag = true;
            }
        }

        // add last segment
        if(trajectory.size()  - startIndex > 1){
        	List<MatcherSample> temp = trajectory.subList(startIndex, trajectory.size() - 1);
            segmentedT.add(temp);
        }

        return segmentedT;
    }
}
