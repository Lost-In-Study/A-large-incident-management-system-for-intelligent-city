package com.mmatch.yanhui.segmentation;

import java.util.List;

import com.bmwcarit.barefoot.matcher.MatcherSample;

/**
 * A common interface for segmenting a long trajectory into multiple short, more meaningful trajectories.
 * @author Zhao Yanhui
 */
public interface TrajectorySegmentation {
	List<List<MatcherSample>> doSegmentation(List<MatcherSample> trajectory);
}
