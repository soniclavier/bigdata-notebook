package com.hadoopexpress.examples.chaining;

/**
 * This example demonstrated how ChainMapper class can be used to chain mappers
 * Each mapper in the chain will be called using the output of the prev one.
 * Output key, Output value of the first mapper must mach with the Input key 
 * and Input value of the second mapper.
 * 
 * Output of a mapper will be used as the input of the next mapper. And again another
 * output file/folder will be created. So its better to delete the existing output
 * from the driver.
 * 
 * Reducer will be called after all the mappers are called. 
 * 
 * TO-DO: check if reducers can be chained.
 */
import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.lib.ChainMapper;
import org.apache.hadoop.mapred.lib.ChainReducer;

public class ChainMapperExample {
	public static void main(String[] args) throws IOException {
		
		/*
		 *  This conf is used as a ref. Set only the input fileformat and output fileformat
		 *  
		 */
		JobConf conf1 = new JobConf(WordCount.class);
		conf1.setJobName("wordcount");

		conf1.setInputFormat(TextInputFormat.class);
		conf1.setOutputFormat(TextOutputFormat.class);
		
		FileInputFormat.setInputPaths(conf1,new Path(args[0]));
		Path output = new Path(args[1]);
		FileSystem fileSystem = FileSystem.get(conf1);
		fileSystem.delete(output,true);
		FileOutputFormat.setOutputPath(conf1,output);
		
		
		/*
		 * Local job conf files
		 */
		JobConf mapConf = new JobConf(false);
		JobConf reduceConf = new JobConf(false);
		
		/*
		 * First argument is the global conf file we already created
		 * Second is the Mapper/Reducer class we gona use
		 * Third,fourth,fifth and sixth arguments are mapper/reducer inputkey inputvalue,outputkey and outputvalue respectively
		 */
		ChainMapper.addMapper(conf1,WordCount.WordCountMapper.class,LongWritable.class,Text.class,Text.class,IntWritable.class,true,mapConf);
		ChainMapper.addMapper(conf1,ToUpperCase.class,Text.class,IntWritable.class,Text.class,IntWritable.class,true,mapConf);
		ChainReducer.setReducer(conf1,WordCount.WordCountReducer.class,Text.class,IntWritable.class,Text.class,IntWritable.class,true,reduceConf);
		
		JobClient.runJob(conf1);
	}
}
