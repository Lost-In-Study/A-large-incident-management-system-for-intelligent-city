package com.bmwcarit.barefoot.analysis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.bmwcarit.barefoot.analysis.DBCAN.ISearchIndex;
import com.bmwcarit.barefoot.analysis.DBSCAN.SearchIndex;
import com.bmwcarit.barefoot.spatial.Geography;
import com.bmwcarit.barefoot.spatial.SpatialOperator;
import com.bmwcarit.barefoot.matcher.MatcherSample;
import com.esri.core.geometry.Envelope2D;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.QuadTree;
import com.esri.core.geometry.QuadTree.QuadTreeIterator;

public class DBSCAN_MatcherSample {
	/**
     * Search index for efficient access to point data.
     */
    protected static class SearchIndex implements ISearchIndex<MatcherSample> {
        private final static SpatialOperator spatial = new Geography();
        private final static int height = 16;
        private final QuadTree index = new QuadTree(defaultRegion(), height);
        private final Map<Integer, List<MatcherSample>> points = new HashMap<Integer, List<MatcherSample>>();

        private static Envelope2D defaultRegion() {
            Envelope2D region = new Envelope2D();
            region.setCoords(-180, -90, 180, 90);
            return region;
        }

        /**
         * Constructs search index with a list of matcherSamples.
         *
         * @param elements List of MatcherSample data.
         */
        public SearchIndex(List<MatcherSample> elements) {
            for (MatcherSample element : elements) {
                put(element);
            }
        }

        @Override
        public List<MatcherSample> radius(MatcherSample centerSample, double radius) {
        	Point center=centerSample.point();
            List<MatcherSample> neighbors = new LinkedList<MatcherSample>();
            Envelope2D query = spatial.envelope(center, radius);
            QuadTreeIterator iterator = index.getIterator(query, 0);
            int handle = -1;
            while ((handle = iterator.next()) != -1) {
                int id = index.getElement(handle);
                List<MatcherSample> bucket = points.get(id);
                assert (bucket != null);
                Point point = bucket.get(0).point();
                double distance = spatial.distance(point, center);
                if (distance < radius) {
                    neighbors.addAll(bucket);
                }
            }
            return neighbors;
        }

        /**
         * Add point to search index.
         *
         * @param point Point to be added.
         * @return Returns true if point has been added, false if search index already contains a
         *         point with the same hash value.
         */
        public boolean put(MatcherSample sample) {
        	Point point=sample.point();
            int hash = Arrays.hashCode(new Object[] {point.getX(), point.getY()});

            if (points.containsKey(hash)) {
                points.get(hash).add(sample);
            } else {
                Envelope2D env = new Envelope2D();
                point.queryEnvelope2D(env);
                index.insert(hash, env);
                points.put(hash, new LinkedList<MatcherSample>(Arrays.asList(sample)));
            }

            return true;
        }

        @Override
        public Iterator<MatcherSample> iterator() {
            return new Iterator<MatcherSample>() {
                private final Iterator<List<MatcherSample>> bucketit = points.values().iterator();
                private Iterator<MatcherSample> it = null;

                @Override
                public boolean hasNext() {
                    while ((it == null || !it.hasNext()) && bucketit.hasNext()) {
                        it = bucketit.next().iterator();
                    }

                    if (it == null || !it.hasNext()) {
                        return false;
                    } else {
                        return true;
                    }
                }

                @Override
                public MatcherSample next() {
                    if (this.hasNext()) {
                        return it.next();
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    /**
     * Gets clusters of points by density properties defined as epsilon and minimum.
     *
     * @param elements List of point data.
     * @param epsilon Defines epsilon range (radius) to analyze density.
     * @param minimum Minimum number of points within epsilon range.
     * @return Set of clusters (lists) of elements.
     */
    public static Set<List<MatcherSample>> cluster(List<MatcherSample> elements, double epsilon, int minimum) {
        ISearchIndex<MatcherSample> index = new SearchIndex(elements);
        return DBCAN.cluster(index, epsilon, minimum);
    }
}
