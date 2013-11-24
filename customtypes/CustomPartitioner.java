package com.hadoopexpress.examples.customtypes;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Partitioner;

/**
 * A custom partitioner to partition keys into reducers
 * @author vishnu
 *
 */
public class CustomPartitioner implements Partitioner<Text,Text>{

	@Override
	public int getPartition(Text key, Text value, int numReducers) {
		// TODO Auto-generated method stub
		System.out.println("number of reducers is "+numReducers);
		if (key.toString().equals("reduce")) {
			System.out.println("in custom partioner is returning 0");
			return 0%numReducers;
		} else {
			System.out.println("in custom partioner is returning 1");
			return 1%numReducers;
		}
	}
	
	
	/**
	 * provides an example of bit masking which will convert negative results to positive
	 */
	/*@Override
	public int getPartition(Text key, IntWritable value, int numPartitions) {
	    return (key.toString().hashCode() % numPartitions) & 0x7FFFFFFF;
	}*/

	@Override
	public void configure(JobConf conf) {
	}
}
