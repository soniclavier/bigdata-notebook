package com.vishnu.storm.bolt;

import java.util.Properties;

import org.apache.storm.hdfs.bolt.HdfsBolt;
import org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat;
import org.apache.storm.hdfs.bolt.format.DelimitedRecordFormat;
import org.apache.storm.hdfs.bolt.format.FileNameFormat;
import org.apache.storm.hdfs.bolt.format.RecordFormat;
import org.apache.storm.hdfs.bolt.rotation.FileRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy.Units;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.hdfs.bolt.sync.SyncPolicy;

import com.vishnu.storm.Keys;

/**
 * @author vishnu viswanath
 * This class is used for building bolts
 */
public class BoltBuilder {
	
	public Properties configs = null;
	
	public BoltBuilder(Properties configs) {
		this.configs = configs;
	}
	
	public SinkTypeBolt buildSinkTypeBolt() {
		return new SinkTypeBolt();
	}
	
	public MongodbBolt buildMongodbBolt() {
		String host = configs.getProperty(Keys.MONGO_HOST);
		int port = Integer.parseInt(configs.getProperty(Keys.MONGO_PORT));
		String db = configs.getProperty(Keys.MONGO_DATABASE);
		String collection = configs.getProperty(Keys.MONGO_COLLECTION);
		return new MongodbBolt(host, port, db, collection);
	}
	
	public  SolrBolt buildSolrBolt() {
		String solrServerUlr = configs.getProperty(Keys.SOLR_SERVER);
		String collection = configs.getProperty(Keys.SOLR_COLLECTION);
		SolrBolt solrBolt = new SolrBolt(solrServerUlr+collection);
		return solrBolt;
	}
	
	public HdfsBolt buildHdfsBolt() {
		RecordFormat format = new DelimitedRecordFormat().withFieldDelimiter("|");
		SyncPolicy syncPolicy = new CountSyncPolicy(1);
		FileRotationPolicy rotationPolicy = new FileSizeRotationPolicy(5.0f, Units.MB);
		FileNameFormat fileNameFormat = new DefaultFileNameFormat().withPath(configs.getProperty(Keys.HDFS_FOLDER));
		String port = configs.getProperty((Keys.HDFS_PORT));
		String host = configs.getProperty((Keys.HDFS_HOST));
		HdfsBolt bolt = new HdfsBolt()
        .withFsUrl("hdfs://"+host+":"+port)
        .withFileNameFormat(fileNameFormat)
        .withRecordFormat(format)
        .withRotationPolicy(rotationPolicy)
        .withSyncPolicy(syncPolicy);
		return bolt;
	}

}
