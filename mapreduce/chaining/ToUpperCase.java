package com.hadoopexpress.examples.chaining;


/**
 * A mapper which converts the key to upper case
 */
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class ToUpperCase extends MapReduceBase implements Mapper<Text, IntWritable, Text, IntWritable>{

	@Override
	public void map(Text key, IntWritable value,
			OutputCollector<Text, IntWritable> output, Reporter reporter)
			throws IOException {
		String keyText = key.toString().toUpperCase();
		output.collect(new Text(keyText),value);
		
	}

}
