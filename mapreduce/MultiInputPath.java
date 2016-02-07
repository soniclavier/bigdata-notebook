package com.hadoopexpress.examples;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.lib.IdentityReducer;
import org.apache.hadoop.mapred.lib.MultipleInputs;

/**
 * Move to new api...
 * @author vishnu
 *
 */

public class MultiInputPath {
	
	private static class MyMapper extends MapReduceBase implements Mapper<LongWritable,Text,LongWritable,IntWritable> {

		@Override
		public void map(LongWritable key, Text value,
				OutputCollector<LongWritable, IntWritable> output, Reporter reporter)
				throws IOException {	
			System.out.println("In mapper 1");
			output.collect(key, new IntWritable(value.toString().length()));
		}
		
	}
	
	
	private static class MyMapper2 extends MapReduceBase implements Mapper<LongWritable,Text,LongWritable,IntWritable> {

		@Override
		public void map(LongWritable key, Text value,
				OutputCollector<LongWritable, IntWritable> output, Reporter reporter)
				throws IOException {	
			System.out.println("In mapper 2");
			output.collect(key, new IntWritable(value.toString().length()));
		}
		
	}
	public static void main(String[] args) throws IOException {
		
		JobConf conf = new JobConf(MultiInputPath.class);
		conf.setJobName("multi");
		conf.setMapperClass(MyMapper.class);
		conf.setReducerClass(IdentityReducer.class);
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);
		conf.setOutputKeyClass(LongWritable.class);
		conf.setOutputValueClass(IntWritable.class);
		MultipleInputs.addInputPath(conf, new Path(args[0]), TextInputFormat.class,MyMapper.class);
		MultipleInputs.addInputPath(conf,new Path(args[1]),TextInputFormat.class,MyMapper2.class);
		FileOutputFormat.setOutputPath(conf,new Path(args[2]));
		
		JobClient.runJob(conf);
		
		
	}
}


