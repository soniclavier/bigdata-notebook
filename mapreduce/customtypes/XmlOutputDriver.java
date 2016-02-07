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
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;


public class XmlOutputDriver {
	
	private static class XmlMapper extends MapReduceBase implements Mapper<LongWritable,Text,Text,Text> {

		@Override
		public void map(LongWritable offset, Text value,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			String line = value.toString();
			String[] parts = line.split(" ");
			Text key = new Text(parts[0]);
			for(int i=1;i<parts.length;i++) {
				output.collect(key,new Text(parts[i]));
			}
		}		
	}
	
	private static class XmlReducer extends MapReduceBase implements Reducer<Text,Text,Text,Text> {

		@Override
		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			while(values.hasNext()) {
				output.collect(key, values.next());
			}			
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		JobConf conf = new JobConf(XmlOutputDriver.class);
	    conf.setJobName("xmlwriter");

	    conf.setOutputKeyClass(Text.class);
	    conf.setOutputValueClass(Text.class);

	    conf.setMapperClass(XmlMapper.class);
	    conf.setReducerClass(XmlReducer.class);

	    conf.setInputFormat(TextInputFormat.class);
	    conf.setOutputFormat(XmlOutputFormat.class);

	    FileInputFormat.setInputPaths(conf, new Path(args[0]));
	    FileOutputFormat.setOutputPath(conf, new Path(args[1]));

	    JobClient.runJob(conf);
	}

}
