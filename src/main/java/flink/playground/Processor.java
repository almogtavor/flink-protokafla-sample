package flink.playground;

import flink.playground.model.Event;
import flink.playground.model.Transaction;
import flink.playground.transformation.MergeEventsFunction;
import lombok.RequiredArgsConstructor;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Processor implements ApplicationRunner {
    private final StreamExecutionEnvironment env;
    private final KafkaSource<Event> kafkaSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        DataStream<Event> events =
                env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "kafka").name("Kafka source");
        events.print();
        MapStateDescriptor<String, Long> descriptor = new MapStateDescriptor<>(
                "id-state",
                BasicTypeInfo.STRING_TYPE_INFO,
                BasicTypeInfo.LONG_TYPE_INFO
        );
        KeyedStream<Event, String> keyedStream = events
                .keyBy(Event::getTransactionId);

        SingleOutputStreamOperator<Transaction> mergedEvents = keyedStream
                .window(TumblingProcessingTimeWindows.of(Time.seconds(1)))
                .process(new MergeEventsFunction());

        mergedEvents.print();

        // Convert Event to Transaction
        try {
            env.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
