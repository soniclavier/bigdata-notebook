package com.vishnuviswanath.examples.customtypes;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;

/**
 * A custom input format for reading a rectangle info from a file
 * @author vishnu
 *
 */

public class RectangleInputFormat extends FileInputFormat<Text,RectangleKey>{

	@Override
	public RecordReader<Text, RectangleKey> getRecordReader(InputSplit input,
			JobConf conf, Reporter reporter) throws IOException {
		reporter.setStatus(input.toString());
		return new RectangleRecordReader(conf,(FileSplit)input);
	}

}
