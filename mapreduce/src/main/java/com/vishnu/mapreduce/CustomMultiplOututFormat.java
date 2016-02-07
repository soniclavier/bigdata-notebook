package com.vishnu.mapreduce;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.lib.MultipleTextOutputFormat;

import org.apache.hadoop.util.Progressable;

public class CustomMultiplOututFormat<K,V> extends MultipleTextOutputFormat<K,V>{

	@Override
	public RecordWriter<K, V> getRecordWriter(FileSystem fs, JobConf job, 
			String name, Progressable arg3) throws IOException {
		String newName = name.substring(0,name.indexOf("-"));
		System.out.println(name);
		System.out.println(newName);
		return super.getRecordWriter(fs, job, newName, arg3);
	}

}
