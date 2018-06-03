package com.vishnu.flink.util

/**
  * Created by vviswanath on 3/2/18.
  * A very simple arg parser. Expects args to be in the format Array("--key1", "value1", "--key2", "value2")
  * everything else is dropped.
  *   e.g,  key in ("key", "value") is dropped since key doesn't have "--" as prefix
  *   everything after key1 in ("--key1", "value1", "--key2", "--key3", "value3") is dropped since key2 doesn't have a corresponding value2.
  * Returns a map of key → value
  */
object ParameterParser {

  def parse(args: Array[String]): Map[String, String] = {
    val Param = "--(.+)".r
    args.grouped(2).flatMap(l ⇒
      if (l.length == 2) (l(0), l(1)) match {
        case (Param(key), value) ⇒ Some(key → value)
        case _ ⇒ None
      }
      else None
    ).toMap
  }
}