package com.vishnu.storm;

import java.util.Properties;

import org.apache.storm.hdfs.bolt.HdfsBolt;

import com.vishnu.storm.bolt.BoltBuilder;
import com.vishnu.storm.bolt.MongodbBolt;
import com.vishnu.storm.bolt.SinkTypeBolt;
import com.vishnu.storm.bolt.SolrBolt;
import com.vishnu.storm.spout.SpoutBuilder;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.kafka.KafkaSpout;

/**
 * @author vishnu viswanath
 * This is the main topology class. 
 * All the spouts and bolts are linked together and is submitted on to the cluster
 */
public class Topology {
	
	public Properties configs;
	public BoltBuilder boltBuilder;
	public SpoutBuilder spoutBuilder;
	public static final String SOLR_STREAM = "solr-stream";
	public static final String HDFS_STREAM = "hdfs-stream";
	public static final String MONGODB_STREAM = "mongodb-stream";
	

	public Topology(String configFile) throws Exception {
		configs = new Properties();
		try {
			configs.load(Topology.class.getResourceAsStream("/default_config.properties"));
			boltBuilder = new BoltBuilder(configs);
			spoutBuilder = new SpoutBuilder(configs);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}

	private void submitTopology() throws Exception {
		TopologyBuilder builder = new TopologyBuilder();	
		KafkaSpout kafkaSpout = spoutBuilder.buildKafkaSpout();
		SinkTypeBolt sinkTypeBolt = boltBuilder.buildSinkTypeBolt();
		SolrBolt solrBolt = boltBuilder.buildSolrBolt();
		HdfsBolt hdfsBolt = boltBuilder.buildHdfsBolt();
		MongodbBolt mongoBolt = boltBuilder.buildMongodbBolt();
		
		
		//set the kafkaSpout to topology
		//parallelism-hint for kafkaSpout - defines number of executors/threads to be spawn per container
		int kafkaSpoutCount = Integer.parseInt(configs.getProperty(Keys.KAFKA_SPOUT_COUNT));
		builder.setSpout(configs.getProperty(Keys.KAFKA_SPOUT_ID), kafkaSpout, kafkaSpoutCount);
		
		
		//set the sinktype bolt
		int sinkBoltCount = Integer.parseInt(configs.getProperty(Keys.SINK_BOLT_COUNT));
		builder.setBolt(configs.getProperty(Keys.SINK_TYPE_BOLT_ID),sinkTypeBolt,sinkBoltCount).shuffleGrouping(configs.getProperty(Keys.KAFKA_SPOUT_ID));
		
		//set the solr bolt
		int solrBoltCount = Integer.parseInt(configs.getProperty(Keys.SOLR_BOLT_COUNT));
		builder.setBolt(configs.getProperty(Keys.SOLR_BOLT_ID), solrBolt,solrBoltCount).shuffleGrouping(configs.getProperty(Keys.SINK_TYPE_BOLT_ID),SOLR_STREAM);
			
		//set the hdfs bolt
		int hdfsBoltCount = Integer.parseInt(configs.getProperty(Keys.HDFS_BOLT_COUNT));
		builder.setBolt(configs.getProperty(Keys.HDFS_BOLT_ID),hdfsBolt,hdfsBoltCount).shuffleGrouping(configs.getProperty(Keys.SINK_TYPE_BOLT_ID),HDFS_STREAM);
		
		//set the mongodb bolt
		int mongoBoltCount = Integer.parseInt(configs.getProperty(Keys.MONGO_BOLT_COUNT));
		builder.setBolt(configs.getProperty(Keys.MONGO_BOLT_ID),mongoBolt,mongoBoltCount).shuffleGrouping(configs.getProperty(Keys.SINK_TYPE_BOLT_ID),MONGODB_STREAM);
		
		
		Config conf = new Config();
		conf.put("solr.zookeeper.hosts",configs.getProperty(Keys.SOLR_ZOOKEEPER_HOSTS));
		String topologyName = configs.getProperty(Keys.TOPOLOGY_NAME);
		//Defines how many worker processes have to be created for the topology in the cluster.
		conf.setNumWorkers(1);
		StormSubmitter.submitTopology(topologyName, conf, builder.createTopology());
	}

	public static void main(String[] args) throws Exception {
		String configFile;
		if (args.length == 0) {
			System.out.println("Missing input : config file location, using default");
			configFile = "default_config.properties";
			
		} else{
			configFile = args[0];
		}
		
		Topology ingestionTopology = new Topology(configFile);
		ingestionTopology.submitTopology();
	}
}
