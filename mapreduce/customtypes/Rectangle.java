package com.vishnuviswanath.examples.customtypes;

/**
 * A custom Key type corresponding to a rectangle
 * 
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
		System.out.println("Called constructor with points"+a.toString()+b.toString()+c.toString()+d.toString());
	}
	
	public Rectangle() {
		Point2D point = new Point2D(0,0);
		this.a = point;
		this.b = point;
		this.c = point;
		this.d = point;
		System.out.println("Called empty constructor");
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		int p = 0;
		System.out.println("Called read on rectangle");
		while (p < 4) {
			float x = in.readFloat();
			System.out.println("Read "+x);
			float y = in.readFloat();
			System.out.println("Read "+y);
			System.out.println("--------");
			if (p == 0)
				a = new Point2D(x,y);
			if (p == 1)
				b = new Point2D(x,y);
			if (p == 2)
				c = new Point2D(x,y);
			if (p == 3)
				d = new Point2D(x,y);
			p++;
		}
		System.out.println("This rectangle is now "+this.toString());
		System.out.println("---------------------------");
	}

	@Override
	public void write(DataOutput out) throws IOException {
		System.out.println("Called write on Rectangle");
		System.out.println(a.x+" "+a.y);
		System.out.println(b.x+" "+b.y);
		System.out.println(c.x+" "+c.y);
		System.out.println(d.x+" "+d.y);
		System.out.println("--------------------");
		out.writeFloat(a.x);
		out.writeFloat(a.y);
		out.writeFloat(b.x);
		out.writeFloat(b.y);
		out.writeFloat(c.x);
		out.writeFloat(c.y);
		out.writeFloat(d.x);
		out.writeFloat(d.y);
		
	}

	
	@Override 
	public String toString() {
	  String returnFormat = "[ (ax,ay),(bx,by),(cx,cy),(dx,dy) ]";
	  returnFormat = returnFormat.replace("ax",a.x+"");
	  returnFormat = returnFormat.replace("ay",a.y+"");
	  returnFormat = returnFormat.replace("bx",b.x+"");
	  returnFormat = returnFormat.replace("by",b.y+"");
	  returnFormat = returnFormat.replace("cx",c.x+"");
	  returnFormat = returnFormat.replace("cy",c.y+"");
	  returnFormat = returnFormat.replace("dx",d.x+"");
	  returnFormat = returnFormat.replace("dy",d.y+"");
	  return returnFormat;
	  
	}

}
