package flink.playground.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import flink.playground.deserializers.EventDeserializationSchema;
import flink.playground.model.Event;
import flink.playground.model.Transaction;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend;
import org.apache.flink.contrib.streaming.state.RocksDBStateBackend;
import org.apache.flink.runtime.executiongraph.failover.flip1.NoRestartBackoffTimeStrategy;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Configuration
public class FlinkConfiguration {
    @Bean
    public StreamExecutionEnvironment streamExecutionEnvironment() throws IOException {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRestartStrategy(RestartStrategies.noRestart());
        ExecutionConfig executionConfig = new ExecutionConfig();
        executionConfig.setRestartStrategy(RestartStrategies.fixedDelayRestart(10, Duration.ofSeconds(3).toMillis()));
        // Configure the RocksDBStateBackend
        // todo: consider using FileSystemCheckpointStorage
        String checkpointDir = "/checkpoint/rocks";
        EmbeddedRocksDBStateBackend stateBackend = new EmbeddedRocksDBStateBackend(true);
        env.setStateBackend(stateBackend);
//        env.getCheckpointConfig().setCheckpointStorage(checkpointDir);

        return env;
    }

    @Bean
    public KafkaSource<Event> createEventKafkaSource(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${kafka.topic}") String topic) {
        Properties kafkaProperties = new Properties();
        kafkaProperties.setProperty("bootstrap.servers", bootstrapServers);
        return KafkaSource.<Event>builder()
                .setBootstrapServers(bootstrapServers)
                .setGroupId("MyGroup")
                .setTopics(Collections.singletonList(topic))
                .setDeserializer(KafkaRecordDeserializationSchema.of(new EventDeserializationSchema(new ObjectMapper())))
                .setStartingOffsets(OffsetsInitializer.earliest())
                .build();
    }
}
