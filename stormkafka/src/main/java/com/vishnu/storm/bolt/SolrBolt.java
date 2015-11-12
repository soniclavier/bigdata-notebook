package com.vishnu.storm.bolt;

import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;

/**
 * @author vishnu viswanath
 * This class is used for ingesting data into SOLR
 */
public class SolrBolt extends BaseRichBolt {
	
	private static final long serialVersionUID = 1L;
	private OutputCollector collector;
	SolrClient solrClient;
	String solrAddress;

	/**
	 * 
	 * @param solrAddress url that is used to connect to solr
	 * e.g., http://localhost:8983/solr/collection1"
	 */
	public SolrBolt(String solrAddress) {
		this.solrAddress = solrAddress;
		
	}
	
	public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.solrClient = new HttpSolrClient(solrAddress);
	}


	public void execute(Tuple input) {
		
		SolrInputDocument document = getSolrInputDocumentForInput(input);
		try{
		solrClient.add(document);
		solrClient.commit();
		collector.ack(input);
		}catch(Exception e) {
			
		}

	}

	/**
	 * Converts the tuple into SOLR document.
	 * Input will have the content in the field named "content" ( this is set by the SinkTypeBolt )
	 * It is assumed that the content will be of the format fieldName1:Value1 fieldName2:Value2 ..
	 * @param input 
	 * @return
	 */
	public SolrInputDocument getSolrInputDocumentForInput(Tuple input) {
		String content = (String) input.getValueByField("content");
		String[] parts = content.trim().split(" ");
		System.out.println("Received in SOLR bolt "+content);
		SolrInputDocument document = new SolrInputDocument();
		try {
			for(String part : parts) {
				String[] subParts = part.split(":");
				String fieldName = subParts[0];
				String value = subParts[1];
				document.addField(fieldName, value);
			}
		} catch(Exception e) {
			
		}
		return document;
	}
	
	@Override
	public void cleanup() {
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		
	}

}

