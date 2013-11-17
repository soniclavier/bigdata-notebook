package com.hadoopexpress.examples.chaining;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

public class ChainingSimple {

	public static void main(String[] args) throws IOException {
		
		JobConf conf1 = new JobConf(WordCount.class);
		conf1.setJobName("wordcount");
		
		conf1.setOutputKeyClass(Text.class);
		conf1.setOutputValueClass(IntWritable.class);
		conf1.setMapperClass(WordCount.WordCountMapper.class);
		conf1.setCombinerClass(WordCount.WordCountReducer.class);
		conf1.setReducerClass(WordCount.WordCountReducer.class);
		conf1.setInputFormat(TextInputFormat.class);
		conf1.setOutputFormat(TextOutputFormat.class);
		FileInputFormat.setInputPaths(conf1,new Path(args[0]));
		Path intermediate = new Path("intermediate");
		FileOutputFormat.setOutputPath(conf1,intermediate);		
		JobClient.runJob(conf1);
		
		
		JobConf conf2 = new JobConf(WordCount.class);
		conf2.setOutputKeyClass(Text.class);
		conf2.setOutputValueClass(IntWritable.class);
		conf2.setMapperClass(LetterCount.LetterCountMapper.class);
		conf2.setCombinerClass(LetterCount.LetterCountReducer.class);
		conf2.setReducerClass(LetterCount.LetterCountReducer.class);
		conf2.setInputFormat(TextInputFormat.class);
		conf2.setOutputFormat(TextOutputFormat.class);
		FileInputFormat.setInputPaths(conf2,intermediate);
		FileOutputFormat.setOutputPath(conf2,new Path(args[1]));
		JobClient.runJob(conf2);
		
		System.out.println("Done");
		
		
		
	}
}
	

