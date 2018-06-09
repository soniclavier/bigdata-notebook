package com.vishnuviswanath.flink.streaming.sources;

import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;

public class TextSourceJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment senv = StreamExecutionEnvironment.getExecutionEnvironment();

        /*Checkpoints are used for recovering from failures.
        Default mode is EXACTLY_ONCE, AT_LEAST_ONCE mode can be used for lower latency, since in this case
        elements are not buffered during barrier alignment.

        e.g.,
        operator1  - operator3 - sink
        operator2  /

        in the above example, operator 3 is downstream of both operator 1 & 2. If op3 is received checkpoint barrier n from op1 but not
        from op2, in EXACTLY_ONCE case op3 will buffer and stop processing messages from op1 till barrier from op2 is received. In AT_LEAST_ONCE
        case op3 will not buffer and will continue processing messages/events from op1. i.e., checkpoint #n could potentially have messages of
        checkpoint n & n + 1. If there is failure after checkpoint #n, Flink will restart from last successful checkpoint i.e., n. This will
        introduce duplicates.
        */

        //checkpoint every 20 seconds.
        senv.enableCheckpointing(20000);

        /*
        Checkpoints are used for automatic application restarts during failure, hence it is deleted by default after completion.
        RETAIN_ON_CANCELLATION is used to not delete the checkpoints.
         */
        CheckpointConfig config = senv.getCheckpointConfig();
        config.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);


        DataStream<String> textInput = senv.readFile(new TextInputFormat(new Path(args[0])), args[0], FileProcessingMode.PROCESS_CONTINUOUSLY, 1);
        textInput
                .map(x ->  Double.parseDouble(x))
                .addSink(new PrintSinkFunction<>());

        senv.execute("text stream");
    }
}
