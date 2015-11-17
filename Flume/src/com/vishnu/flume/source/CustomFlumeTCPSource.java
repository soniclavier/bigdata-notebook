package com.vishnu.flume.source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A custom flume source to connect listen to a TCP port
 */
public class CustomFlumeTCPSource extends AbstractSource
        implements EventDrivenSource, Configurable {

    private static final Logger logger =
            LoggerFactory.getLogger(CustomFlumeTCPSource.class);
    private int port;
    private int buffer;
    private ServerSocket serverSocket;
    private BufferedReader receiveBuffer;
    private Socket clientSocket;
    /**
     * Configurations for the TCP source
     */
    @Override
    public void configure(Context context) {
        port = context.getInteger("port");
        buffer = context.getInteger("buffer");

        try{
            serverSocket = new ServerSocket(port);
            logger.info("FlumeTCP source initialized");
        }catch(Exception e) {
            logger.error("FlumeTCP source failed to initialize");
        }
    }

    @Override
    public void start() {
        try {
            clientSocket = serverSocket.accept();
            receiveBuffer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            logger.info("Connection established with client : " + clientSocket.getRemoteSocketAddress());
            final ChannelProcessor channel = getChannelProcessor();
            final Map<String, String> headers = new HashMap<String, String>();
            headers.put("hostname", clientSocket.getRemoteSocketAddress().toString());
            String line = "";
            List<Event> events = new ArrayList<Event>();

            while ((line = receiveBuffer.readLine()) != null) {
                Event event = EventBuilder.withBody(
                        line, Charset.defaultCharset(),headers);

                logger.info("Event created");
                events.add(event);
                if (events.size() == buffer) {
                    channel.processEventBatch(events);
                }
            }
        } catch (Exception e) {

        }
        super.start();
    }


    @Override
    public void stop() {
        logger.info("Closing the connection");
        try {
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.stop();
    }
}