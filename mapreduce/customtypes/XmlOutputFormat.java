package com.vishnuviswanath.examples.customtypes;

import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.Progressable;

/**
 * Xml output format from:
 * http://developer.yahoo.com/hadoop/tutorial/module5.html#outputformat
 * @author vishnu
 *
 * @param <K>
 * @param <V>
 */
public class XmlOutputFormat<K, V> extends FileOutputFormat {

	protected static class XmlRecordWriter<K, V> implements RecordWriter<K, V> {
		private static final String utf8 = "UTF-8";
		private DataOutputStream out;

		public XmlRecordWriter(DataOutputStream out) throws IOException {
			this.out = out;
			out.writeBytes("<results>\n");
		}

		@Override
		public void close(Reporter arg0) throws IOException {
			try {
		        out.writeBytes("</results>\n");
		      } finally {
		        // even if writeBytes() fails, make sure we close the stream
		        out.close();
		      }
		}

		@Override
		public void write(K key, V value) throws IOException {
			boolean nullKey = key == null || key instanceof NullWritable;
		    boolean nullValue = value == null || value instanceof NullWritable;
		    
			if (nullKey && nullValue) {
				return;
			}	
			Object keyObj = key;

			if (nullKey) {
				keyObj = "value";
			}		
			writeKey(keyObj, false);
			
			if (!nullValue) {
				writeObject(value);
			}		
			writeKey(keyObj, true);
		}
		
		private void writeKey(Object o, boolean closing) throws IOException {
			out.writeBytes("<");
			if (closing) {
				out.writeBytes("/");
			}
			writeObject(o);
			out.writeBytes(">");
			if (closing) {
				out.writeBytes("\n");
			}
		}
		
		private void writeObject(Object o) throws IOException {
			if (o instanceof Text) {
				Text to = (Text) o;
				out.write(to.getBytes(), 0, to.getLength());
			} else {
				out.write(o.toString().getBytes(utf8));
			}
		}

	}

	@Override
	public RecordWriter getRecordWriter(FileSystem ignored, JobConf conf,
			String name, Progressable progress) throws IOException {
		Path file = FileOutputFormat.getTaskOutputPath(conf, name);
	    FileSystem fs = file.getFileSystem(conf);
	    FSDataOutputStream fileOut = fs.create(file, progress);
	    return new XmlRecordWriter<K, V>(fileOut);
	}

}
