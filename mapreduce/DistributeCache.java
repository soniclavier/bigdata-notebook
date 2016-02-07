package com.hadoopexpress.examples;

import java.io.IOException;

import org.apache.hadoop.filecache.DistributedCache;
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

/**
 * Shows how a file can be loaded to dist cache. and how it can be used in mapper.
 * @author vishnu
 *
 */

public class DistributeCache {
	
	private static class MyMapper extends MapReduceBase implements Mapper<LongWritable,Text,Text,IntWritable> {
		
		private Path[] localFiles;
		
		@Override
		public void map(LongWritable key, Text value,
				OutputCollector<Text, IntWritable> output, Reporter reporter)
				throws IOException {
			for(Path path : localFiles) {
				output.collect(new Text(path.getName()), new IntWritable(1));
			}
			
		}
		
		@Override 
		public void configure(JobConf conf) {
			try {
				localFiles = DistributedCache.getLocalCacheFiles(conf);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static void main(String[] args) throws IOException {		
		JobConf conf = new JobConf(DistributeCache.class);
		conf.setJobName("distcache");

	    conf.setOutputKeyClass(Text.class);
	    conf.setOutputValueClass(IntWritable.class);

	    conf.setMapperClass(MyMapper.class);
	    /* by default identity reducer will be called
	     * conf.setReducerClass(MyReducer.class);*/
	    conf.setInputFormat(TextInputFormat.class);
	    conf.setOutputFormat(TextOutputFormat.class);
	    FileSystem fs = FileSystem.get(conf);
	    DistributedCache.addFileToClassPath(new Path(args[2]), conf, fs);
	    FileInputFormat.setInputPaths(conf, new Path(args[0]));
	    FileOutputFormat.setOutputPath(conf, new Path(args[1]));

	    JobClient.runJob(conf);
	}

}
