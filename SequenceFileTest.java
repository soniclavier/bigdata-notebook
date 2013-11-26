package com.hadoopexpress.examples;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;

public class SequenceFileTest {
	
	public static void main(String[] args) throws IOException {
		
		JobConf conf = new JobConf();
		Path sqFile = new Path(args[0]);
		FileSystem fs = sqFile.getFileSystem(conf);
		/*for local files
		 * FileSystem fs = FileSystem.getLocal(conf);
		Path sqFile = new Path(args[0]);*/
		
		
		SequenceFile.Writer sqWriter = SequenceFile.createWriter(fs,conf,sqFile,
				Text.class,
				LongWritable.class);
		sqWriter.append(new Text("key1"),new LongWritable(1));
		sqWriter.close();
		SequenceFile.Reader sqReader = new SequenceFile.Reader(fs,sqFile,conf);
		
		Text key = new Text();
		LongWritable value = new LongWritable();
		sqReader.next(key,value);
		
		System.out.println(key.toString()+" - "+value.toString());
		
		sqReader.close();
		
		
		
		
	}

}
