package com.vishnu.spark.graph.ml

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.mllib.recommendation.{ALS,MatrixFactorizationModel,Rating}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql._

object ALSRecommender2 {
  
  def main(args: Array[String]): Unit = {
    //conf
    val conf = new SparkConf().setAppName("MovieRecommender")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._
    
    
    //load data
    val ratingText = sc.textFile("/mapr_lab_data/data/ratings.dat")
    val ratingsRDD = ratingText.map(parseRating).cache()
    
    val moviesDF= sc.textFile("/mapr_lab_data/data/movies.dat").map(parseMovie).toDF()  
    val usersDF = sc.textFile("/mapr_lab_data/data/users.dat").map(parseUser).toDF() 
    val ratingsDF = ratingsRDD.toDF()
    
    ratingsDF.registerTempTable("ratings")
    usersDF.registerTempTable("users")
    moviesDF.registerTempTable("movies")
    
    //TODO
  }
  
  def parseRating(str: String): Rating = {
    val p = str.split("::")
    Rating(p(0).toInt,p(1).toInt,p(2).toDouble)
  }
  
  def parseUser(str: String): User = {
      val fields = str.split("::")
      assert(fields.size == 5)
      User(fields(0).toInt, fields(1).toString, fields(2).toInt, fields(3).toInt, fields(4).toString)
    }
  
   def parseMovie(str: String): Movie = {
      val fields = str.split("::")
      assert(fields.size == 3)
      Movie(fields(0).toInt, fields(1))
    }
  case class Movie(movieId: Int, title: String)
  case class User(userId: Int, gender: String, age: Int, occupation: Int, zip: String)
  //case class Rating(user:Int, movie: Int, rating: Double) no need of this since spark ml lib package is having Rating class
  
}