package com.vishnuviswanath.examples.customtypes;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
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

public class IdentityReducerEx {
	
	public static class MyMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text>{

		@Override
		public void map(LongWritable key, Text value,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			if (value.toString().contains("vishnu"))
				output.collect(new Text("vishnu"),value);
			else 
				output.collect(new Text("reduce"),value);
		}
		
	}
	
	
	public static void main(String[] args) throws IOException {
		
		JobConf conf1 = new JobConf(IdentityReducerEx.class);
		conf1.setJobName("partition_identity");
		
		conf1.setMapperClass(MyMapper.class);
		conf1.setReducerClass(IdentityReducer.class);
		
		conf1.setPartitionerClass(CustomPartitioner.class);
		
		conf1.setOutputKeyClass(Text.class);
		conf1.setOutputValueClass(Text.class);
		
		conf1.setInputFormat(TextInputFormat.class);
		conf1.setOutputFormat(TextOutputFormat.class);
		
		FileInputFormat.setInputPaths(conf1,new Path(args[0]));
		FileOutputFormat.setOutputPath(conf1,new Path(args[1]));
		
		JobClient.runJob(conf1);
		
		
		
	
		
		
		
	}

}
