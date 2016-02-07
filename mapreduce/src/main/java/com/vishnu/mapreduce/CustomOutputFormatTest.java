package com.vishnu.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.LazyOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class CustomOutputFormatTest {
	
public static class ParserMapper extends Mapper<Object, Text, Text, Text> {
		
		Configuration conf = null;
		MultipleOutputs<Text, Text> mout;
				
		

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String val = value.toString();
			mout.write("filename",key,new Text(val));			
		}
}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "CustomMultiplOutput");
		job.setJarByClass(CustomOutputFormatTest.class);
		job.setMapperClass(ParserMapper.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		Path source = new Path(args[0]);
		FileInputFormat.addInputPath(job,source);
		CustomMultiplOututFormat.set
		//MultipleOutputs.addNamedOutput(job, BLUECOAT, TextOutputFormat.class, Text.class, Text.class);
		//MultipleOutputs.addNamedOutput(job, BTDIAMOND, TextOutputFormat.class, Text.class, Text.class);
		LazyOutputFormat.setOutputFormatClass(job, TextOutputFormat.class);
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		boolean success = job.waitForCompletion(true);
	}
}
