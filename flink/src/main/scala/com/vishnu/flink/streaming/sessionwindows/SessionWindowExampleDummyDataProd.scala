package com.vishnu.flink.streaming.sessionwindows

import java.io._
import java.net.ServerSocket

/**
  * Created by vviswanath on 6/8/17.
  *
  * Data producer for ttesting SessionWindowExample.scala
  */
object SessionWindowExampleDummyDataProd {

  def main(args: Array[String]): Unit = {

    val serverSocket = new ServerSocket(4444)
    val clientSocket = serverSocket.accept
    val out = new PrintWriter(clientSocket.getOutputStream, true)

    /*
    //0th second
    out.write(s"${System.currentTimeMillis},user2,recommendation\n")
    out.flush()
    Thread.sleep(1000)//1st second
    out.write(s"${System.currentTimeMillis},user1,recommendation\n")
    out.flush()
    Thread.sleep(1000) //2nd second
    out.write(s"${System.currentTimeMillis},user1,ad\n")
    out.flush()
    Thread.sleep(4000)  //6th second
    out.write(s"${System.currentTimeMillis - 5000},user2,ad\n") //event time 3rd second
    out.flush()
    Thread.sleep(1000) //7th second
    out.write(s"${System.currentTimeMillis},user2,recommendation\n")
    out.flush()
    Thread.sleep(2000) //9th second
    out.write(s"${System.currentTimeMillis},user1,recommendation\n")
    out.flush()
    Thread.sleep(4000)
    out.close()
    */

    //0th second
    out.write(s"${System.currentTimeMillis},user1,recommendation\n")
    out.flush()
    Thread.sleep(1000)//1st second
    out.write(s"${System.currentTimeMillis},user1,recommendation\n")
    out.flush()
    Thread.sleep(2001)//2nd second
    //this message is sent just to advance watermark, to show how AllowedLateness can cause a Window to be evaluated multiple times
    out.write(s"${System.currentTimeMillis},user2,recommendation\n")
    out.flush()
    Thread.sleep(2500)  //4.5th second
    out.write(s"${System.currentTimeMillis - 3500},user1,ad\n") //event time 3rd second
    out.flush()
    Thread.sleep(2500) //7th second
    out.write(s"${System.currentTimeMillis},user1,recommendation\n")
    out.flush()
    Thread.sleep(4000)
    out.close()
  }


}
