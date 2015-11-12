package com.vishnu.storm.bolt;

import java.util.Map;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;


public class MongodbBolt extends BaseRichBolt {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OutputCollector collector;
	private MongoDatabase mongoDB;
	private MongoClient mongoClient;
	private String collection;
	
	public String host;
	public int port ;
	public String db;

	protected MongodbBolt(String host, int port, String db,String collection) {
		this.host = host;
		this.port = port;
		this.db = db;
		this.collection = collection;
	}
	
	
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.mongoClient = new MongoClient(host,port);
		this.mongoDB = mongoClient.getDatabase(db);
	}

	
	public void execute(Tuple input) {
		
		Document mongoDoc = getMongoDocForInput(input);
		try{
			mongoDB.getCollection(collection).insertOne(mongoDoc);
			collector.ack(input);
		}catch(Exception e) {
			e.printStackTrace();
			collector.fail(input);
		}
	}

	
	@Override
	public void cleanup() {
		this.mongoClient.close();
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
	}
	
	public Document  getMongoDocForInput(Tuple input) {
		Document doc = new Document();
		String content = (String) input.getValueByField("content");
		String[] parts = content.trim().split(" ");
		System.out.println("Received in MongoDB bolt "+content);
		try {
			for(String part : parts) {
				String[] subParts = part.split(":");
				String fieldName = subParts[0];
				String value = subParts[1];
				doc.append(fieldName, value);
			}
		} catch(Exception e) {
			
		}
		return doc;
	}
	


}