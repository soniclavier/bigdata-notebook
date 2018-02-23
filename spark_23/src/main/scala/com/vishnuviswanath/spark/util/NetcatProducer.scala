package com.vishnuviswanath.spark.util

import java.io.{BufferedWriter, OutputStreamWriter, PrintWriter}
import java.net.ServerSocket

/**
  * Created by vviswanath on 2/21/18.
  *
  * A util for writing to a socket}
  */
object NetcatProducer {

  def main(args: Array[String]): Unit = {
    val port = 9999
    val server = new ServerSocket(port)
    val sleepInterval = 100


    val socket = server.accept()
    val outputStream = socket.getOutputStream

    val writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream)))

    val contentStream = WordsStream.stream

    for {
      word ← contentStream
    } {
      print(s"writing word $word\n")
      writer.println(word)
      writer.flush()
      Thread.sleep(sleepInterval)
    }

    print("close the producer?")
    System.in.read()
  }
}
