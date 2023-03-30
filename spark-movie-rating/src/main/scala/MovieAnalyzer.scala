import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{avg, stddev}
import org.apache.spark.sql.types.DoubleType

object MovieAnalyzer {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("MovieAnalyzer")
      .master("local[*]")
      .getOrCreate()

    val df = spark.read
      .option("header", true)
      .option("inferSchema", true)
      .csv("spark-movie-rating/src/main/resources/ratings.csv")

    // Calculate the mean rating for each movie
    val ratings = df.groupBy("movieId")
      .agg(avg("rating").cast(DoubleType).alias("Mean"),
        stddev("rating").cast(DoubleType).alias("Standard Deviation"))
        .na.fill(0.0, Seq("Standard Deviation"))

    // Show the resulting DataFrame
    ratings.show()
    println("count : " + ratings.count()) //45115
    spark.stop()
  }
}
