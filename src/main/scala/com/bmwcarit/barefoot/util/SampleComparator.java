package com.bmwcarit.barefoot.util;

import java.util.Comparator;

import com.bmwcarit.barefoot.matcher.MatcherSample;

public class SampleComparator implements Comparator<MatcherSample> {

	@Override
	public int compare(MatcherSample s0, MatcherSample s1) {
		// TODO Auto-generated method stub
		int result;
		int arg0=Integer.parseInt(s0.id());
		int arg1=Integer.parseInt(s1.id());
		
		if(arg0<arg1){result=-1;}
		else if(arg0==arg1){result=0;}
		else{result=1;}
		return result;
	}

}
