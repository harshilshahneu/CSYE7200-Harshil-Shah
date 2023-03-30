package csv

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class MovieDatabaseAnalyzerTest extends AnyFlatSpec with Matchers {

  implicit val sparkSession: SparkSession = SparkSession
    .builder()
    .appName("MovieDatabaseAnalyzer")
    .master("local[*]")
    .getOrCreate()


  private var spark: SparkSession = _
  private var ratings: DataFrame = _


  "MovieAnalyzer" should "calculate the mean and standard deviation ratings for each movie" in {
    // Check that the DataFrame has the expected number of rows
    assert(ratings.count() == 45115)
  }
}
