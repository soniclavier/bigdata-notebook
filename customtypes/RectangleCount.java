package com.hadoopexpress.examples.customtypes;
/**
 * Simple word count program with custom counters and custom comparator(commented)
 */
import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;

public class RectangleCount {
	
	// A custom counter named CUSTOM_COUNT
	static enum CustomCounter{CUSTOM_COUNT};
  public static class MyMapper extends MapReduceBase implements Mapper<Text, RectangleKey, RectangleKey, IntWritable> {

    public void map(Text key, RectangleKey value, OutputCollector<RectangleKey, IntWritable> output, Reporter reporter) throws IOException {
      String line = value.toString();
      System.out.println("Received "+line);
      reporter.incrCounter(CustomCounter.CUSTOM_COUNT,1);
      output.collect(value,new IntWritable(1));
    }
  }

  public static class MyReducer extends MapReduceBase implements Reducer<RectangleKey, IntWritable, Text, IntWritable> {
    public void reduce(RectangleKey key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
      int sum = 0;
      while (values.hasNext()) {
        sum += values.next().get();
      }
      output.collect(new Text(key.toString()), new IntWritable(sum));
    }
  }

  public static void main(String[] args) throws Exception {
    JobConf conf = new JobConf(RectangleCount.class);
    conf.setJobName("rectanglecount");

    conf.setOutputKeyClass(RectangleKey.class);
    conf.setOutputValueClass(IntWritable.class);
    
    /*
     * Add the custom comparator for the key output class
     * It didnt work out by adding the comparator using WritableComparator.define() in the static block
     *    
     *  Add this to add the custom compartor. This comparator expects a Text key class.
     *	conf.setOutputKeyComparatorClass(Comparator.class); 
     */
    
    
    conf.setMapperClass(MyMapper.class);
    conf.setReducerClass(MyReducer.class);

    conf.setInputFormat(RectangleInputFormat.class);
    conf.setOutputFormat(TextOutputFormat.class);

    FileInputFormat.setInputPaths(conf, new Path(args[0]));
    FileOutputFormat.setOutputPath(conf, new Path(args[1]));

    JobClient.runJob(conf);
  }
}