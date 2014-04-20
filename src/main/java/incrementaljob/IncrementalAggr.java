package incrementaljob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.fs.Path;

import datafu.hourglass.jobs.PartitionCollapsingIncrementalJob;
import datafu.hourglass.model.Accumulator;
import datafu.hourglass.model.KeyValueCollector;
import datafu.hourglass.model.Mapper;

public class IncrementalAggr {
	
	
	
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		PartitionCollapsingIncrementalJob job = new PartitionCollapsingIncrementalJob(IncrementalAggr.class);
		final String namespace = "incrementaljob.datafu";
		
		
		//create schema for site and load it into memory.
		final Schema keySchema = Schema.createRecord("Key", null, namespace,false);
		List<Field> keyFields = new ArrayList<Field>();
		Field id = new Field("town",Schema.create(Type.STRING),null,null);
		keyFields.add(id);
		keySchema.setFields(keyFields);
		final String keySchemaString = keySchema.toString(true);
		
		final Schema valueSchema = Schema.createRecord("Value", null, namespace,false);
		List<Field> valueFields = new ArrayList<Field>();
		Field value = new Field("value",Schema.create(Type.LONG),null,null);
		valueFields.add(value);
		valueSchema.setFields(valueFields);
		final String valueSchemaString = valueSchema.toString(true);
		
		
		final Schema outputSchema = Schema.createRecord("Output", null, namespace,false);
		List<Field> outputFields = new ArrayList<Field>();
		Field kpi = new Field("kpi",Schema.create(Type.LONG),null,null);
		outputFields.add(kpi);
		outputSchema.setFields(outputFields);
		final String outputSchemaString = outputSchema.toString(true);
		
		job.setKeySchema(keySchema);
		job.setIntermediateValueSchema(valueSchema);
		job.setOutputValueSchema(valueSchema);
		job.setInputPaths(Arrays.asList(new Path("datafu/data/sitelice")));
		job.setOutputPath(new Path("datafu/data/output"));
		job.setReusePreviousOutput(true);
		
		Mapper mapper = new IncrementalMapper(keySchemaString, valueSchemaString);
		job.setMapper(mapper);
		Accumulator accumulator = new IncrementalAccumulator(outputSchemaString);
		job.setReducerAccumulator(accumulator);
//		job.setCombinerAccumulator(job.getReducerAccumulator());
//		job.setUseCombiner(true);
		job.run();
		 
	}
	

}
