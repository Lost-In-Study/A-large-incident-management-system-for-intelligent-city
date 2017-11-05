package dblab.WordCount

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import java.util.Properties
import org.apache.spark.sql.SQLContext
 
object WordCount {
    def main(args: Array[String]) {
              
 
        val url="jdbc:mysql://msd16094s1.cs.hku.hk:3306/taxi?characterEncoding=utf8&useSSL=true"  
        val prop = new java.util.Properties  
        prop.setProperty("user","root")  
        prop.setProperty("password","1234")
        prop.setProperty("driver","com.mysql.jdbc.Driver")
        
        val conf = new SparkConf().setAppName("WordCount").setMaster("local[2]")
        val sc = new SparkContext(conf)
        val sqlContext= new SQLContext(sc)
        val taxi = sqlContext.read.jdbc(url,"taxidata",Array("DevID=001038"),prop).select("DevID","Lat","Lon")
        println(taxi.count())
    }
}