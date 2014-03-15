package com.hadoopexpress.examples;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;


public class LetterWordMapper {

	private static class MyMapper extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, Text> {
		private Text word = new Text();

		public void map(LongWritable key, Text value,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			String line = value.toString();
			StringTokenizer tokenizer = new StringTokenizer(line);
			while (tokenizer.hasMoreTokens()) {
				String strWord = tokenizer.nextToken();
				word.set(strWord.charAt(0)+"");
				if (!strWord.trim().equals("")) {
					System.out.println("emitting word "+strWord);
					output.collect(word, new Text(strWord));
				}
			}
		}
	}

	private static class MyReducer extends MapReduceBase implements
			Reducer<Text, Text, Text, Text> {
		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			System.out.println("key is "+key.toString());
			String result = "";
			while(values.hasNext()) {
				String next = values.next().toString();
				System.out.println("next value is "+next);
				result+=next+",";
			}
			result = result.substring(0,result.length()-1);
			System.out.println("result is "+result);
			output.collect(key, new Text(result));
		}
	}

	public static void main(String[] args) throws Exception {
		JobConf conf = new JobConf(WordCount.class);
		conf.setJobName("lettemapper");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(MyMapper.class);
		conf.setReducerClass(MyReducer.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		JobClient.runJob(conf);
	}
}
