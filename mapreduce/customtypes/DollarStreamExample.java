package com.vishnuviswanath.examples.customtypes;
/**
 * Simple program to test dollar($) as file delimiter
 */
import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;


/**\
 * have to do it in the new api
 * @author vishnu
 *
 */
public class DollarStreamExample {

  public static class MyMapper extends MapReduceBase implements Mapper<LongWritable, Text, LongWritable, Text> {
    
    public void map(LongWritable key, Text value, OutputCollector<LongWritable, Text> output, Reporter reporter) throws IOException {
      String line = value.toString();
      System.out.println("received in mapper"+line);
      output.collect(key, value);
    }
  }

  public static class MyReducer extends MapReduceBase implements Reducer<LongWritable, Text, LongWritable, Text> {
    public void reduce(LongWritable key, Iterator<Text> values, OutputCollector<LongWritable, Text> output, Reporter reporter) throws IOException {
     String temp = "";
     while(values.hasNext()) {
    	 temp += values.next().toString();
     }
     System.out.println("In reducer "+temp);
      output.collect(key,new Text(temp));
    }
  }

  public static void main(String[] args) throws Exception {
    JobConf conf = new JobConf(DollarStreamExample.class);
    conf.setJobName("wordcount");
    
    
    conf.setMapperClass(MyMapper.class);
    conf.setReducerClass(MyReducer.class);
    conf.setOutputKeyClass(LongWritable.class);
    conf.setOutputValueClass(Text.class);

    conf.setMapperClass(MyMapper.class);
    conf.setReducerClass(MyReducer.class);
    Job job = new Job(conf,"wordcount");
    job.setOutputKeyClass(LongWritable.class);
    job.setOutputValueClass(Text.class);
    job.setInputFormatClass(DollarInputFormat.class);
    job.setOutputFormatClass(TextOutputFormat.class);

    FileInputFormat.setInputPaths(conf, new Path(args[0]));
    FileOutputFormat.setOutputPath(conf, new Path(args[1]));
    job.waitForCompletion(true);
  }
}