package com.codemaster.demo.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaReceiver {

    public static void main(String[] args) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.15.6:9092,192.168.15.6:9093,192.168.15.6:9094");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
        config.put(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG,  100 * 60 * 1000);
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 100 * 60 * 1000);
        config.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 100 * 60 * 1000);

        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(config, new StringDeserializer(), new StringDeserializer());

        consumer.subscribe(Collections.singletonList("test"));

        AtomicBoolean running = new AtomicBoolean(true);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running.set(false);
            consumer.wakeup();
        }));

        try {
            while (running.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(3));

                if (records.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<String, String> record : records) {
                    System.out.println(record);
                }
            }
        } finally {
            consumer.close();
        }
    }
}
