package com.vishnuviswanath.spark.util

/**
  * Created by vviswanath on 3/12/18.
  */
object Test {
  def main(args: Array[String]): Unit = {
    /*val year = "2018"
    val month = "03"
    val days = Array("12", "11", "10", "09", "08", "07", "06", "05", "04")

    var from = "s3://mm-prod-stats-reports/v1/strategy-postal-code"
    var to = "s3://mm-dev-data-platform/vishnu/RELMGMT-5947-backup/strategy-postal-code"

    for {
      day ← days
    } {
      val daybatch = s"$year-$month-$day/"
      println(s"aws s3 cp $from/$daybatch $to/$daybatch --recursive")
    }

    println()
    for {
      day ← days
    } {
      val daybatch = s"$year-$month-$day/"
      println(s"aws s3 rm $from/$daybatch --recursive")
    }

    println()
    from = "s3://mm-dev-data-platform/absharma/single-workflow-site-tech/v1/strategy-postal-code"
    to = "s3://mm-prod-stats-reports/v1/strategy-postal-code"
    for {
      day ← days
    } {
      val daybatch = s"$year-$month-$day/"
      println(s"aws s3 cp $from/$daybatch $to/$daybatch --recursive")
    }*/
    var prefixes = Array("viewability-aggregations-other-site", "viewability-aggregations-other-tech", "viewability-aggregations-site", "viewability-aggregations-tech")
    val batch = "2018-03-12/2018031201/"
    var from = "s3://mm-prod-stats-reports/viewability/v1/"
    var to = "s3://mm-dev-data-platform/vishnu/RELMGMT-5947-backup/single-workflow-viewability-site-tech/"


    for {
      prefix ← prefixes
    } {
      val key = s"$prefix/$batch"
      println(s"aws s3 cp $from$key to $to$key --recursive")
    }

    println()

    for {
      prefix ← prefixes
    } {
      val key = s"$prefix/$batch"
      println(s"aws s3 rm $from$key --recursive")
    }

    println()
    from = "s3://mm-dev-data-platform/absharma/single-workflow-viewability-site-tech/v1/"
    to = "s3://mm-prod-stats-reports/viewability/v1/"

    for {
      prefix ← prefixes
    } {
      val key = s"$prefix/$batch"
      println(s"aws s3 cp $from$key to $to$key --recursive")
    }


  }
}
