package incrementaljob;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import datafu.hourglass.model.KeyValueCollector;
import datafu.hourglass.model.Mapper;

public class IncrementalMapper implements Mapper<GenericRecord,GenericRecord,GenericRecord>
{
	
	private transient Schema kSchema;
    private transient Schema vSchema;
    private String keySchemaString;
    private String valueSchemaString;
    
	public IncrementalMapper(String keySchemaString,String valueSchemaString) {
		this.keySchemaString = keySchemaString;
		this.valueSchemaString = valueSchemaString;
	}
	
    
	@Override
	public void map(GenericRecord input,
			KeyValueCollector<GenericRecord, GenericRecord> collector)
			throws IOException, InterruptedException {
		  if (kSchema == null) kSchema = new Schema.Parser().parse(keySchemaString);
	      if (vSchema == null) vSchema = new Schema.Parser().parse(valueSchemaString);
	      GenericRecord key = new GenericData.Record(kSchema);
	      key.put("town", input.get("town"));
	      GenericRecord value = new GenericData.Record(vSchema);
	      value.put("value",input.get("value")); // member id   
	      collector.collect(key,value);
	}
}


