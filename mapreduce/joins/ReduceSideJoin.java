package com.vishnuviswanath.examples.joins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.lib.MultipleInputs;

/**
 * An example of reduce side join
 * there are two files for two tables. employee and dept
 * employee is having empid,name,deptid
 * dept is having deptid,deptname
 * 
 * output will tell the dept name of each employee
 * @author vishnu
 *
 */


public class ReduceSideJoin {

	private static class EmployeeMapper extends MapReduceBase implements Mapper<LongWritable,Text,IntWritable,Text> {

		@Override
		public void map(LongWritable key, Text value,
				OutputCollector<IntWritable, Text> output, Reporter reporter)
				throws IOException {
			
			String line = value.toString();
			String[] parts = line.split(" ");
			IntWritable deptId = new IntWritable(Integer.parseInt(parts[2]));
			Text name = new Text("emp_"+parts[1]);
			output.collect(deptId, name);
			
		}
		
	}
	
	private static class DeptMapper extends MapReduceBase implements Mapper<LongWritable,Text,IntWritable,Text> {

		@Override
		public void map(LongWritable key, Text value,
				OutputCollector<IntWritable, Text> output, Reporter reporter)
				throws IOException {
			
			String line = value.toString();
			String[] parts = line.split(" ");
			IntWritable deptId = new IntWritable(Integer.parseInt(parts[0]));
			Text deptname = new Text("dept_"+parts[1]);
			output.collect(deptId, deptname);
			
		}
		
	}
	
	private static class Joiner extends MapReduceBase implements Reducer<IntWritable,Text,Text,Text> {

		@Override
		public void reduce(IntWritable key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			Text deptName = new Text();
			ArrayList<String> employees = new ArrayList<String>(); 
			while(values.hasNext()) {
				Text value = values.next();
				String val = value.toString();
				if (val.startsWith("dept_")) {
					deptName = new Text(val.substring(5));
				}else if (val.startsWith("emp_")){
					String employee = val.substring(4);
					employees.add(employee);
				}
			}
			
			for(String emp : employees)
				output.collect(new Text(emp),deptName);
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		JobConf conf = new JobConf(ReduceSideJoin.class);
	    conf.setJobName("reduceside-join");

	    conf.setOutputKeyClass(IntWritable.class);
	    conf.setOutputValueClass(Text.class);

	    conf.setReducerClass(Joiner.class);
	    
	    conf.setInputFormat(TextInputFormat.class);
	    conf.setOutputFormat(TextOutputFormat.class);

	    MultipleInputs.addInputPath(conf,new Path(args[0]),TextInputFormat.class,EmployeeMapper.class);
	    MultipleInputs.addInputPath(conf,new Path(args[1]),TextInputFormat.class,DeptMapper.class);
	    FileOutputFormat.setOutputPath(conf, new Path(args[2]));

	    JobClient.runJob(conf);
	}
}
