package com.hadoopexpress.examples.customtypes;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
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
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

/**
 * Area calculator will read a file in which each line corresponds to 4 points (x,y) for a rectangle
 * in the format ax ay,bx by,cx cy,dx dy
 * 
 * 	   (bx,by)-------------------------(cx,xy)
 *  	  |								|
 *   	  |								|
 *    	  |								|
 *     	  |                             |
 *        |								|
 * 	   (ax,ay)-------------------------(dx,dy)
 * 
 * The task is to find out for how many rectangles are there with same area 
 * @author vishnu
 *
 */
public class AreaCalculator {
	
	public static class AreaMapper extends MapReduceBase implements Mapper<LongWritable, Text, RectangleKey, IntWritable>{

		@Override
		public void map(LongWritable key, Text value,
				OutputCollector<RectangleKey, IntWritable> output, Reporter reporter)
				throws IOException {
			
			
			System.out.println("In Mapper");
			String line = value.toString();;
			String[] values = line.split(",");
			
			//four points of a rectangle
			Point2D a = null;
			Point2D b = null;
			Point2D c = null;
			Point2D d = null;
			
			for (int i =0;i<values.length;i++) {
				String[] points = values[i].split(" ");
				float x = Float.parseFloat(points[0]);
				float y = Float.parseFloat(points[1]);
				if (i == 0)
					a = new Point2D(x,y);
				if (i == 1)
					b = new Point2D(x,y);
				if (i == 2)
					c = new Point2D(x,y);
				if (i == 3)
					d = new Point2D(x,y);
			}
			//construct the rectangle
			RectangleKey outkey = new RectangleKey(a, b, c, d);
			System.out.println("Constructed rectangle "+outkey.toString());
			output.collect(outkey, new IntWritable(1));
		}
		
	}
	
	public static class AreaReducer extends MapReduceBase implements Reducer<RectangleKey,IntWritable,Text,IntWritable> {

		@Override
		public void reduce(RectangleKey key, Iterator<IntWritable> values,
				OutputCollector<Text, IntWritable> output, Reporter reorter)
				throws IOException {
			
			System.out.println("In redcuer");
			System.out.println("Received rectangle "+key.toString());
			int count = 0;
			while(values.hasNext()) {
				int clocal = values.next().get();
				count += clocal;
			}
			System.out.println("count is "+count);
			output.collect(new Text(key.toString()),new IntWritable(count));
			
		}	
	}
	
	
	public static void main(String[] args) throws IOException {
		
		JobConf conf = new JobConf(AreaCalculator.class);
		
		conf.setJobName("AreaCalculator");
		conf.setOutputKeyClass(RectangleKey.class);
		conf.setOutputValueClass(IntWritable.class);
		
		conf.setMapperClass(AreaMapper.class);
/*		conf.setMapOutputKeyClass(RectangleKey.class);
		conf.setMapOutputValueClass(IntWritable.class);*/
		conf.setReducerClass(AreaReducer.class);
		
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);
		
		FileInputFormat.setInputPaths(conf,new Path(args[0]));
		FileOutputFormat.setOutputPath(conf,new Path(args[1]));
		
		JobClient.runJob(conf);
		
		
		
	}

}
