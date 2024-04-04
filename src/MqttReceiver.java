import org.eclipse.paho.client.mqttv3.*;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;

public class MqttReceiver {

    private static final Logger logger = LoggerFactory.getLogger(MqttReceiver.class);
    private static final int CHECKSUM_INDEX = 26;
    private static final String AES_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    public void start() {
        String broker = "tcp://127.0.0.1:1883";
        String topic = "transport/data";
        String clientId = "JavaSubscriber";

        String kafkaBootstrapServers = "localhost:9092";
        Properties kafkaProps = KafkaSender.getKafkaProperties(kafkaBootstrapServers);
        KafkaProducer<String, String> producer = new KafkaProducer<>(kafkaProps);

        // ExecutorService executor = Executors.newFixedThreadPool(2); // Creating a
        // thread pool with 2 threads

        try {
            MqttClient client = new MqttClient(broker, clientId);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    logger.error("Connection lost!", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    handleMessage(message, producer, topic); // Submitting task to the thread
                                                             // pool
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Not used in this example
                }
            });

            client.connect();
            client.subscribe(topic);
        } catch (MqttException e) {
            logger.error("Error connecting to MQTT broker", e);
            producer.close();

        } finally {
            logger.error("Thota");
            // executor.shutdown(); // Shutting down the executor
        }
    }

    private void handleMessage(MqttMessage message, KafkaProducer<String, String> producer, String topic) {
        byte[] encryptedData = message.getPayload();
        String decryptedData = decryptData(encryptedData, "Sixteen byte key");
        if (decryptedData != null) {
            logger.info("Decrypted data: {}", decryptedData);
            String jsonObjectString = parseDecryptedData(decryptedData);
            if (jsonObjectString != null) {
                logger.info("Parsed JSON data: {}", jsonObjectString);
                JSONObject jsonObject = new JSONObject(jsonObjectString);
                String packetType = jsonObject.optString("PacketType");
                logger.info("PacketType: {}", packetType);
                sendToCorrectKafkaTopic(packetType, jsonObjectString, producer);
                // KafkaSender.sendToKafka("xyz", jsonObjectString, producer);
            } else {
                logger.warn("Parsed JSON object is null.");
            }
        }
    }

    private void sendToCorrectKafkaTopic(String packetType, String jsonObjectString,
            KafkaProducer<String, String> producer) {
        String kafkaTopic;
        switch (packetType) {
            case "nr":
                kafkaTopic = "xyz"; // Kafka topic for nr packets
                break;
            case "obd":
                kafkaTopic = "xyz"; // Kafka topic for obd packets
                break;
            case "dtc":
                kafkaTopic = "xyz"; // Kafka topic for dtc packets
                break;
            default:
                kafkaTopic = "xyz"; // Default Kafka topic if packetType is not recognized
        }
        // Send data to the determined Kafka topic
        KafkaSender.sendToKafka(kafkaTopic, jsonObjectString, producer);
    }

    private String decryptData(byte[] encryptedData, String key) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            byte[] iv = new byte[16];
            System.arraycopy(encryptedData, 0, iv, 0, iv.length);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] decrypted = cipher.doFinal(encryptedData, 16, encryptedData.length - 16);
            return new String(decrypted, "UTF-8").trim();
        } catch (Exception e) {
            logger.error("Error decrypting data: {}", e.getMessage());
            return null;
        }
    }

    private String parseDecryptedData(String decryptedData) {
        try {
            String[] fields = decryptedData.split(",");
            String incomingChecksum = fields[CHECKSUM_INDEX].trim().substring(1);
            String checksum = calculateChecksum(decryptedData);

            if (!checksum.equals(incomingChecksum)) {
                logger.error("Checksum does not match! Corrupted data: {}", decryptedData);
                return null;
            } else {
                JSONObject jsonObject = new JSONObject();
                for (int i = 0; i < fields.length; i++) {
                    jsonObject.put(getFieldName(i), fields[i].trim());
                }
                jsonObject.put("Checksum", checksum);
                return jsonObject.toString();
            }
        } catch (Exception e) {
            logger.error("Failed to parse decrypted data: {}", decryptedData, e);
            return null;
        }
    }

    private String getFieldName(int index) {
        switch (index) {
            case 0:
                return "StartCharacter";
            case 1:
                return "Header";
            case 2:
                return "FirmwareVersion";
            case 3:
                return "ConfigVersion";
            case 4:
                return "PacketType";
            case 5:
                return "PacketStatus";
            case 6:
                return "IMEI";
            case 7:
                return "GPSFix";
            case 8:
                return "Date";
            case 9:
                return "Time";
            case 10:
                return "Latitude";
            case 11:
                return "LatitudeDirection";
            case 12:
                return "Longitude";
            case 13:
                return "LongitudeDirection";
            case 14:
                return "Speed";
            case 15:
                return "Heading";
            case 16:
                return "NoofSatellites";
            case 17:
                return "Altitude";
            case 18:
                return "PDOP";
            case 19:
                return "HDOP";
            case 20:
                return "NetworkOperatorName";
            case 21:
                return "IgnitionStatus";
            case 22:
                return "MainInputVoltage";
            case 23:
                return "GSMSignalStrength";
            case 24:
                return "GPRSStatus";
            case 25:
                return "FrameNumber";
            case 26:
                return "EndCharacter";
            // Add more fields here if needed
            default:
                return "Field" + index;
        }
    }

    private String calculateChecksum(String data) {
        String[] fields = data.split(",");
        int sum = 0;
        for (int i = 1; i < fields.length - 1; i++) {
            sum += fields[i].chars().sum();
        }
        return String.valueOf(sum);
    }
}