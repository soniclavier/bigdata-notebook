### Flume Custom TCP Source

CustomFlumeTCPSource.java is custom flume source which listens to a port and sends the content to the configured channel. The custom source adds the client information to the header of message before sending to the channel.
It takes two configurations

1. port - the port to listen to
2. buffer - how often should the events be send to the channel

#### Sample configuration
agent.sources.CustomTcpSource.type = com.vishnu.flume.source.CustomFlumeTCPSource
agent.sources.CustomTcpSource.port = 4443
agent.sources.CustomTcpSource.buffer = 1


