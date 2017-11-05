package com.mmatch.yanhui.segmentation;

import com.bmwcarit.barefoot.matcher.MatcherSample;

public class SampleUtils {
	public static boolean isEqual(MatcherSample a,MatcherSample b){
		return Math.abs(a.point().getX() - b.point().getX()) < 1e-4 && Math.abs(a.point().getY() - b.point().getY()) < 1e-4;
	}
}
