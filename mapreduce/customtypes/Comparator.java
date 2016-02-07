package com.hadoopexpress.examples.customtypes;

/**
 * A custom comparator for comparing Text.
 * source : http://developer.yahoo.com/hadoop/tutorial/module5.html#types
 */
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;


public class Comparator extends WritableComparator {

	protected Comparator() {
		super(Text.class);
	}
	
	public int compare(byte[] b1, int s1, int l1,
            byte[] b2, int s2, int l2) {
		System.out.println("Using custom comparator");		
		int n1 = WritableUtils.decodeVIntSize(b1[s1]);
		int n2 = WritableUtils.decodeVIntSize(b2[s2]);
		return compareBytes(b1, s1+n1, l1-n1, b2, s2+n2, l2-n2);
	}
}
