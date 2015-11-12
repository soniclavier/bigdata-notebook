package com.vishnu.storm;

/**
 * @author vishnu viswanath
 * This is an utility class. It contains the keys that should be present in the input config-file
 */
public class Keys {
	
	
	public static final String TOPOLOGY_NAME                  = "topology";
	
	//kafka spout
	public static final String KAFKA_SPOUT_ID = "kafka-spout";
	public static final String KAFKA_ZOOKEEPER               = "kafka.zookeeper";
	public static final String KAFKA_TOPIC              = "kafa.topic";
	public static final String KAFKA_ZKROOT                    = "kafka.zkRoot";
	public static final String KAFKA_CONSUMERGROUP     = "kafka.consumer.group";
	public static final String KAFKA_SPOUT_COUNT          = "kafkaspout.count";
		
	//sink bolt
	public static final String SINK_TYPE_BOLT_ID = "sink-type-bolt";
	public static final String SINK_BOLT_COUNT = "sinkbolt.count";
	
	//solr bolt
	public static final String SOLR_BOLT_ID = "solr-bolt";
	public static final String SOLR_BOLT_COUNT = "solrbolt.count";
	public static final String SOLR_COLLECTION = "solr.collection";
	public static final String SOLR_SERVER = "solr.url";
	public static final String SOLR_ZOOKEEPER_HOSTS = "solr.zookeeper.hosts";
	
	//hdfs bolt 
	public static final String HDFS_BOLT_ID = "hdfs-bolt";
	public static final String HDFS_BOLT_COUNT = "hdfsbolt.count";
	public static final String HDFS_FOLDER = "hdfs.folder";
	public static final String HDFS_PORT = "hdfs.port";
	public static final String HDFS_HOST = "hdfs.host";
	
	//mongodb bolt
	public static final String MONGO_BOLT_ID = "mongodb.bolt.id";
	public static final String MONGO_HOST = "mongodb.host";
	public static final String MONGO_PORT = "mongodb.port";
	public static final String MONGO_DATABASE = "mongodb.database";
	public static final String MONGO_COLLECTION = "mongodb.collection";
	public static final String MONGO_BOLT_COUNT = "mongodbbolt.count";
	
	
	
	
}
