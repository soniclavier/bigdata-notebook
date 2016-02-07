package com.vishnuviswanath.examples;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;
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

/**
 * An example implementing ArrayWritable
 * @author vishnu
 *
 */

public class ArrayWritableExample {

	private static class MyMapper extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, IntArrayWritable> {
		@Override
		public void map(LongWritable dummKey, Text value,
				OutputCollector<Text, IntArrayWritable> output, Reporter reporter)
				throws IOException {
			String line = value.toString();
			String[] parts = line.split(" ");
			IntArrayWritable arr = new IntArrayWritable();
			IntWritable[] intArr = new IntWritable[parts.length - 1];
			if (parts.length >= 2) {
				Text key = new Text(parts[0]);
				for (int i = 1; i < parts.length; i++) {
					IntWritable val = new IntWritable(
							Integer.parseInt(parts[i]));
					intArr[i - 1] = val;
				}
				arr.set(intArr);
				System.out.println("key "+key.toString()+" arr"+arr.toString());
				output.collect(key, arr);
			}
		}
	}
	
	private static class IntArrayWritable extends ArrayWritable {

		public IntArrayWritable() {
			super(IntWritable.class);
		}
		
		@Override 
		public String toString() {
			String[] arr = super.toStrings();
			String result = "";
			for ( String str:arr) {
				result+=str+" ";						
			}
			return result;
		}
		
	}

	public static void main(String[] args) throws IOException {

		JobConf conf = new JobConf(ArrayWritableExample.class);
		conf.setJobName("array writable");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(IntArrayWritable.class);

		conf.setMapperClass(MyMapper.class);
		conf.setReducerClass(IdentityReducer.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
		
		JobClient.runJob(conf);
	}
}
