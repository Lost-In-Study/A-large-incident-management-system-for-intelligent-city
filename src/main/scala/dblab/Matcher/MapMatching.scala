package dblab.Matcher
import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql._
import java.util.Properties
import org.apache.spark.sql.SQLContext
import com.esri.core.geometry.Point
import com.bmwcarit.barefoot.matcher.MatcherSample
import scala.io.Source
import org.json.JSONObject;
import com.bmwcarit.barefoot.roadmap.Loader
import com.mysql.jdbc.Driver

object MapMatching {
  def main(args: Array[String]) {
        
        val conf = new SparkConf().setAppName("Mapmatching")
        val sc = new SparkContext(conf)    
        val sqlContext= new SQLContext(sc)    
    
        val file=Source.fromFile("/home/hduser/yanhui/WordCount/map/tools/road-types.json")
        val json=new StringBuilder
        for(line<-file.getLines()){
          json.++=(line)
        }
        println(json.toString())
//        val roadtype=new JSONObject(json)
       
        val url="jdbc:mysql://msd16094s1.cs.hku.hk:3306/taxi?characterEncoding=utf8&useSSL=true"  
        val prop = new java.util.Properties  
        prop.setProperty("user","root")  
        prop.setProperty("password","1234")
        prop.setProperty("driver","com.mysql.jdbc.Driver")
        
        //val where=Array("Date(HkDatetime)='2010-01-01'","Time(HkDatetime) between '09:00:00' and '09:30:00'" )
        val taxi = sqlContext.read.jdbc(url,"taxidata",prop).select("DevID","HkDatetime","Lat","Lon")
        val taxiFilter=taxi.filter("Date(HkDatetime)='2010-01-01' and Hour(HkDatetime)=9")
//        println(taxiFilter.count())
//        
        val host="msd16094s1.cs.hku.hk"
        val port=5432
        val database="hongkong"
        val user="osmuser"
        val pass="pass"
        val matcher = sc.broadcast(new BroadcastMatcher(host, port, database, user, pass, json.toString()))

        // Load trace data as RDD from CSV file asset of tuples:
        // (object-id: String, time: Long, position: Point)
        val traces = taxiFilter.rdd.map(x=>(x.getString(0),x.getTimestamp(1).getTime(),new Point(x.getDouble(3),x.getDouble(2))))
//
//        // Run a map job on RDD that uses the matcher instance.
        val matches = traces.groupBy(x => x._1).map(x => {
         val trip = x._2.map({
            x => new MatcherSample(x._1, x._2, x._3)
         }).toList
         matcher.value.mmatch(trip)})
         
         matches.foreach(state=>println(state.toGeoJSON().toString()))
         
//        println(matches.collect().length) 
       
    }
}