package com.mmatch.yanhui.traveltime;

import java.util.List;

import com.bmwcarit.barefoot.matcher.MatcherSample;
import com.bmwcarit.barefoot.roadmap.Road;
import com.bmwcarit.barefoot.roadmap.RoadPoint;
import com.bmwcarit.barefoot.roadmap.Route;

public class TrajSegment extends Route{
	
	private String devID;
	
	public TrajSegment(RoadPoint source, RoadPoint target, List<Road> roads,String devID) {
        super(source, target, roads);
        this.devID=devID;
    }
	
	public TrajSegment(Route route, String devID) {
        this(route.source(), route.target(), route.path(),devID);
    }
	
	public String devID() {
        return this.devID;
    }

	
}
