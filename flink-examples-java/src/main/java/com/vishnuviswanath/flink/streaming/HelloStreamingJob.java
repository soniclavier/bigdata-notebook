package com.vishnuviswanath.flink.streaming;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;

public class HelloStreamingJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment senv = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<String> socketDataStream = senv.socketTextStream("localhost", 9999);
        DataStream<SensorData> sensorDataStream = socketDataStream.map(x -> new SensorData(x));
        sensorDataStream.addSink(new PrintSinkFunction<>());

        senv.execute("Sensor data stream");

    }
}

class SensorData {
    double reading;

    public SensorData(String reading) {
        this.reading = Double.parseDouble(reading);
    }

    @Override
    public String toString() {
     return String.format("{Temp : %10.4f}", reading);
    }
}


