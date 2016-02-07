package com.vishnuviswanath.examples;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
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
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

/**
 * Reads an image file in the hdfs and converts it to bytes and output
 * The input should contain the image_name,image_path.
 * @author vishnu
 *
 */
public class ImageReader {

	private static class ImageMapper extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, BytesWritable> {

		private JobConf localconf;

		@Override
		public void map(LongWritable offset, Text value,
				OutputCollector<Text, BytesWritable> output, Reporter reporter)
				throws IOException {

			String line = value.toString();
			String[] parts = line.split(" ");
			Text key = new Text(parts[0]);
			String path = parts[1];
			FileSystem fs = FileSystem.get(URI.create(path), localconf);
			FSDataInputStream fsin = null;

			try {
				fsin = fs.open(new Path(path));
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024 * 1024];

				while (fsin.read(buffer, 0, buffer.length) >= 0) {
					bout.write(buffer);
				}
				output.collect(key, new BytesWritable(bout.toByteArray()));
			} finally {
				IOUtils.closeStream(fsin);
			}

		}

		@Override
		public void configure(JobConf conf) {
			localconf = conf;
		}

	}

	public static void main(String[] args) throws IOException {
		JobConf conf = new JobConf(ImageReader.class);
		conf.setJobName("imagereader");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(BytesWritable.class);

		conf.setMapperClass(ImageMapper.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		JobClient.runJob(conf);
	}
}
