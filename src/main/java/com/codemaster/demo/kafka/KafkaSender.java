package com.codemaster.demo.kafka;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class KafkaSender {

    public static void main(String[] args) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.15.6:9092,192.168.15.6:9093,192.168.15.6:9094");

        KafkaProducer<String, String> producer = new KafkaProducer<>(config, new StringSerializer(), new StringSerializer());

        Scanner scanner = new Scanner(System.in);

        int num = 0;

        while (true) {
            int times = scanner.nextInt();

            if (times == 0) {
                break;
            }

            for (int i = 0; i < times; i++) {
                String msg = Integer.toString(num);
                ProducerRecord<String, String> record = new ProducerRecord<>("test", msg, msg);
                producer.send(record);
                num++;
            }
        }

        producer.close();
    }
}
