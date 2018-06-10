package com.vishnuviswanath.flink.streaming;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;

import java.util.ArrayList;
import java.util.List;

public class HelloStreamingJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment senv = StreamExecutionEnvironment.getExecutionEnvironment();

        List<String> sample = new ArrayList<>();
        sample.add("test");
        sample.add("data");
        DataStream<String> sampleStream = senv.fromCollection(sample);
        sampleStream.addSink(new PrintSinkFunction<>());

        senv.execute("hellow data stream");

    }
}



