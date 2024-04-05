# command to run java program
**To Compile** : make
**To Run** : make run
**To Clean**: make clean

## Commands to run kafka, Zookeper 
> cd C:\zookeeper
> bin\zkServer.cmd

**Kafka**
> cd C:\kafka
> .\bin\windows\kafka-server-start.bat .\config\server.properties


> .\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic xyz
> .\bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic xyz


