package flink.playground.dev;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import flink.playground.model.Event;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DevEventKafkaProducer {
    public static final short REPLICATION_FACTOR = 1;
    public static final int NUM_PARTITIONS = 1;
    public static final String TOPIC = "noga";
    public static final String BOOTSTRAP_SERVERS = "localhost:9093";

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        try (AdminClient adminClient = AdminClient.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS))) {
            Collection<NewTopic> topics = Collections.singletonList(new NewTopic(TOPIC, NUM_PARTITIONS, REPLICATION_FACTOR));
            if (!adminClient.listTopics().names().get().contains(TOPIC)) {
                adminClient.createTopics(topics).all().get(30, TimeUnit.SECONDS);
            }
        }
        try (
                KafkaProducer<String, String> producer = new KafkaProducer<>(
                        Map.of(
                                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                                BOOTSTRAP_SERVERS,
                                ProducerConfig.CLIENT_ID_CONFIG,
                                UUID.randomUUID().toString()
                        ),
                        new StringSerializer(),
                        new StringSerializer()
                );
        ) {
            String id1 = "ID1";
            String id2 = "ID2";
            var event1 = Event.builder()
                    .id(id1)
                    .transactionId(id1)
                    .coolId("47189473982148932194")
                    .hopId("Ou")
                    .createdDate(new Date())
                    .numbers(Set.of(5627))
                    .build();
            var event2 = Event.builder()
                    .id(id2)
                    .transactionId(id2)
                    .coolId("47189473982148932194")
                    .hopId("Bob")
                    .createdDate(new Date())
                    .numbers(Set.of(5627, 2824))
                    .build();
            var event3 = Event.builder()
                    .id(id1)
                    .transactionId(id1)
                    .coolId("47189473982148932194")
                    .hopId("Bob")
                    .createdDate(new Date())
                    .numbers(Set.of(3566, 2824))
                    .build();
            var event4 = Event.builder()
                    .id(id1)
                    .transactionId(id1)
                    .coolId("47189473982148932194")
                    .hopId("Bob")
                    .createdDate(new Date())
                    .numbers(Set.of(5627, 3566))
                    .build();
            var event5 = Event.builder()
                    .id(id2)
                    .transactionId(id2)
                    .coolId("47189473982148932194")
                    .hopId("Bob")
                    .createdDate(new Date())
                    .numbers(Set.of(2824))
                    .build();
            producer.send(new ProducerRecord<>(TOPIC, id1, new ObjectMapper().writeValueAsString(event1))).get();
            producer.send(new ProducerRecord<>(TOPIC, id2, new ObjectMapper().writeValueAsString(event2))).get();
            producer.send(new ProducerRecord<>(TOPIC, id1, new ObjectMapper().writeValueAsString(event3))).get();
            producer.send(new ProducerRecord<>(TOPIC, id1, new ObjectMapper().writeValueAsString(event4))).get();
            producer.send(new ProducerRecord<>(TOPIC, id2, new ObjectMapper().writeValueAsString(event5))).get();

        } catch (ExecutionException | InterruptedException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
