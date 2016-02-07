package com.hadoopexpress.examples.customtypes;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

/**
 * A Custom InputFormat which will split the lines based on $ and will ignore \n
 * @author vishnu
 *
 */
public class DollarInputFormat extends FileInputFormat<LongWritable,Text>{


	@Override
	public RecordReader<LongWritable, Text> createRecordReader(InputSplit split,
			TaskAttemptContext context) throws IOException, InterruptedException {
		return new DollarRecordReader();
	}
	
	

}
