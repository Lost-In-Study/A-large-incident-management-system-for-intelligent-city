package com.bmwcarit.barefoot.spatial;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.bmwcarit.barefoot.matcher.MatcherSample;
import com.bmwcarit.barefoot.roadmap.Road;
import com.bmwcarit.barefoot.util.Tuple;
import com.esri.core.geometry.Envelope2D;
import com.esri.core.geometry.OperatorExportToWkb;
import com.esri.core.geometry.OperatorImportFromWkb;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.QuadTree;
import com.esri.core.geometry.WkbExportFlags;
import com.esri.core.geometry.WkbImportFlags;
import com.esri.core.geometry.Geometry.Type;
import com.esri.core.geometry.QuadTree.QuadTreeIterator;

public class PointsIndex implements SpatialIndex<Integer>,Serializable {
	private static final long serialVersionUID = 1L;
	private final Geography spatial;
    private final static int height = 16;
    private QuadTree index = null;
    private final HashMap<Integer, byte[]> geometries;
    private final Envelope2D envelope;
    
    /**
     * Creates a GPS sample points with default bounding box of spatially indexed region and
     * uses {@link SpatialOperator} implementation {@link Geography}.
     */
    public PointsIndex() {
        spatial = new Geography();
        envelope = new Envelope2D();
        envelope.setCoords(-180, -90, 180, 90);
        index = new QuadTree(envelope, height);
        geometries = new HashMap<>();
    }
    
    /**
     * Creates a {@link PointsIndex}.
     *
     * @param envelope Bounding box of spatially indexed region.
     * @param spatial {@link SpatialOperator} for spatial operations.
     */
    public PointsIndex(Envelope2D envelope, Geography spatial) {
        this.spatial = spatial;
        this.envelope = envelope;
        index = new QuadTree(envelope, height);
        geometries = new HashMap<>();
    }
    
    /**
     * Adds a copy of a {@link Polyline} in WKB format to spatial index with some reference
     * identifier.
     * <p>
     * <b>Note:</b> To store only references to geometry objects provide geometries in WKB format
     * use {@link QuadTreeIndex#add(int, byte[])}.
     *
     * @param id Identifier reference for polyline.
     * @param polyline {@link Polyline} object of geometry.
     */
    public void add(int id, Point point) {
        Envelope2D env = new Envelope2D();
        point.queryEnvelope2D(env);

        index.insert(id, env);

        ByteBuffer wkb = OperatorExportToWkb.local().execute(WkbExportFlags.wkbExportPoint,
                point, null);
        geometries.put(id, wkb.array());
    }
    
    public void clear() {
        index = new QuadTree(envelope, height);
        geometries.clear();
    }
    
    public boolean contains(int id) {
        return geometries.containsKey(id);
    }
    
    public Set<Road> coveredRoads(Point c, double r){return null;}
    
    public Set<Integer> nearest(Point c){
    	if (index.getElementCount() == 0) {
            return null;
        }
    	
    	Set<Integer> nearests = new HashSet<>();
    	double radius = 20, min = Double.MAX_VALUE;
    	
    	do {
            Envelope2D env = spatial.envelope(c, radius);

            QuadTreeIterator it = index.getIterator(env, 0);
            int handle = -1;

            while ((handle = it.next()) != -1) {
                int id = index.getElement(handle);
                Point geometry = (Point) OperatorImportFromWkb.local().execute(
                        WkbImportFlags.wkbImportDefaults, Type.Point,
                        ByteBuffer.wrap(geometries.get(id)), null);
                
                double d = spatial.distance(geometry, c);

                if (d > min) {
                    continue;
                }

                if (d < min) {
                    min = d;
                    nearests.clear();
                }

                nearests.add(id);
            }

            radius *= 2;

        } while (nearests.isEmpty());
    	
    	return nearests;
    }
    
    public Set<Integer> nearest(Polyline polyline,double distance){
    	Set<Integer> neighbors = new HashSet<>();
    	
    	Point center=spatial.interpolate(polyline, 0.5);
        double radius=spatial.length(polyline);
        Envelope2D env = spatial.envelope(center, radius);
//        Envelope2D env = spatial.envelope(polyline, distance);
        
        QuadTreeIterator it = index.getIterator(env, 0);
        int handle = -1;
        
        while ((handle = it.next()) != -1) {
            int id = index.getElement(handle);
            Point geometry = (Point) OperatorImportFromWkb.local().execute(
                    WkbImportFlags.wkbImportDefaults, Type.Point,
                    ByteBuffer.wrap(geometries.get(id)), null);

            double f = spatial.intercept(polyline,geometry);
            Point p = spatial.interpolate(polyline, spatial.length(polyline), f);
            double d = spatial.distance(p, geometry);

            if (d < distance) {
                neighbors.add(id);
            }
        }
        
        return neighbors;
        
    }
    
    public Set<Integer> radius(Point c, double radius){
    	Set<Integer> neighbors = new HashSet<>();

        Envelope2D env = spatial.envelope(c, radius);

        QuadTreeIterator it = index.getIterator(env, 0);
        int handle = -1;

        while ((handle = it.next()) != -1) {
            int id = index.getElement(handle);
            Point geometry = (Point) OperatorImportFromWkb.local().execute(
                    WkbImportFlags.wkbImportDefaults, Type.Point,
                    ByteBuffer.wrap(geometries.get(id)), null);

            double d = spatial.distance(geometry, c);

            if (d < radius) {
                neighbors.add(id);
            }
        }

        return neighbors;
    }
    
    public Set<Integer> knearest(Point c, int k) {return null;}
    
    
    
    
}
