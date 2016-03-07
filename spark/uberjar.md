## How to create Uber/Fat jar using sbt

1. create assembly.sbt file inside your project folder. 
  e.g., if your eclipse project root folder is spark_learn, then you should have spark_learn/project/assembly.sbt
2. add below lines to assembly.sbt
  ```
  addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.2")
  ```
3. To create the jar, from your sbt console type `assembly`. Note that you won't get a fat jar if you use the command `package`
4. All dependecies would now be packaged in your jar file. to exclude a jar file and its dependencies, mention it as provided.
  e.g., `val spark_streaming = "org.apache.spark" % "spark-streaming_2.10" % "1.6.0" % "provided"`
5. You might need to add some merge starategies since multiple depnedencies can depend on same dependency(e.g., A can depend on C and B can depend on another verision of C)
  .
  This is how my merge strategy is defined. Works so far for Spark

```
mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
    case x if x.contains("unused") => MergeStrategy.last
    case "application.conf" => MergeStrategy.concat
    case "unwanted.txt"     => MergeStrategy.discard
    case x => old(x)
  }
}
```
