import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaSender {
    private static final Logger logger = LoggerFactory.getLogger(KafkaSender.class);

    public static void sendToKafka(String topic, String data, KafkaProducer<String, String> producer) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, data);
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                logger.error("Error sending message to Kafka: {}", exception.getMessage());
            }
        });
    }

    public static Properties getKafkaProperties(String bootstrapServers) {
        Properties kafkaProps = new Properties();
        kafkaProps.put("bootstrap.servers", bootstrapServers);
        kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return kafkaProps;
    }
}
