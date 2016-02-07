package com.hadoopexpress.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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
 * Uses hdfs file system api to read the input and find a max value and store it in 
 * the job conf so that it can be used later
 * @author vishnu
 *
 */
public class FindMaximum {

	private static class MaxMapper extends MapReduceBase implements
			Mapper<LongWritable, Text, IntWritable, Text> {

		int max = 0;

		@Override
		public void map(LongWritable key, Text value,
				OutputCollector<IntWritable, Text> output, Reporter reporter)
				throws IOException {
			if (!value.toString().trim().equals("")) {
				String[] parts = value.toString().split(" ");
				String newValue = parts[0];
				int intVal = Integer.parseInt(parts[1]);
				int newKey = intVal / (max / 3);
				output.collect(new IntWritable(newKey), new Text(newValue));
			}
		}

		@Override
		public void configure(JobConf conf) {
			max = Integer.parseInt(conf.get("max_val"));
		}
	}

	public static void main(String[] args) throws IOException {

		JobConf conf = new JobConf(FindMaximum.class);

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(Text.class);
		conf.setMapperClass(MaxMapper.class);
		conf.setReducerClass(IdentityReducer.class);
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileSystem fs = FileSystem.get(conf);
		Path inpath = new Path(args[0]);
		Path outpath = new Path(args[1]);

		BufferedReader br = new BufferedReader(new InputStreamReader(
				fs.open(inpath)));
		String line = "";
		line = br.readLine();
		int max = Integer.MIN_VALUE;
		try {
			while (line != null) {
				System.out.println("line is " + line);
				if (line.trim().length() == 0 || line.trim().equals("")) {
					line = br.readLine();
					continue;
				}
				String[] parts = line.split(" ");
				int val = Integer.parseInt(parts[1]);
				if (val > max)
					max = val;
				line = br.readLine();
			}
		} finally {
			br.close();
		}
		System.out.println("read all the lines, max is " + max);
		conf.setInt("max_val", max);
		fs.delete(outpath, true);
		FileInputFormat.setInputPaths(conf, inpath);
		FileOutputFormat.setOutputPath(conf, outpath);
		JobClient.runJob(conf);
	}
}
