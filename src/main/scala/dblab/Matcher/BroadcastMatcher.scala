package dblab.Matcher

import com.bmwcarit.barefoot.matcher.Matcher
import com.bmwcarit.barefoot.road.PostGISReader
import com.bmwcarit.barefoot.roadmap.RoadMap
import com.bmwcarit.barefoot.topology.Dijkstra
import com.bmwcarit.barefoot.roadmap.Road
import com.bmwcarit.barefoot.roadmap.RoadPoint
import com.bmwcarit.barefoot.roadmap.TimePriority
import com.bmwcarit.barefoot.spatial.Geography
import com.bmwcarit.barefoot.matcher.MatcherSample
import com.bmwcarit.barefoot.matcher.MatcherKState
import com.bmwcarit.barefoot.roadmap.Loader
import com.bmwcarit.barefoot.road.BfmapReader

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import scala.List
import java.util.ArrayList
import scala.collection.JavaConverters._

object BroadcastMatcher {
  private var instance = null: Matcher
  
  private def initialize(host: String, port: Int, name: String, user: String, pass: String, config: String) {
    if (instance != null) return
    this.synchronized {
      if (instance == null) { // initialize map matcher once per Executor (JVM process/cluster node)
        val reader = new PostGISReader(host, port, name, "bfmap_ways", user, pass, Loader.roadtypes(new JSONObject(config)))
        val map = RoadMap.Load(reader)
//        val map = RoadMap.Load(new BfmapReader("map/hongkong.bfmap"))
        map.construct();

        val router = new Dijkstra[Road, RoadPoint]()
        val cost = new TimePriority()
        val spatial = new Geography()

        instance = new Matcher(map, router, cost, spatial)
      }
    }
  }
}

@SerialVersionUID(1L)
class BroadcastMatcher(host: String, port: Int, name: String, user: String, pass: String, config: String) extends Serializable {

  def mmatch(samples: List[MatcherSample]): MatcherKState = {
    mmatch(samples, 0, 1000)
  }

  def mmatch(samples: List[MatcherSample], minDistance: Double, minInterval: Int): MatcherKState = {
    BroadcastMatcher.initialize(host, port, name, user, pass, config)
    BroadcastMatcher.instance.mmatch(new ArrayList[MatcherSample](samples.asJava), minDistance, minInterval)
  }
}