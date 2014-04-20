package incrementaljob;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import datafu.hourglass.model.Accumulator;

public class IncrementalAccumulator implements
		Accumulator<GenericRecord, GenericRecord> {
	private transient long sum;
	private transient Schema oSchema;
	private String outputSchemaString;
	
	public IncrementalAccumulator(String outputSchemaString) {
		this.outputSchemaString = outputSchemaString;
	}
	
	@Override
	public void accumulate(GenericRecord value) {
		this.sum += (Long) value.get("value");
	}

	@Override
	public GenericRecord getFinal() {
		if (oSchema == null) {
			oSchema = new Schema.Parser().parse(outputSchemaString);
		}
		GenericRecord output = new GenericData.Record(oSchema);
		output.put("kpi", sum);
		return output;
	}

	@Override
	public void cleanup() {
		this.sum = 0;
	}
}
