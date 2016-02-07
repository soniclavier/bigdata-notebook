package com.vishnuviswanath.examples.customtypes;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.LineRecordReader;
import org.apache.hadoop.mapred.RecordReader;
	
/**
 * RecordReader for custom input format RectangleInputFormat
 * @author vishnu
 *
 */
public class RectangleRecordReader implements RecordReader<Text, RectangleKey> {

	private LineRecordReader lineReader;
	private LongWritable lineKey;
	private Text lineValue;

	public RectangleRecordReader(JobConf job,FileSplit split) throws IOException {
		lineReader = new LineRecordReader(job,split);
		lineKey = lineReader.createKey();
		lineValue = lineReader.createValue();
	}
		
	@Override
	public void close() throws IOException {
		lineReader.close();

	}

	@Override
	public Text createKey() {
		return new Text("");
	}

	@Override
	public RectangleKey createValue() {
		return new RectangleKey();
	}

	@Override
	public long getPos() throws IOException {
		return lineReader.getPos();
	}

	@Override
	public float getProgress() throws IOException {
		return lineReader.getProgress();
	}

	@Override
	public boolean next(Text key, RectangleKey value) throws IOException {
		//go to the next line for this input split
		if (!lineReader.next(lineKey, lineValue)) {
			return false;
		}

		String[] lineParts = lineValue.toString().split(",");
		if(lineParts.length != 5) {
			System.out.println("Invalid input.\nValid format :[ id,ax ay,bx by,cx cy,dx dy ]");
			return false;
		}
		float ax,ay,bx,by,cx,cy,dx,dy;
		try {
			String[] pointA =lineParts[1].split(" ");
			String[] pointB =lineParts[2].split(" ");
			String[] pointC =lineParts[3].split(" ");
			String[] pointD =lineParts[4].split(" ");
			
			ax = Float.parseFloat(pointA[0].trim());
			ay = Float.parseFloat(pointA[1].trim());
			bx = Float.parseFloat(pointB[0].trim());
			by = Float.parseFloat(pointB[1].trim());
			cx = Float.parseFloat(pointC[0].trim());
			cy = Float.parseFloat(pointC[1].trim());
			dx = Float.parseFloat(pointD[0].trim());
			dy = Float.parseFloat(pointD[1].trim());
			
		} catch (NumberFormatException nfe) {
			throw new IOException("Error parsing floating point "+nfe);
		}
		
		//set the key as the first part of the record
		key.set(lineParts[0].trim());
		//set the value
		Point2D a = new Point2D(ax,ay);
		Point2D b = new Point2D(bx,by);
		Point2D c = new Point2D(cx,cy);
		Point2D d = new Point2D(dx,dy);
		value.a = a;
		value.b = b;
		value.c = c;
		value.d = d;
		
		return true;
	}
}
