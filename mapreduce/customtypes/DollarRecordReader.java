package com.vishnuviswanath.examples.customtypes;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.util.LineReader;

/**
 * from :
 * http://hadoopi.wordpress.com/2013/05/27/understand-recordreader-inputsplit/
 * 
 * @author vishnu
 * 
 */

public class DollarRecordReader extends RecordReader<LongWritable, Text> {

	private LineReader in;
	private long start;
	private long pos;
	private long end;
	private LongWritable key = new LongWritable();
	private final static Text EOL = new Text("\n");
	private Pattern delimiterPattern;
	private String delimiterRegex;
	private int maxLengthRecord;
	private Text value = new Text();
	private int maxLineLength;

	@Override
	public void close() throws IOException {
		if (in != null) {
			in.close();
		}

	}

	@Override
	public LongWritable getCurrentKey() throws IOException,
			InterruptedException {
		return key;

	}

	@Override
	public Text getCurrentValue() throws IOException, InterruptedException {
		return value;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		if (start == end) {
			return 0.0f;
		} else {
			return Math.min(1.0f, (pos - start) / (float) (end - start));
		}
	}

	@Override
	public void initialize(InputSplit split, TaskAttemptContext context)
			throws IOException, InterruptedException {
		Configuration job = context.getConfiguration();
		this.delimiterRegex = "*$*";
		this.maxLengthRecord = job.getInt("mapred.linerecordreader.maxlength",
				Integer.MAX_VALUE);

		delimiterPattern = Pattern.compile(delimiterRegex);

	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		// Current offset is the key
		key.set(pos);

		int newSize = 0;

		// Make sure we get at least one record that starts in this Split
		while (pos < end) {

			// Read first line and store its content to "value"
			newSize = readNext(value, maxLineLength,
					Math.max((int) Math.min(Integer.MAX_VALUE, end - pos),
							maxLineLength));

			// No byte read, seems that we reached end of Split
			// Break and return false (no key / value)
			if (newSize == 0) {
				break;
			}

			// Line is read, new position is set
			pos += newSize;

			// Line is lower than Maximum record line size
			// break and return true (found key / value)
			if (newSize < maxLineLength) {
				break;
			}

			// Line is too long
			// Try again with position = position + line offset,
			// i.e. ignore line and go to next one
			// TODO: Shouldn't it be LOG.error instead ??
			System.out.println("Skipped line of size " + newSize + " at pos "
					+ (pos - newSize));
		}

		if (newSize == 0) {
			// We've reached end of Split
			key = null;
			value = null;
			return false;
		} else {
			// Tell Hadoop a new line has been found
			// key / value will be retrieved by
			// getCurrentKey getCurrentValue methods
			return true;
		}
	}

	private int readNext(Text text, int maxLineLength, int maxBytesToConsume)
			throws IOException {

		int offset = 0;
		text.clear();
		Text tmp = new Text();

		for (int i = 0; i < maxBytesToConsume; i++) {

			int offsetTmp = in.readLine(tmp, maxLineLength, maxBytesToConsume);
			offset += offsetTmp;
			Matcher m = delimiterPattern.matcher(tmp.toString());

			// End of File
			if (offsetTmp == 0) {
				break;
			}

			if (m.matches()) {
				// Record delimiter
				break;
			} else {
				// Append value to record
				text.append(EOL.getBytes(), 0, EOL.getLength());
				text.append(tmp.getBytes(), 0, tmp.getLength());
			}
		}
		return offset;
	}

}
