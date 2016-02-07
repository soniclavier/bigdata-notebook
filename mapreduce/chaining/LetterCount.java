package com.vishnuviswanath.examples.chaining;
/**
 * Take a word and count as input
 * Emit the count for each letter in the word
 * In effect generates a value for each letter which = number of times the letter occurs * number of words in which the word occur 
 */
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;


public class LetterCount {
	

public static class LetterCountMapper extends MapReduceBase implements Mapper<LongWritable,Text,Text,IntWritable> {

	@Override
	public void map(LongWritable key, Text value,
			OutputCollector<Text, IntWritable> output, Reporter reporter)
			throws IOException {
		
		String line = value.toString();
		System.out.println("line is "+line);
		StringTokenizer tokenizer = new StringTokenizer(line);
		boolean first = true;
		String word = "";
		int sum = 0;
		while(tokenizer.hasMoreTokens()) {
				String next = tokenizer.nextToken();
				if (first) {
					first = false;
					word = next;
				}else {
					sum += Integer.parseInt(next);
				}
			}
		System.out.println("word is "+word);
		System.out.println("sum is "+sum);
		
		for(char ch : word.toCharArray()) {
			output.collect(new Text(ch+""),new IntWritable(sum));
		}
	}
}

public static class LetterCountReducer extends MapReduceBase implements Reducer<Text,IntWritable,Text,IntWritable> {

	@Override
	public void reduce(Text key, Iterator<IntWritable> values,
			OutputCollector<Text, IntWritable> output, Reporter reorter)
			throws IOException {
		
	System.out.println("In reducer of letter count");
	int sum = 0;
		while(values.hasNext()) {
			int value = values.next().get();
			System.out.println(value);
			sum += value;
		}
	output.collect(key,new IntWritable(sum));
	}
}

}
