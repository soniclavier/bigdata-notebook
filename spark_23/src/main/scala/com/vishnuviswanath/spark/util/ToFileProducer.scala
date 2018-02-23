package com.vishnuviswanath.spark.util

import java.io._

/**
  * Created by vviswanath on 2/22/18.
  *
  * Writes to a file, rolls over every rollOverCount, stops when maxFiles #files are created.
  * Sources from WordsStream.scala
  */
object ToFileProducer {

  def main(args: Array[String]): Unit = {

    val wordsStream = WordsStream.stream
    val rollOverCount = 1000
    var fileIndex = 0
    val maxFiles: Option[Int] = Some(50)

    val path = if (args.length > 0) args(0) else "/tmp/spark_file_stream"


    val filePrefix = "words_set"

    val stream = WordsStream.stream

    def rollingWriter(path: String, filePrefix: String)(index: Int, previousWriter: Option[PrintWriter]): PrintWriter = {
      previousWriter.foreach(w ⇒ {
        w.flush()
        w.close()
      })
      val file = new File(s"$path/${filePrefix}_$index")
      print(s"new file created ${file.getAbsolutePath}\n")
      file.getParentFile.mkdirs()
      file.createNewFile()
      new PrintWriter(file)
    }

    val writerGen: (Int, Option[PrintWriter]) => PrintWriter = rollingWriter(path, filePrefix)

    var writer = writerGen(0, None)

    var wordsWritten = 0

    for {
      word ← stream
    } {
      if (wordsWritten == rollOverCount) {
        wordsWritten = 0
        if (maxFiles.isDefined && fileIndex + 1 > maxFiles.get) {
          System.exit(0)
        }
        fileIndex += 1
        writer = writerGen(fileIndex, Some(writer))
      }
      writer.write(word+" ")
      wordsWritten += 1
    }

    writer.close()
  }

}
