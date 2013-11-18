package com.hadoopexpress.examples.customtypes;

/**
 * A custom type corresponding to a rectangle
 *    B-------------------------C
 *    |							|
 *    |							|
 *    |							|
 *    |							|
 *    A-------------------------D
 */
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class Rectangle implements Writable{
	
	public Point2D a;
	public Point2D b;
	public Point2D c;
	public Point2D d;
	
	public Rectangle(Point2D a,Point2D b,Point2D c,Point2D d) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		int p = 0;
		while (p < 4) {
			float x = in.readFloat();
			float y = in.readFloat();
			Point2D point = new Point2D(x,y); 
			if (p == 0)
				a = point;
			if (p == 1)
				b = point;
			if (p == 2)
				c = point;
			if (p == 3)
				d = point;
			p++;
		}		
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeFloat(a.x);
		out.writeFloat(a.y);
		out.writeFloat(b.x);
		out.writeFloat(b.y);
		out.writeFloat(c.x);
		out.writeFloat(c.y);
		out.writeFloat(d.x);
		out.writeFloat(d.y);
	}

}
