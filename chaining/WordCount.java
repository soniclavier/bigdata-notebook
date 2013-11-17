package com.hadoopexpress.examples.chaining;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;


public class WordCount {
	
public static  class WordCountMapper extends MapReduceBase implements Mapper<LongWritable,Text,Text,IntWritable> {

	@Override
	public void map(LongWritable key, Text value,
			OutputCollector<Text, IntWritable> output, Reporter reporter)
			throws IOException {
		String line = value.toString();
		StringTokenizer tokenizer = new StringTokenizer(line);
		while(tokenizer.hasMoreTokens()) {
			Text word = new Text();
			word.set(tokenizer.nextToken());
			IntWritable one = new IntWritable(1);
			output.collect(word,one);
		}
	}
	
	
}

public static class WordCountReducer extends MapReduceBase implements Reducer<Text,IntWritable,Text,IntWritable> {

	@Override
	public void reduce(Text key, Iterator<IntWritable> values,
			OutputCollector<Text, IntWritable> output, Reporter reporter)
			throws IOException {
		int sum = 0;
		while(values.hasNext()) {
			int value = values.next().get();
			sum += value;				
		}
		output.collect(key,new IntWritable(sum));
	}	
}

}