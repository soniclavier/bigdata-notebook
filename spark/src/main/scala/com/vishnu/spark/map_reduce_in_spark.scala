//map-reduce
val lines = sc.parallelize(List("this is","an example"))
val lengthOfLines = lines.map(line => line.length)
lengthOfLines.collect()
lines.map(line => (line.length)).reduce((a,b) => a + b) //returns an int
lines.map(line => (line.length)).reduce(_ + _) 
lines.map(_.length).reduce(_ + _) 


//another example with string
val lines = sc.parallelize(List("this is","an example"))
val firstWords = lines.map(line => (line.substring(0,line.indexOf(" ")))).collect()
firstWords: Array[String] = Array(this, an)
firstWords.reduce((a,b) => a +" "+b) //returns an string
firstWords.reduce(_ +" "+_)


//wordcount using flatMap and reduceByKey
val lines = sc.parallelize(List("line number one","line number two"))
val words = lines.map(line => (line.split(" "))) // to show difference between map and flatMap - map does not flatten the result
val words = lines.flatMap(line => (line.split(" "))) // difference between using collect and not using collect - collect returns an array
words.map(x => (x,1))
val wordCount = words.map(x => (x,1)).reduceByKey(_ + _)
wordCount.collect()

//another difference between map and flatMap
//map should always emit an output but not required for flatMap
val lines = sc.parallelize(List("this is line number one","line number two","line number three"))
lines.flatMap(_.split(" ").filter(word => word.contains("this")).map(word => (word,1)))
lines.map(_.split(" ").filter(word => word.contains("this")).map(word => (word,1)))

//filter out a line 
val lines = sc.parallelize(List("line number one","line number two","line number three"))
//filtering lines
val filtered = lines.filter(!_.contains("two"))
//filtering in map
val filteredAgain = filtered.map(_.split(" ").filter(!_.equals("three")))
//filtering using flatMap
val filteredAgain = lines.faltMap(_.split(" ").filter(!_.equals("three")))




