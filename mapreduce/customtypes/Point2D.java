package com.vishnuviswanath.examples.customtypes;

/**
 * Custom type representing a 2D point
 */
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class Point2D implements Writable {
	
	public float x;
	public float y;
	
	public Point2D(float x,float y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public void readFields(DataInput in) throws IOException {
		x = in.readFloat();
		y = in.readFloat();
		
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeFloat(x);
		out.writeFloat(y);
	}
	
	@Override
	public String toString() {
		return "("+x+","+y+")";
	}

}
